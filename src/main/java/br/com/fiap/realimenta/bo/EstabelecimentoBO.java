package br.com.fiap.realimenta.bo;

import br.com.fiap.realimenta.dao.EstabelecimentoDAO;
import br.com.fiap.realimenta.dao.LoteAlimentoDAO;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.exception.DadosInvalidosException;
import br.com.fiap.realimenta.exception.Integridade;
import br.com.fiap.realimenta.exception.RecursoNaoEncontradoException;
import br.com.fiap.realimenta.model.Estabelecimento;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstabelecimentoBO {

    private final EstabelecimentoDAO estabelecimentoDAO;
    private final LoteAlimentoDAO loteAlimentoDAO;

    public EstabelecimentoBO(EstabelecimentoDAO estabelecimentoDAO, LoteAlimentoDAO loteAlimentoDAO) {
        this.estabelecimentoDAO = estabelecimentoDAO;
        this.loteAlimentoDAO = loteAlimentoDAO;
    }

    @Transactional(readOnly = true)
    public List<Estabelecimento> listar() {
        return estabelecimentoDAO.listar();
    }

    @Transactional(readOnly = true)
    public Estabelecimento buscar(Long id) {
        return estabelecimentoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado."));
    }

    @Transactional
    public Estabelecimento criar(Estabelecimento estabelecimento) {
        normalizar(estabelecimento);
        if (estabelecimentoDAO.buscarPorEmail(estabelecimento.getEmail()).isPresent()) {
            throw new ConflitoNegocioException("Já existe um estabelecimento com este e-mail.");
        }
        try {
            estabelecimentoDAO.inserir(estabelecimento);
        } catch (DataIntegrityViolationException ex) {
            throw Integridade.conflito(ex, "Já existe um estabelecimento com este e-mail.");
        }
        return buscar(estabelecimento.getId());
    }

    @Transactional
    public Estabelecimento atualizar(Long id, Estabelecimento dados) {
        Estabelecimento atual = buscar(id);
        normalizar(dados);
        Optional<Estabelecimento> outro = estabelecimentoDAO.buscarPorEmail(dados.getEmail());
        if (outro.isPresent() && !outro.get().getId().equals(id)) {
            throw new ConflitoNegocioException("Já existe um estabelecimento com este e-mail.");
        }
        atual.setNome(dados.getNome());
        atual.setTipo(dados.getTipo());
        atual.setEmail(dados.getEmail());
        atual.setTelefone(dados.getTelefone());
        atual.setEndereco(dados.getEndereco());
        atual.setBairro(dados.getBairro());
        atual.setCidade(dados.getCidade());
        try {
            estabelecimentoDAO.atualizar(atual);
        } catch (DataIntegrityViolationException ex) {
            throw Integridade.conflito(ex, "Já existe um estabelecimento com este e-mail.");
        }
        return buscar(id);
    }

    @Transactional
    public void excluir(Long id) {
        buscar(id);
        if (loteAlimentoDAO.existePorEstabelecimento(id)) {
            throw new ConflitoNegocioException(
                    "Não é possível excluir o estabelecimento porque existem lotes vinculados.");
        }
        if (estabelecimentoDAO.excluir(id) != 1) {
            throw new RecursoNaoEncontradoException("Estabelecimento não encontrado.");
        }
    }

    private void normalizar(Estabelecimento estabelecimento) {
        estabelecimento.setNome(obrigatorio(estabelecimento.getNome(), "O nome é obrigatório."));
        estabelecimento.setTelefone(obrigatorio(estabelecimento.getTelefone(), "O telefone é obrigatório."));
        estabelecimento.setEndereco(obrigatorio(estabelecimento.getEndereco(), "O endereço é obrigatório."));
        estabelecimento.setBairro(obrigatorio(estabelecimento.getBairro(), "O bairro é obrigatório."));
        estabelecimento.setCidade(obrigatorio(estabelecimento.getCidade(), "A cidade é obrigatória."));
        estabelecimento.setEmail(obrigatorio(estabelecimento.getEmail(), "O e-mail é obrigatório.")
                .toLowerCase(Locale.ROOT));
        if (estabelecimento.getTipo() == null) {
            throw new DadosInvalidosException("O tipo de estabelecimento é obrigatório.");
        }
    }

    private String obrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException(mensagem);
        }
        return valor.trim();
    }
}
