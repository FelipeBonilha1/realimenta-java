package br.com.fiap.realimenta.exception;

import br.com.fiap.realimenta.dto.ErroCampo;
import br.com.fiap.realimenta.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class TratadorExcecoesGlobal {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorExcecoesGlobal.class);

    private final Clock clock;

    public TratadorExcecoesGlobal(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                            HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException ex,
                                                              HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado.", request, List.of());
    }

    @ExceptionHandler(ConflitoNegocioException.class)
    public ResponseEntity<ErroResponse> tratarConflito(ConflitoNegocioException ex, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarIntegridade(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        LOG.warn("Violacao de integridade em {}", request.getRequestURI());
        return resposta(
                HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados.",
                request,
                List.of());
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResponse> tratarDadosInvalidos(DadosInvalidosException ex, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        List<ErroCampo> erros = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                erros.add(new ErroCampo(erro.getField(), erro.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors().forEach(erro ->
                erros.add(new ErroCampo(erro.getObjectName(), erro.getDefaultMessage())));
        return resposta(HttpStatus.BAD_REQUEST, "Existem campos inválidos.", request, erros);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return resposta(
                HttpStatus.BAD_REQUEST,
                "O JSON enviado é inválido ou contém um valor não reconhecido.",
                request,
                List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarIdentificador(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "O identificador informado é inválido.", request, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMidia(HttpMediaTypeNotSupportedException ex,
                                                    HttpServletRequest request) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "O conteúdo enviado precisa estar em JSON.", request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMetodo(HttpRequestMethodNotSupportedException ex,
                                                     HttpServletRequest request) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "O método HTTP não é aceito neste caminho.", request, List.of());
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErroResponse> tratarConsultaVazia(EmptyResultDataAccessException ex,
                                                            HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado.", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarInesperado(Exception ex, HttpServletRequest request) {
        LOG.error("Falha inesperada em {}", request.getRequestURI(), ex);
        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu uma falha inesperada. Tente novamente mais tarde.",
                request,
                List.of());
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem, HttpServletRequest request,
                                                  List<ErroCampo> erros) {
        ErroResponse corpo = new ErroResponse();
        corpo.setStatus(status.value());
        corpo.setMensagem(mensagem);
        corpo.setCaminho(request.getRequestURI());
        corpo.setInstante(OffsetDateTime.now(clock));
        corpo.setErros(erros);
        return ResponseEntity.status(status).body(corpo);
    }
}
