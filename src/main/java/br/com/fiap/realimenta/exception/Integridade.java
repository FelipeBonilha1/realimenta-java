package br.com.fiap.realimenta.exception;

import java.sql.SQLException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Traduz a falha de integridade do Oracle para uma mensagem de negocio,
 * sem devolver SQL, nome de constraint ou credencial.
 */
public final class Integridade {

    private static final int ORA_CHAVE_UNICA = 1;

    private Integridade() {
    }

    public static ConflitoNegocioException conflito(DataIntegrityViolationException ex, String mensagemUnicidade) {
        Throwable causa = ex.getMostSpecificCause();
        if (causa instanceof SQLException sql && sql.getErrorCode() == ORA_CHAVE_UNICA) {
            return new ConflitoNegocioException(mensagemUnicidade);
        }
        return new ConflitoNegocioException("A operação viola uma restrição de integridade dos dados.");
    }
}
