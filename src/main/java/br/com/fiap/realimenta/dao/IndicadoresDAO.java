package br.com.fiap.realimenta.dao;

import br.com.fiap.realimenta.model.Indicadores;
import br.com.fiap.realimenta.model.StatusDoacao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IndicadoresDAO {

    private final JdbcTemplate jdbcTemplate;

    public IndicadoresDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Kg ofertados somam todos os lotes cadastrados, inclusive cancelados.
     * Kg entregues, doacoes e instituicoes consideram somente STATUS ENTREGUE.
     * Reserva e coleta nao entram como entrega.
     */
    public Indicadores consultar() {
        String sql = """
                SELECT
                    (SELECT COALESCE(SUM(QUANTIDADE_KG), 0) FROM LOTE_ALIMENTO) AS KG_OFERTADOS,
                    (SELECT COALESCE(SUM(QUANTIDADE_ENTREGUE_KG), 0) FROM DOACAO WHERE STATUS = ?) AS KG_ENTREGUES,
                    (SELECT COUNT(*) FROM DOACAO WHERE STATUS = ?) AS DOACOES_ENTREGUES,
                    (SELECT COUNT(DISTINCT INSTITUICAO_ID) FROM DOACAO WHERE STATUS = ?) AS INSTITUICOES_COM_ENTREGA
                FROM DUAL
                """;
        String entregue = StatusDoacao.ENTREGUE.name();
        return jdbcTemplate.queryForObject(sql, this::mapear, entregue, entregue, entregue);
    }

    private Indicadores mapear(ResultSet rs, int rowNum) throws SQLException {
        Indicadores indicadores = new Indicadores();
        indicadores.setKgOfertados(quilos(rs, "KG_OFERTADOS"));
        indicadores.setKgEntregues(quilos(rs, "KG_ENTREGUES"));
        indicadores.setDoacoesEntregues(rs.getLong("DOACOES_ENTREGUES"));
        indicadores.setInstituicoesComEntrega(rs.getLong("INSTITUICOES_COM_ENTREGA"));
        return indicadores;
    }

    private BigDecimal quilos(ResultSet rs, String coluna) throws SQLException {
        BigDecimal valor = rs.getBigDecimal(coluna);
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }
        return valor.setScale(3, RoundingMode.HALF_UP);
    }
}
