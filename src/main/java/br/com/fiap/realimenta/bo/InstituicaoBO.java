package br.com.fiap.realimenta.bo;

import br.com.fiap.realimenta.dao.DoacaoDAO;
import br.com.fiap.realimenta.dao.InstituicaoDAO;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.exception.DadosInvalidosException;
import br.com.fiap.realimenta.exception.Integridade;
import br.com.fiap.realimenta.exception.RecursoNaoEncontradoException;
import br.com.fiap.realimenta.model.Instituicao;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstituicaoBO {

    private final InstituicaoDAO instituicaoDAO;
    private final DoacaoDAO doacaoDAO;

    public InstituicaoBO(InstituicaoDAO instituicaoDAO, DoacaoDAO doacaoDAO) {
        this.instituicaoDAO = instituicaoDAO;
        this.doacaoDAO = doacaoDAO;
    }

    @Transactional(readOnly = true)
    public List<Instituicao> listar() {
        return instituicaoDAO.listar();
    }

    @Transactional(readOnly = true)
    public Instituicao buscar(Long id) {
        return instituicaoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instituição não encontrada."));
    }

    @Transactional
    public Instituicao criar(Instituicao instituicao) {
        normalizar(instituicao);
        if (instituicaoDAO.buscarPorEmail(instituicao.getEmail()).isPresent()) {
            throw new ConflitoNegocioException("Já existe uma instituição com este e-mail.");
        }
        try {
            instituicaoDAO.inserir(instituicao);
        } catch (DataIntegrityViolationException ex) {
            throw Integridade.conflito(ex, "Já existe uma instituição com este e-mail.");
        }
        return buscar(instituicao.getId());
    }

    @Transactional
    public Instituicao atualizar(Long id, Instituicao dados) {
        Instituicao atual = buscar(id);
        normalizar(dados);
        Optional<Instituicao> outra = instituicaoDAO.buscarPorEmail(dados.getEmail());
        if (outra.isPresent() && !outra.get().getId().equals(id)) {
            throw new ConflitoNegocioException("Já existe uma instituição com este e-mail.");
        }
        atual.setNome(dados.getNome());
        atual.setEmail(dados.getEmail());
        atual.setTelefone(dados.getTelefone());
        atual.setEndereco(dados.getEndereco());
        atual.setBairro(dados.getBairro());
        atual.setCidade(dados.getCidade());
        try {
            instituicaoDAO.atualizar(atual);
        } catch (DataIntegrityViolationException ex) {
            throw Integridade.conflito(ex, "Já existe uma instituição com este e-mail.");
        }
        return buscar(id);
    }

    @Transactional
    public void excluir(Long id) {
        buscar(id);
        if (doacaoDAO.existePorInstituicao(id)) {
            throw new ConflitoNegocioException(
                    "Não é possível excluir a instituição porque existem doações vinculadas.");
        }
        if (instituicaoDAO.excluir(id) != 1) {
            throw new RecursoNaoEncontradoException("Instituição não encontrada.");
        }
    }

    private void normalizar(Instituicao instituicao) {
        instituicao.setNome(obrigatorio(instituicao.getNome(), "O nome é obrigatório."));
        instituicao.setTelefone(obrigatorio(instituicao.getTelefone(), "O telefone é obrigatório."));
        instituicao.setEndereco(obrigatorio(instituicao.getEndereco(), "O endereço é obrigatório."));
        instituicao.setBairro(obrigatorio(instituicao.getBairro(), "O bairro é obrigatório."));
        instituicao.setCidade(obrigatorio(instituicao.getCidade(), "A cidade é obrigatória."));
        instituicao.setEmail(obrigatorio(instituicao.getEmail(), "O e-mail é obrigatório.").toLowerCase(Locale.ROOT));
    }

    private String obrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException(mensagem);
        }
        return valor.trim();
    }
}
