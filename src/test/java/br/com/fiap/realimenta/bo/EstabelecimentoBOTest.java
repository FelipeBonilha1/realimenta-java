package br.com.fiap.realimenta.bo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.realimenta.dao.EstabelecimentoDAO;
import br.com.fiap.realimenta.dao.LoteAlimentoDAO;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.model.Estabelecimento;
import br.com.fiap.realimenta.model.TipoEstabelecimento;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EstabelecimentoBOTest {

    private EstabelecimentoDAO estabelecimentoDAO;
    private LoteAlimentoDAO loteAlimentoDAO;
    private EstabelecimentoBO estabelecimentoBO;

    @BeforeEach
    void preparar() {
        estabelecimentoDAO = mock(EstabelecimentoDAO.class);
        loteAlimentoDAO = mock(LoteAlimentoDAO.class);
        estabelecimentoBO = new EstabelecimentoBO(estabelecimentoDAO, loteAlimentoDAO);
    }

    @Test
    void gravaEmailEmMinusculas() {
        when(estabelecimentoDAO.buscarPorEmail("contato@padaria.example")).thenReturn(Optional.empty());
        when(estabelecimentoDAO.buscarPorId(8L)).thenAnswer(chamada -> {
            Estabelecimento lido = cadastro();
            lido.setId(8L);
            lido.setEmail("contato@padaria.example");
            return Optional.of(lido);
        });
        org.mockito.Mockito.doAnswer(chamada -> {
            Estabelecimento gravado = chamada.getArgument(0);
            gravado.setId(8L);
            return null;
        }).when(estabelecimentoDAO).inserir(any(Estabelecimento.class));

        Estabelecimento entrada = cadastro();
        entrada.setEmail("  Contato@Padaria.Example  ");
        estabelecimentoBO.criar(entrada);

        ArgumentCaptor<Estabelecimento> captor = ArgumentCaptor.forClass(Estabelecimento.class);
        verify(estabelecimentoDAO).inserir(captor.capture());
        assertEquals("contato@padaria.example", captor.getValue().getEmail());
        assertEquals("Padaria Sol", captor.getValue().getNome());
    }

    @Test
    void impedeExclusaoQuandoHaLoteVinculado() {
        Estabelecimento existente = cadastro();
        existente.setId(1L);
        when(estabelecimentoDAO.buscarPorId(1L)).thenReturn(Optional.of(existente));
        when(loteAlimentoDAO.existePorEstabelecimento(1L)).thenReturn(true);

        assertThrows(ConflitoNegocioException.class, () -> estabelecimentoBO.excluir(1L));
        verify(estabelecimentoDAO, never()).excluir(any());
    }

    private Estabelecimento cadastro() {
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setNome("  Padaria Sol  ");
        estabelecimento.setTipo(TipoEstabelecimento.PADARIA);
        estabelecimento.setEmail("contato@padaria.example");
        estabelecimento.setTelefone("11999999999");
        estabelecimento.setEndereco("Rua A, 10");
        estabelecimento.setBairro("Vila Mariana");
        estabelecimento.setCidade("São Paulo");
        return estabelecimento;
    }
}
