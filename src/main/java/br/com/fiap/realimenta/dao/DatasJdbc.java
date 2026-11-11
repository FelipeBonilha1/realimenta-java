package br.com.fiap.realimenta.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Conversao unica entre LocalDateTime e TIMESTAMP(6).
 * O valor gravado e a hora civil de America/Sao_Paulo, sem deslocar para UTC.
 * O BO ja reduziu o prazo a microssegundos, que e a precisao do TIMESTAMP Oracle.
 */
final class DatasJdbc {

    private DatasJdbc() {
    }

    static Timestamp paraTimestamp(LocalDateTime valor) {
        if (valor == null) {
            return null;
        }
        return Timestamp.valueOf(valor);
    }

    static LocalDateTime paraLocalDateTime(ResultSet rs, String coluna) throws SQLException {
        Timestamp valor = rs.getTimestamp(coluna);
        if (valor == null) {
            return null;
        }
        return valor.toLocalDateTime();
    }
}
