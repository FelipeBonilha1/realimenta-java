package br.com.fiap.realimenta.bo;

import br.com.fiap.realimenta.dao.DoacaoDAO;
import br.com.fiap.realimenta.dao.EstabelecimentoDAO;
import br.com.fiap.realimenta.dao.LoteAlimentoDAO;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.exception.DadosInvalidosException;
import br.com.fiap.realimenta.exception.Integridade;
import br.com.fiap.realimenta.exception.RecursoNaoEncontradoException;
import br.com.fiap.realimenta.model.LoteAlimento;
import br.com.fiap.realimenta.model.StatusLote;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoteAlimentoBO {

    private final LoteAlimentoDAO loteAlimentoDAO;
    private final EstabelecimentoDAO estabelecimentoDAO;
    private final DoacaoDAO doacaoDAO;
    private final Clock clock;

    public LoteAlimentoBO(LoteAlimentoDAO loteAlimentoDAO, EstabelecimentoDAO estabelecimentoDAO,
                          DoacaoDAO doacaoDAO, Clock clock) {
        this.loteAlimentoDAO = loteAlimentoDAO;
        this.estabelecimentoDAO = estabelecimentoDAO;
        this.doacaoDAO = doacaoDAO;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<LoteAlimento> listar() {
        return loteAlimentoDAO.listar();
    }

    @Transactional(readOnly = true)
    public List<LoteAlimento> listarDisponiveis() {
        return loteAlimentoDAO.listarDisponiveis(LocalDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public LoteAlimento buscar(Long id) {
        return loteAlimentoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado."));
    }

    @Transactional
    public LoteAlimento criar(LoteAlimento lote) {
        preparar(lote);
        if (!lote.getPrazoAproveitamento().isAfter(LocalDateTime.now(clock))) {
            throw new DadosInvalidosException("O prazo de aproveitamento deve ser posterior ao momento atual.");
        }
        exigirEstabelecimento(lote.getEstabelecimentoId());
        lote.setStatus(StatusLote.DISPONIVEL);
        try {
            loteAlimentoDAO.inserir(lote);
        } catch (DataIntegrityViolationException ex) {
            throw Integridade.conflito(ex, "Não foi possível gravar o lote.");
        }
        return buscar(lote.getId());
    }

    /**
     * Trava o lote com SELECT FOR UPDATE antes de aceitar quantidade, doador ou prazo.
     * A reserva trava essa mesma linha e nao trava doacao antes do lote.
     * Coleta e entrega travam a doacao e, depois, o lote. Esta edicao nao trava doacao,
     * entao nao inverte essa ordem. O SQL cadastral nao grava status.
     */
    @Transactional
    public LoteAlimento atualizar(Long id, LoteAlimento dados) {
        LoteAlimento atual = loteAlimentoDAO.buscarPorIdParaAtualizacao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado."));
        preparar(dados);
        exigirEstabelecimento(dados.getEstabelecimentoId());

        LocalDateTime prazoInformado = dados.getPrazoAproveitamento();
        boolean mudouQuantidade = atual.getQuantidadeKg().compareTo(dados.getQuantidadeKg()) != 0;
        boolean mudouDoador = !atual.getEstabelecimentoId().equals(dados.getEstabelecimentoId());
        boolean mudouPrazo = !normalizarPrazo(atual.getPrazoAproveitamento()).equals(prazoInformado);
        if (atual.getStatus() != StatusLote.DISPONIVEL && (mudouQuantidade || mudouDoador || mudouPrazo)) {
            throw new ConflitoNegocioException(
                    "Quantidade, estabelecimento e prazo não podem ser alterados depois que o lote deixa de estar DISPONIVEL.");
        }
        if (atual.getStatus() == StatusLote.DISPONIVEL && !prazoInformado.isAfter(LocalDateTime.now(clock))) {
            throw new DadosInvalidosException("O prazo de aproveitamento deve ser posterior ao momento atual.");
        }

        atual.setEstabelecimentoId(dados.getEstabelecimentoId());
        atual.setDescricao(dados.getDescricao());
        atual.setCategoria(dados.getCategoria());
        atual.setQuantidadeKg(dados.getQuantidadeKg());
        if (mudouPrazo) {
            atual.setPrazoAproveitamento(prazoInformado);
        }
        if (loteAlimentoDAO.atualizarDados(atual) != 1) {
            throw new RecursoNaoEncontradoException("Lote não encontrado.");
        }
        return buscar(id);
    }

    @Transactional
    public void excluir(Long id) {
        buscar(id);
        if (doacaoDAO.buscarPorLoteId(id).isPresent()) {
            throw new ConflitoNegocioException(
                    "Somente um lote sem doação associada pode ser excluído. O cancelamento preserva o histórico.");
        }
        if (loteAlimentoDAO.excluir(id) != 1) {
            throw new RecursoNaoEncontradoException("Lote não encontrado.");
        }
    }

    private void preparar(LoteAlimento lote) {
        if (lote.getDescricao() == null || lote.getDescricao().isBlank()) {
            throw new DadosInvalidosException("A descrição é obrigatória.");
        }
        lote.setDescricao(lote.getDescricao().trim());
        if (lote.getCategoria() == null) {
            throw new DadosInvalidosException("A categoria é obrigatória.");
        }
        if (lote.getEstabelecimentoId() == null) {
            throw new DadosInvalidosException("O estabelecimento é obrigatório.");
        }
        if (lote.getPrazoAproveitamento() == null) {
            throw new DadosInvalidosException("O prazo de aproveitamento é obrigatório.");
        }
        lote.setPrazoAproveitamento(normalizarPrazo(lote.getPrazoAproveitamento()));
        lote.setQuantidadeKg(quantidadePositiva(lote.getQuantidadeKg()));
    }

    private void exigirEstabelecimento(Long estabelecimentoId) {
        if (estabelecimentoDAO.buscarPorId(estabelecimentoId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Estabelecimento não encontrado.");
        }
    }

    private BigDecimal quantidadePositiva(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DadosInvalidosException("A quantidade ofertada deve ser maior que zero.");
        }
        return quantidade.setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Oracle TIMESTAMP sem precisao explicita guarda 6 digitos fracionarios.
     * A mesma truncagem vale na entrada, na comparacao e na gravacao.
     * Se o prazo bloqueado nao mudou nessa precisao, o valor original da linha e mantido.
     */
    private LocalDateTime normalizarPrazo(LocalDateTime prazo) {
        return prazo.truncatedTo(ChronoUnit.MICROS);
    }
}
