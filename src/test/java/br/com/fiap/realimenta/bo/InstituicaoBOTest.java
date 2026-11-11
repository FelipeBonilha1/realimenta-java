package br.com.fiap.realimenta.bo;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.realimenta.dao.DoacaoDAO;
import br.com.fiap.realimenta.dao.InstituicaoDAO;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.model.Instituicao;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InstituicaoBOTest {

    @Test
    void impedeExclusaoQuandoHaDoacaoVinculada() {
        InstituicaoDAO instituicaoDAO = mock(InstituicaoDAO.class);
        DoacaoDAO doacaoDAO = mock(DoacaoDAO.class);
        InstituicaoBO instituicaoBO = new InstituicaoBO(instituicaoDAO, doacaoDAO);
        Instituicao instituicao = new Instituicao();
        instituicao.setId(2L);
        when(instituicaoDAO.buscarPorId(2L)).thenReturn(Optional.of(instituicao));
        when(doacaoDAO.existePorInstituicao(2L)).thenReturn(true);

        assertThrows(ConflitoNegocioException.class, () -> instituicaoBO.excluir(2L));
        verify(instituicaoDAO, never()).excluir(any());
    }
}
