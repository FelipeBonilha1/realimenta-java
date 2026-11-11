package br.com.fiap.realimenta.bo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.realimenta.dao.DoacaoDAO;
import br.com.fiap.realimenta.dao.InstituicaoDAO;
import br.com.fiap.realimenta.dao.LoteAlimentoDAO;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.model.Doacao;
import br.com.fiap.realimenta.model.Instituicao;
import br.com.fiap.realimenta.model.LoteAlimento;
import br.com.fiap.realimenta.model.StatusDoacao;
import br.com.fiap.realimenta.model.StatusLote;
import br.com.fiap.realimenta.suporte.RelogioFixo;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

class DoacaoBOTest {

    private static final long LOTE_ID = 1L;
    private static final long INSTITUICAO_ID = 2L;
    private static final long DOACAO_ID = 10L;

    private DoacaoDAO doacaoDAO;
    private LoteAlimentoDAO loteAlimentoDAO;
    private DoacaoBO doacaoBO;

    @BeforeEach
    void preparar() {
        doacaoDAO = mock(DoacaoDAO.class);
        loteAlimentoDAO = mock(LoteAlimentoDAO.class);
        InstituicaoDAO instituicaoDAO = mock(InstituicaoDAO.class);
        doacaoBO = new DoacaoBO(doacaoDAO, loteAlimentoDAO, instituicaoDAO, RelogioFixo.CLOCK);
        when(instituicaoDAO.buscarPorId(INSTITUICAO_ID)).thenReturn(Optional.of(new Instituicao()));
    }

    @Test
    void reservaLoteDisponivelDentroDoPrazo() {
        LoteAlimento lote = lote(StatusLote.DISPONIVEL, RelogioFixo.AGORA.plusDays(1));
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(LOTE_ID)).thenReturn(Optional.of(lote));
        when(doacaoDAO.buscarPorLoteId(LOTE_ID)).thenReturn(Optional.empty());
        when(loteAlimentoDAO.atualizarStatus(LOTE_ID, StatusLote.RESERVADO, StatusLote.DISPONIVEL)).thenReturn(1);
        doAnswer(chamada -> {
            Doacao nova = chamada.getArgument(0);
            nova.setId(DOACAO_ID);
            return null;
        }).when(doacaoDAO).inserir(any(Doacao.class));
        when(doacaoDAO.buscarPorId(DOACAO_ID)).thenReturn(Optional.of(doacao(StatusDoacao.RESERVADA)));

        Doacao reservada = doacaoBO.reservar(LOTE_ID, INSTITUICAO_ID);

        assertEquals(StatusDoacao.RESERVADA, reservada.getStatus());
        assertEquals(RelogioFixo.AGORA, reservada.getDataReserva());
        verify(loteAlimentoDAO).atualizarStatus(LOTE_ID, StatusLote.RESERVADO, StatusLote.DISPONIVEL);
        verify(doacaoDAO).inserir(any(Doacao.class));
    }

    @Test
    void impedeReservaDuplicadaAntesDeInserir() {
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(LOTE_ID))
                .thenReturn(Optional.of(lote(StatusLote.DISPONIVEL, RelogioFixo.AGORA.plusHours(2))));
        when(doacaoDAO.buscarPorLoteId(LOTE_ID)).thenReturn(Optional.of(doacao(StatusDoacao.RESERVADA)));

        ConflitoNegocioException erro = assertThrows(
                ConflitoNegocioException.class,
                () -> doacaoBO.reservar(LOTE_ID, INSTITUICAO_ID));

        assertTrue(erro.getMessage().contains("única doação"));
        verify(doacaoDAO, never()).inserir(any());
    }

    @Test
    void transformaViolacaoUnicaDoOracleEmConflitoDeReserva() {
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(LOTE_ID))
                .thenReturn(Optional.of(lote(StatusLote.DISPONIVEL, RelogioFixo.AGORA.plusHours(2))));
        when(doacaoDAO.buscarPorLoteId(LOTE_ID)).thenReturn(Optional.empty());
        SQLException causa = new SQLException("ORA-00001 unique constraint violated", "23000", 1);
        doThrow(new DataIntegrityViolationException("falha de integridade", causa))
                .when(doacaoDAO).inserir(any(Doacao.class));

        ConflitoNegocioException erro = assertThrows(
                ConflitoNegocioException.class,
                () -> doacaoBO.reservar(LOTE_ID, INSTITUICAO_ID));

        assertTrue(erro.getMessage().contains("única doação"));
        assertFalse(erro.getMessage().contains("ORA-00001"));
        verify(loteAlimentoDAO, never()).atualizarStatus(any(), any(), any());
    }

    @Test
    void recusaReservaDeLoteForaDoPrazo() {
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(LOTE_ID))
                .thenReturn(Optional.of(lote(StatusLote.DISPONIVEL, RelogioFixo.AGORA.minusMinutes(1))));

        assertThrows(ConflitoNegocioException.class, () -> doacaoBO.reservar(LOTE_ID, INSTITUICAO_ID));
        verify(doacaoDAO, never()).inserir(any());
    }

    @Test
    void recusaReservaQuandoOPrazoEExatamenteAgora() {
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(LOTE_ID))
                .thenReturn(Optional.of(lote(StatusLote.DISPONIVEL, RelogioFixo.AGORA)));

        assertThrows(ConflitoNegocioException.class, () -> doacaoBO.reservar(LOTE_ID, INSTITUICAO_ID));
    }

    @Test
    void recusaColetaQuandoADoacaoNaoEstaReservada() {
        when(doacaoDAO.buscarPorIdParaAtualizacao(DOACAO_ID))
                .thenReturn(Optional.of(doacao(StatusDoacao.ENTREGUE)));

        ConflitoNegocioException erro = assertThrows(ConflitoNegocioException.class, () -> doacaoBO.coletar(DOACAO_ID));

        assertTrue(erro.getMessage().contains("RESERVADA"));
        verify(doacaoDAO, never()).registrarColeta(any(), any());
        verify(loteAlimentoDAO, never()).atualizarStatus(any(), any(), any());
    }

    @Test
    void recusaColetaDepoisDoPrazo() {
        Doacao reservada = doacao(StatusDoacao.RESERVADA);
        when(doacaoDAO.buscarPorIdParaAtualizacao(DOACAO_ID)).thenReturn(Optional.of(reservada));
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(LOTE_ID))
                .thenReturn(Optional.of(lote(StatusLote.RESERVADO, RelogioFixo.AGORA.minusHours(1))));

        ConflitoNegocioException erro = assertThrows(ConflitoNegocioException.class, () -> doacaoBO.coletar(DOACAO_ID));

        assertTrue(erro.getMessage().contains("prazo"));
        verify(doacaoDAO, never()).registrarColeta(any(), any());
    }

    @Test
    void recusaEntregaSemColeta() {
        when(doacaoDAO.buscarPorIdParaAtualizacao(DOACAO_ID))
                .thenReturn(Optional.of(doacao(StatusDoacao.RESERVADA)));

        ConflitoNegocioException erro = assertThrows(
                ConflitoNegocioException.class,
                () -> doacaoBO.entregar(DOACAO_ID, new BigDecimal("1.000")));

        assertTrue(erro.getMessage().contains("coleta"));
        verify(doacaoDAO, never()).registrarEntrega(any(), any(), any());
    }

    @Test
    void recusaQuantidadeEntregueAcimaDaOfertada() {
        when(doacaoDAO.buscarPorIdParaAtualizacao(DOACAO_ID))
                .thenReturn(Optional.of(doacao(StatusDoacao.COLETADA)));
        LoteAlimento lote = lote(StatusLote.COLETADO, RelogioFixo.AGORA.plusHours(4));
        lote.setQuantidadeKg(new BigDecimal("5.000"));
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(LOTE_ID)).thenReturn(Optional.of(lote));

        ConflitoNegocioException erro = assertThrows(
                ConflitoNegocioException.class,
                () -> doacaoBO.entregar(DOACAO_ID, new BigDecimal("5.001")));

        assertTrue(erro.getMessage().contains("superar"));
        verify(doacaoDAO, never()).registrarEntrega(any(), any(), any());
    }

    @Test
    void recusaEntregaDepoisDoPrazo() {
        when(doacaoDAO.buscarPorIdParaAtualizacao(DOACAO_ID))
                .thenReturn(Optional.of(doacao(StatusDoacao.COLETADA)));
        when(loteAlimentoDAO.buscarPorIdParaAtualizacao(LOTE_ID))
                .thenReturn(Optional.of(lote(StatusLote.COLETADO, RelogioFixo.AGORA)));

        assertThrows(ConflitoNegocioException.class, () -> doacaoBO.entregar(DOACAO_ID, new BigDecimal("1.000")));
        verify(doacaoDAO, never()).registrarEntrega(any(), any(), any());
    }

    @Test
    void recusaCancelamentoDepoisDaColeta() {
        when(doacaoDAO.buscarPorIdParaAtualizacao(DOACAO_ID))
                .thenReturn(Optional.of(doacao(StatusDoacao.COLETADA)));

        ConflitoNegocioException erro = assertThrows(ConflitoNegocioException.class, () -> doacaoBO.cancelar(DOACAO_ID));

        assertTrue(erro.getMessage().contains("antes da coleta"));
        verify(doacaoDAO, never()).cancelar(any());
        verify(loteAlimentoDAO, never()).atualizarStatus(any(), any(), any());
    }

    @Test
    void percorreReservaColetaEEntregaMantendoLoteEDoacaoCoerentes() {
        Memoria memoria = new Memoria();
        memoria.ligar(doacaoDAO, loteAlimentoDAO);

        Doacao reservada = doacaoBO.reservar(LOTE_ID, INSTITUICAO_ID);
        assertEquals(StatusDoacao.RESERVADA, reservada.getStatus());
        assertEquals(StatusLote.RESERVADO, memoria.lote.getStatus());
        assertEquals(RelogioFixo.AGORA, reservada.getDataReserva());

        Doacao coletada = doacaoBO.coletar(DOACAO_ID);
        assertEquals(StatusDoacao.COLETADA, coletada.getStatus());
        assertEquals(StatusLote.COLETADO, memoria.lote.getStatus());
        assertEquals(RelogioFixo.AGORA, coletada.getDataColeta());

        Doacao entregue = doacaoBO.entregar(DOACAO_ID, new BigDecimal("8.5"));
        assertEquals(StatusDoacao.ENTREGUE, entregue.getStatus());
        assertEquals(StatusLote.ENTREGUE, memoria.lote.getStatus());
        assertEquals(0, new BigDecimal("8.500").compareTo(entregue.getQuantidadeEntregueKg()));
        assertNotNull(entregue.getDataEntrega());
    }

    @Test
    void cancelamentoAntesDaColetaEncerraDoacaoELoteSemReabrirOferta() {
        Memoria memoria = new Memoria();
        memoria.ligar(doacaoDAO, loteAlimentoDAO);

        doacaoBO.reservar(LOTE_ID, INSTITUICAO_ID);
        Doacao cancelada = doacaoBO.cancelar(DOACAO_ID);

        assertEquals(StatusDoacao.CANCELADA, cancelada.getStatus());
        assertEquals(StatusLote.CANCELADO, memoria.lote.getStatus());
        assertThrows(ConflitoNegocioException.class, () -> doacaoBO.reservar(LOTE_ID, INSTITUICAO_ID));
        assertThrows(ConflitoNegocioException.class, () -> doacaoBO.coletar(DOACAO_ID));
    }

    @Test
    void fluxoDeclaradoEmTransacaoUnica() throws Exception {
        assertFalse(transacao("reservar", Long.class, Long.class).readOnly());
        assertFalse(transacao("coletar", Long.class).readOnly());
        assertFalse(transacao("entregar", Long.class, BigDecimal.class).readOnly());
        assertFalse(transacao("cancelar", Long.class).readOnly());
    }

    private Transactional transacao(String nome, Class<?>... tipos) throws Exception {
        Transactional anotacao = DoacaoBO.class.getMethod(nome, tipos).getAnnotation(Transactional.class);
        assertNotNull(anotacao, nome);
        return anotacao;
    }

    private LoteAlimento lote(StatusLote status, LocalDateTime prazo) {
        LoteAlimento lote = new LoteAlimento();
        lote.setId(LOTE_ID);
        lote.setEstabelecimentoId(7L);
        lote.setDescricao("Paes do dia");
        lote.setQuantidadeKg(new BigDecimal("8.500"));
        lote.setPrazoAproveitamento(prazo);
        lote.setStatus(status);
        return lote;
    }

    private Doacao doacao(StatusDoacao status) {
        Doacao doacao = new Doacao();
        doacao.setId(DOACAO_ID);
        doacao.setLoteId(LOTE_ID);
        doacao.setInstituicaoId(INSTITUICAO_ID);
        doacao.setStatus(status);
        doacao.setDataReserva(RelogioFixo.AGORA);
        return doacao;
    }

    /**
     * Estado em memoria usado somente para verificar a sequencia das transicoes.
     * Nao substitui o Oracle e nao abre transacao real.
     */
    private final class Memoria {
        private final LoteAlimento lote = lote(StatusLote.DISPONIVEL, RelogioFixo.AGORA.plusDays(1));
        private Doacao doacao;

        private void ligar(DoacaoDAO doacoes, LoteAlimentoDAO lotes) {
            when(lotes.buscarPorIdParaAtualizacao(LOTE_ID)).thenAnswer(chamada -> Optional.of(lote));
            when(lotes.atualizarStatus(eq(LOTE_ID), any(StatusLote.class), any(StatusLote.class)))
                    .thenAnswer(chamada -> {
                        StatusLote novo = chamada.getArgument(1);
                        StatusLote esperado = chamada.getArgument(2);
                        if (lote.getStatus() != esperado) {
                            return 0;
                        }
                        lote.setStatus(novo);
                        return 1;
                    });
            when(doacoes.buscarPorLoteId(LOTE_ID)).thenAnswer(chamada -> Optional.ofNullable(copiar(doacao)));
            doAnswer(chamada -> {
                Doacao nova = chamada.getArgument(0);
                nova.setId(DOACAO_ID);
                doacao = copiar(nova);
                return null;
            }).when(doacoes).inserir(any(Doacao.class));
            when(doacoes.buscarPorId(DOACAO_ID)).thenAnswer(chamada -> Optional.of(copiar(doacao)));
            when(doacoes.buscarPorIdParaAtualizacao(DOACAO_ID)).thenAnswer(chamada -> Optional.of(copiar(doacao)));
            when(doacoes.registrarColeta(eq(DOACAO_ID), any(LocalDateTime.class))).thenAnswer(chamada -> {
                if (doacao.getStatus() != StatusDoacao.RESERVADA) {
                    return 0;
                }
                doacao.setStatus(StatusDoacao.COLETADA);
                doacao.setDataColeta(chamada.getArgument(1));
                return 1;
            });
            when(doacoes.registrarEntrega(eq(DOACAO_ID), any(LocalDateTime.class), any(BigDecimal.class)))
                    .thenAnswer(chamada -> {
                        if (doacao.getStatus() != StatusDoacao.COLETADA) {
                            return 0;
                        }
                        doacao.setStatus(StatusDoacao.ENTREGUE);
                        doacao.setDataEntrega(chamada.getArgument(1));
                        doacao.setQuantidadeEntregueKg(chamada.getArgument(2));
                        return 1;
                    });
            when(doacoes.cancelar(DOACAO_ID)).thenAnswer(chamada -> {
                if (doacao.getStatus() != StatusDoacao.RESERVADA) {
                    return 0;
                }
                doacao.setStatus(StatusDoacao.CANCELADA);
                return 1;
            });
        }
    }

    private Doacao copiar(Doacao origem) {
        if (origem == null) {
            return null;
        }
        Doacao copia = new Doacao();
        copia.setId(origem.getId());
        copia.setLoteId(origem.getLoteId());
        copia.setInstituicaoId(origem.getInstituicaoId());
        copia.setStatus(origem.getStatus());
        copia.setDataReserva(origem.getDataReserva());
        copia.setDataColeta(origem.getDataColeta());
        copia.setDataEntrega(origem.getDataEntrega());
        copia.setQuantidadeEntregueKg(origem.getQuantidadeEntregueKg());
        return copia;
    }
}
