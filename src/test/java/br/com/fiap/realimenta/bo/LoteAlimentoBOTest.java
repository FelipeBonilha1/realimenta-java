package br.com.fiap.realimenta.bo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.realimenta.dao.DoacaoDAO;
import br.com.fiap.realimenta.dao.EstabelecimentoDAO;
import br.com.fiap.realimenta.dao.LoteAlimentoDAO;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.exception.DadosInvalidosException;
import br.com.fiap.realimenta.exception.RecursoNaoEncontradoException;
import br.com.fiap.realimenta.model.CategoriaAlimento;
import br.com.fiap.realimenta.model.Doacao;
import br.com.fiap.realimenta.model.Estabelecimento;
import br.com.fiap.realimenta.model.LoteAlimento;
import br.com.fiap.realimenta.model.StatusLote;
import br.com.fiap.realimenta.suporte.RelogioFixo;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LoteAlimentoBOTest {

    private LoteAlimentoDAO loteAlimentoDAO;
    private EstabelecimentoDAO estabelecimentoDAO;
    private DoacaoDAO doacaoDAO;
    private LoteAlimentoBO loteAlimentoBO;

    @BeforeEach
    void preparar() {
        loteAlimentoDAO = mock(LoteAlimentoDAO.class);
        estabelecimentoDAO = mock(EstabelecimentoDAO.class);
        doacaoDAO = mock(DoacaoDAO.class);
        loteAlimentoBO = new LoteAlimentoBO(loteAlimentoDAO, estabelecimentoDAO, doacaoDAO, RelogioFixo.CLOCK);
    }

    @Test
    void criaLoteDisponivelComQuantidadeNormalizada() {
        when(estabelecimentoDAO.buscarPorId(4L)).thenReturn(Optional.of(new Estabelecimento()));
        doAnswer(chamada -> {
            LoteAlimento gravado = chamada.getArgument(0);
            gravado.setId(15L);
            return null;
        }).when(loteAlimentoDAO).inserir(any(LoteAlimento.class));
        when(loteAlimentoDAO.buscarPorId(15L)).thenAnswer(chamada -> {
            LoteAlimento lido = new LoteAlimento();
            lido.setId(15L);
            lido.setStatus(StatusLote.DISPONIVEL);
            return Optional.of(lido);
        });

        LoteAlimento criado = loteAlimentoBO.criar(pedido(new BigDecimal("1.5"), RelogioFixo.AGORA.plusDays(2)));

        ArgumentCaptor<LoteAlimento> captor = ArgumentCaptor.forClass(LoteAlimento.class);
        verify(loteAlimentoDAO).inserir(captor.capture());
        assertEquals(StatusLote.DISPONIVEL, captor.getValue().getStatus());
        assertEquals(0, new BigDecimal("1.500").compareTo(captor.getValue().getQuantidadeKg()));
        assertEquals(15L, criado.getId());
    }

    @Test
    void recusaLoteNovoComPrazoNoPassado() {
        LoteAlimento pedido = pedido(new BigDecimal("2"), RelogioFixo.AGORA.minusMinutes(1));

        assertThrows(DadosInvalidosException.class, () -> loteAlimentoBO.criar(pedido));
        verify(loteAlimentoDAO, never()).inserir(any());
    }

    @Test
    void recusaQuantidadeOfertadaIgualAZero() {
        assertThrows(DadosInvalidosException.class,
                () -> loteAlimentoBO.criar(pedido(BigDecimal.ZERO, RelogioFixo.AGORA.plusDays(1))));
        verify(loteAlimentoDAO, never()).inserir(any());
    }

    @Test
    void recusaLoteDeEstabelecimentoInexistente() {
        when(estabelecimentoDAO.buscarPorId(4L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> loteAlimentoBO.criar(pedido(new BigDecimal("3"), RelogioFixo.AGORA.plusDays(1))));
        verify(loteAlimentoDAO, never()).inserir(any());
    }

    @Test
    void impedeAlterarQuantidadeDeLoteJaReservado() {
        LoteAlimento atual = pedido(new BigDecimal("6.000"), RelogioFixo.AGORA.plusDays(1));
        atual.setId(3L);
        atual.setStatus(StatusLote.RESERVADO);
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(3L)).thenReturn(Optional.of(atual));
        when(estabelecimentoDAO.buscarPorId(4L)).thenReturn(Optional.of(new Estabelecimento()));

        LoteAlimento alteracao = pedido(new BigDecimal("9.000"), atual.getPrazoAproveitamento());
        assertThrows(ConflitoNegocioException.class, () -> loteAlimentoBO.atualizar(3L, alteracao));
        verify(loteAlimentoDAO).buscarPorIdParaAtualizacao(3L);
        verify(loteAlimentoDAO, never()).atualizarDados(any());
        verify(loteAlimentoDAO, never()).atualizarStatus(any(), any(), any());
    }

    @Test
    void atualizacaoCadastralNaoGravaStatus() {
        LoteAlimento atual = pedido(new BigDecimal("6.000"), RelogioFixo.AGORA.plusDays(1));
        atual.setId(3L);
        atual.setStatus(StatusLote.RESERVADO);
        atual.setDescricao("Descricao original");
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(3L)).thenReturn(Optional.of(atual));
        when(loteAlimentoDAO.buscarPorId(3L)).thenReturn(Optional.of(atual));
        when(estabelecimentoDAO.buscarPorId(4L)).thenReturn(Optional.of(new Estabelecimento()));
        when(loteAlimentoDAO.atualizarDados(any(LoteAlimento.class))).thenReturn(1);

        LoteAlimento alteracao = pedido(new BigDecimal("6.000"), atual.getPrazoAproveitamento());
        alteracao.setDescricao("Descricao revisada");
        loteAlimentoBO.atualizar(3L, alteracao);

        verify(loteAlimentoDAO).atualizarDados(any(LoteAlimento.class));
        verify(loteAlimentoDAO, never()).atualizarStatus(any(), any(), any());
        assertEquals(StatusLote.RESERVADO, atual.getStatus());
        assertEquals("Descricao revisada", atual.getDescricao());
    }

    @Test
    void edicaoDecidePeloEstadoTravadoENaoPorUmaLeituraAnterior() {
        LoteAlimento travado = pedido(new BigDecimal("6.000"), RelogioFixo.AGORA.plusDays(1));
        travado.setId(3L);
        travado.setStatus(StatusLote.RESERVADO);
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(3L)).thenReturn(Optional.of(travado));
        when(estabelecimentoDAO.buscarPorId(4L)).thenReturn(Optional.of(new Estabelecimento()));

        LoteAlimento alteracao = pedido(new BigDecimal("7.000"), travado.getPrazoAproveitamento());
        assertThrows(ConflitoNegocioException.class, () -> loteAlimentoBO.atualizar(3L, alteracao));
        verify(loteAlimentoDAO, never()).buscarPorId(3L);
        verify(loteAlimentoDAO, never()).atualizarDados(any());
    }

    @Test
    void recusaPrazoDiferenteDentroDoMesmoSegundoEmLoteReservado() {
        java.time.LocalDateTime original = RelogioFixo.AGORA.plusDays(1).withNano(100_000_000);
        LoteAlimento atual = pedido(new BigDecimal("6.000"), original);
        atual.setId(3L);
        atual.setStatus(StatusLote.RESERVADO);
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(3L)).thenReturn(Optional.of(atual));
        when(estabelecimentoDAO.buscarPorId(4L)).thenReturn(Optional.of(new Estabelecimento()));

        LoteAlimento alteracao = pedido(new BigDecimal("6.000"), original.withNano(100_001_000));
        assertThrows(ConflitoNegocioException.class, () -> loteAlimentoBO.atualizar(3L, alteracao));
        verify(loteAlimentoDAO, never()).atualizarDados(any());
        assertEquals(original, atual.getPrazoAproveitamento());
    }

    @Test
    void preservaOPrazoOriginalQuandoSoMudamNanosAlemDoMicrosegundo() {
        java.time.LocalDateTime original = RelogioFixo.AGORA.plusDays(1).withNano(100_000_789);
        LoteAlimento atual = pedido(new BigDecimal("6.000"), original);
        atual.setId(3L);
        atual.setStatus(StatusLote.RESERVADO);
        atual.setDescricao("Descricao original");
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(3L)).thenReturn(Optional.of(atual));
        when(loteAlimentoDAO.buscarPorId(3L)).thenReturn(Optional.of(atual));
        when(estabelecimentoDAO.buscarPorId(4L)).thenReturn(Optional.of(new Estabelecimento()));
        when(loteAlimentoDAO.atualizarDados(any(LoteAlimento.class))).thenReturn(1);

        LoteAlimento alteracao = pedido(new BigDecimal("6.000"), original.withNano(100_000_123));
        alteracao.setDescricao("Descricao revisada");
        loteAlimentoBO.atualizar(3L, alteracao);

        ArgumentCaptor<LoteAlimento> captor = ArgumentCaptor.forClass(LoteAlimento.class);
        verify(loteAlimentoDAO).atualizarDados(captor.capture());
        assertEquals(original, captor.getValue().getPrazoAproveitamento());
        assertEquals(StatusLote.RESERVADO, captor.getValue().getStatus());
    }

    @Test
    void gravaPrazoSomenteAteMicrossegundoQuandoOLoteAindaEstaDisponivel() {
        java.time.LocalDateTime informado = RelogioFixo.AGORA.plusDays(1).withNano(200_000_789);
        LoteAlimento atual = pedido(new BigDecimal("6.000"), RelogioFixo.AGORA.plusDays(1).withNano(100_000_000));
        atual.setId(3L);
        atual.setStatus(StatusLote.DISPONIVEL);
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(3L)).thenReturn(Optional.of(atual));
        when(loteAlimentoDAO.buscarPorId(3L)).thenReturn(Optional.of(atual));
        when(estabelecimentoDAO.buscarPorId(4L)).thenReturn(Optional.of(new Estabelecimento()));
        when(loteAlimentoDAO.atualizarDados(any(LoteAlimento.class))).thenReturn(1);

        loteAlimentoBO.atualizar(3L, pedido(new BigDecimal("6.000"), informado));

        ArgumentCaptor<LoteAlimento> captor = ArgumentCaptor.forClass(LoteAlimento.class);
        verify(loteAlimentoDAO).atualizarDados(captor.capture());
        assertEquals(informado.truncatedTo(java.time.temporal.ChronoUnit.MICROS), captor.getValue().getPrazoAproveitamento());
        verify(loteAlimentoDAO, never()).atualizarStatus(any(), any(), any());
    }

    @Test
    void impedeExclusaoDeLoteComDoacaoAssociada() {
        LoteAlimento atual = pedido(new BigDecimal("6.000"), RelogioFixo.AGORA.plusDays(1));
        atual.setId(3L);
        when(loteAlimentoDAO.buscarPorId(3L)).thenReturn(Optional.of(atual));
        when(doacaoDAO.buscarPorLoteId(3L)).thenReturn(Optional.of(new Doacao()));

        assertThrows(ConflitoNegocioException.class, () -> loteAlimentoBO.excluir(3L));
        verify(loteAlimentoDAO, never()).excluir(any());
    }

    private LoteAlimento pedido(BigDecimal quantidade, java.time.LocalDateTime prazo) {
        LoteAlimento lote = new LoteAlimento();
        lote.setEstabelecimentoId(4L);
        lote.setDescricao("  Arroz cozido  ");
        lote.setCategoria(CategoriaAlimento.PREPARADO);
        lote.setQuantidadeKg(quantidade);
        lote.setPrazoAproveitamento(prazo);
        return lote;
    }
}
