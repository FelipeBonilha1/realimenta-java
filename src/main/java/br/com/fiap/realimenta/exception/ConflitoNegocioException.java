package br.com.fiap.realimenta.exception;

public class ConflitoNegocioException extends RuntimeException {

    public ConflitoNegocioException(String mensagem) {
        super(mensagem);
    }
}
