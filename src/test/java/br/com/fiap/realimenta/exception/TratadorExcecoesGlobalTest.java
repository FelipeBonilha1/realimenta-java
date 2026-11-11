package br.com.fiap.realimenta.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.fiap.realimenta.dto.ErroResponse;
import br.com.fiap.realimenta.suporte.RelogioFixo;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TratadorExcecoesGlobalTest {

    private TratadorExcecoesGlobal tratador;
    private HttpServletRequest request;

    @BeforeEach
    void preparar() {
        tratador = new TratadorExcecoesGlobal(RelogioFixo.CLOCK);
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/lotes/1");
    }

    @Test
    void traduzRegrasParaOsStatusCombinados() {
        ResponseEntity<ErroResponse> naoEncontrado = tratador.tratarNaoEncontrado(
                new RecursoNaoEncontradoException("Lote não encontrado."), request);
        ResponseEntity<ErroResponse> conflito = tratador.tratarConflito(
                new ConflitoNegocioException("O lote já possui reserva."), request);
        ResponseEntity<ErroResponse> invalido = tratador.tratarDadosInvalidos(
                new DadosInvalidosException("A quantidade ofertada deve ser maior que zero."), request);

        assertEquals(HttpStatus.NOT_FOUND, naoEncontrado.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, conflito.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, invalido.getStatusCode());
        assertEquals(OffsetDateTime.of(RelogioFixo.AGORA, ZoneOffset.ofHours(-3)), invalido.getBody().getInstante());
        assertEquals("/api/lotes/1", invalido.getBody().getCaminho());
        assertTrue(invalido.getBody().getErros().isEmpty());
    }

    @Test
    void falhaInesperadaNaoExpoeSqlNemCausa() {
        ResponseEntity<ErroResponse> resposta = tratador.tratarInesperado(
                new RuntimeException("ORA-00942 SELECT SENHA FROM USUARIO"), request);

        assertEquals(500, resposta.getBody().getStatus());
        assertFalse(resposta.getBody().getMensagem().contains("ORA"));
        assertFalse(resposta.getBody().getMensagem().toLowerCase().contains("select"));
        assertFalse(resposta.getBody().getMensagem().toLowerCase().contains("senha"));
    }

    @Test
    void violacaoDeIntegridadeNaoDevolveDetalheDoBanco() {
        ResponseEntity<ErroResponse> resposta = tratador.tratarIntegridade(
                new DataIntegrityViolationException("ORA-00001 UK_DOACAO_LOTE"), request);

        assertEquals(409, resposta.getBody().getStatus());
        assertFalse(resposta.getBody().getMensagem().contains("ORA"));
        assertFalse(resposta.getBody().getMensagem().contains("UK_DOACAO_LOTE"));
    }
}
