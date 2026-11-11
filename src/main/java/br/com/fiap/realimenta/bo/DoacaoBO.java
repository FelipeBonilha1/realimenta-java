package br.com.fiap.realimenta.bo;

import br.com.fiap.realimenta.dao.DoacaoDAO;
import br.com.fiap.realimenta.dao.InstituicaoDAO;
import br.com.fiap.realimenta.dao.LoteAlimentoDAO;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.exception.DadosInvalidosException;
import br.com.fiap.realimenta.exception.Integridade;
import br.com.fiap.realimenta.exception.RecursoNaoEncontradoException;
import br.com.fiap.realimenta.model.Doacao;
import br.com.fiap.realimenta.model.LoteAlimento;
import br.com.fiap.realimenta.model.StatusDoacao;
import br.com.fiap.realimenta.model.StatusLote;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cada operacao do fluxo grava doacao e lote na mesma transacao.
 * A trava FOR UPDATE vale ate o commit ou o rollback deste metodo.
 */
@Service
public class DoacaoBO {

    private static final Logger LOG = LoggerFactory.getLogger(DoacaoBO.class);

    private final DoacaoDAO doacaoDAO;
    private final LoteAlimentoDAO loteAlimentoDAO;
    private final InstituicaoDAO instituicaoDAO;
    private final Clock clock;

    public DoacaoBO(DoacaoDAO doacaoDAO, LoteAlimentoDAO loteAlimentoDAO,
                    InstituicaoDAO instituicaoDAO, Clock clock) {
        this.doacaoDAO = doacaoDAO;
        this.loteAlimentoDAO = loteAlimentoDAO;
        this.instituicaoDAO = instituicaoDAO;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Doacao> listar() {
        return doacaoDAO.listar();
    }

    @Transactional(readOnly = true)
    public Doacao buscar(Long id) {
        return doacaoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doação não encontrada."));
    }

    @Transactional
    public Doacao reservar(Long loteId, Long instituicaoId) {
        if (loteId == null || instituicaoId == null) {
            throw new DadosInvalidosException("O lote e a instituição são obrigatórios.");
        }
        if (instituicaoDAO.buscarPorId(instituicaoId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Instituição não encontrada.");
        }

        LoteAlimento lote = loteAlimentoDAO.buscarPorIdParaAtualizacao(loteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado."));
        LocalDateTime agora = LocalDateTime.now(clock);
        if (lote.getStatus() != StatusLote.DISPONIVEL || !dentroDoPrazo(lote, agora)) {
            throw new ConflitoNegocioException(
                    "O lote só pode ser reservado quando está DISPONIVEL e dentro do prazo de aproveitamento.");
        }
        if (doacaoDAO.buscarPorLoteId(loteId).isPresent()) {
            throw new ConflitoNegocioException(
                    "Este lote já possui uma reserva. Cada lote admite uma única doação.");
        }

        Doacao doacao = new Doacao();
        doacao.setLoteId(loteId);
        doacao.setInstituicaoId(instituicaoId);
        doacao.setStatus(StatusDoacao.RESERVADA);
        doacao.setDataReserva(agora);
        try {
            doacaoDAO.inserir(doacao);
            int lotes = loteAlimentoDAO.atualizarStatus(loteId, StatusLote.RESERVADO, StatusLote.DISPONIVEL);
            if (lotes != 1) {
                throw new ConflitoNegocioException("O lote não pôde ser reservado porque seu estado mudou.");
            }
        } catch (DataIntegrityViolationException ex) {
            throw Integridade.conflito(ex, "Este lote já possui uma reserva. Cada lote admite uma única doação.");
        }
        LOG.info("Lote {} reservado para a instituicao {}.", loteId, instituicaoId);
        return reler(doacao.getId());
    }

    @Transactional
    public Doacao coletar(Long doacaoId) {
        Doacao doacao = travarDoacao(doacaoId);
        if (doacao.getStatus() != StatusDoacao.RESERVADA) {
            throw new ConflitoNegocioException("A coleta só pode ser registrada para uma doação RESERVADA.");
        }
        LoteAlimento lote = travarLote(doacao.getLoteId());
        LocalDateTime agora = LocalDateTime.now(clock);
        if (!dentroDoPrazo(lote, agora)) {
            throw new ConflitoNegocioException("Não é possível coletar após o prazo de aproveitamento.");
        }
        int doacoes = doacaoDAO.registrarColeta(doacaoId, agora);
        int lotes = loteAlimentoDAO.atualizarStatus(lote.getId(), StatusLote.COLETADO, StatusLote.RESERVADO);
        if (doacoes != 1 || lotes != 1) {
            throw new ConflitoNegocioException("A coleta não pôde ser registrada porque o estado mudou.");
        }
        LOG.info("Doacao {} coletada.", doacaoId);
        return reler(doacaoId);
    }

    @Transactional
    public Doacao entregar(Long doacaoId, BigDecimal quantidadeEntregueKg) {
        Doacao doacao = travarDoacao(doacaoId);
        if (doacao.getStatus() != StatusDoacao.COLETADA) {
            throw new ConflitoNegocioException("A entrega só pode ser confirmada depois da coleta.");
        }
        LoteAlimento lote = travarLote(doacao.getLoteId());
        LocalDateTime agora = LocalDateTime.now(clock);
        if (!dentroDoPrazo(lote, agora)) {
            throw new ConflitoNegocioException("Não é possível confirmar a entrega após o prazo de aproveitamento.");
        }
        BigDecimal quantidade = quantidadeEntregue(quantidadeEntregueKg);
        if (quantidade.compareTo(lote.getQuantidadeKg()) > 0) {
            throw new ConflitoNegocioException("A quantidade entregue não pode superar a quantidade ofertada.");
        }
        int doacoes = doacaoDAO.registrarEntrega(doacaoId, agora, quantidade);
        int lotes = loteAlimentoDAO.atualizarStatus(lote.getId(), StatusLote.ENTREGUE, StatusLote.COLETADO);
        if (doacoes != 1 || lotes != 1) {
            throw new ConflitoNegocioException("A entrega não pôde ser confirmada porque o estado mudou.");
        }
        LOG.info("Doacao {} entregue.", doacaoId);
        return reler(doacaoId);
    }

    @Transactional
    public Doacao cancelar(Long doacaoId) {
        Doacao doacao = travarDoacao(doacaoId);
        if (doacao.getStatus() == StatusDoacao.CANCELADA) {
            throw new ConflitoNegocioException("Esta doação já está cancelada.");
        }
        if (doacao.getStatus() != StatusDoacao.RESERVADA) {
            throw new ConflitoNegocioException("O cancelamento só é permitido antes da coleta.");
        }
        LoteAlimento lote = travarLote(doacao.getLoteId());
        int doacoes = doacaoDAO.cancelar(doacaoId);
        int lotes = loteAlimentoDAO.atualizarStatus(lote.getId(), StatusLote.CANCELADO, StatusLote.RESERVADO);
        if (doacoes != 1 || lotes != 1) {
            throw new ConflitoNegocioException("O cancelamento não pôde ser registrado porque o estado mudou.");
        }
        LOG.info("Doacao {} cancelada. O lote {} nao volta a ficar disponivel.", doacaoId, lote.getId());
        return reler(doacaoId);
    }

    private Doacao travarDoacao(Long doacaoId) {
        if (doacaoId == null) {
            throw new DadosInvalidosException("A doação é obrigatória.");
        }
        return doacaoDAO.buscarPorIdParaAtualizacao(doacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doação não encontrada."));
    }

    private LoteAlimento travarLote(Long loteId) {
        return loteAlimentoDAO.buscarPorIdParaAtualizacao(loteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado."));
    }

    private Doacao reler(Long id) {
        return doacaoDAO.buscarPorId(id)
                .orElseThrow(() -> new IllegalStateException("A doação foi gravada, mas não pôde ser relida."));
    }

    private boolean dentroDoPrazo(LoteAlimento lote, LocalDateTime agora) {
        return lote.getPrazoAproveitamento() != null && lote.getPrazoAproveitamento().isAfter(agora);
    }

    private BigDecimal quantidadeEntregue(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DadosInvalidosException("A quantidade entregue deve ser maior que zero.");
        }
        return quantidade.setScale(3, RoundingMode.HALF_UP);
    }
}
