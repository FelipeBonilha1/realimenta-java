package br.com.fiap.realimenta.dao;

import br.com.fiap.realimenta.model.Doacao;
import br.com.fiap.realimenta.model.StatusDoacao;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DoacaoDAO {

    private static final String COLUNAS = """
            ID, LOTE_ID, INSTITUICAO_ID, STATUS, DATA_RESERVA, DATA_COLETA, DATA_ENTREGA, QUANTIDADE_ENTREGUE_KG
            """;

    private final JdbcTemplate jdbcTemplate;

    public DoacaoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Doacao> listar() {
        String sql = "SELECT " + COLUNAS + " FROM DOACAO ORDER BY ID";
        return jdbcTemplate.query(sql, MAPEADOR);
    }

    public Optional<Doacao> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM DOACAO WHERE ID = ?";
        return primeira(jdbcTemplate.query(sql, MAPEADOR, id));
    }

    public Optional<Doacao> buscarPorIdParaAtualizacao(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM DOACAO WHERE ID = ? FOR UPDATE";
        return primeira(jdbcTemplate.query(sql, MAPEADOR, id));
    }

    public Optional<Doacao> buscarPorLoteId(Long loteId) {
        String sql = "SELECT " + COLUNAS + " FROM DOACAO WHERE LOTE_ID = ?";
        return primeira(jdbcTemplate.query(sql, MAPEADOR, loteId));
    }

    public boolean existePorInstituicao(Long instituicaoId) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DOACAO WHERE INSTITUICAO_ID = ?",
                Long.class,
                instituicaoId);
        return total != null && total > 0;
    }

    public void inserir(Doacao doacao) {
        Long id = jdbcTemplate.queryForObject("SELECT SEQ_DOACAO.NEXTVAL FROM DUAL", Long.class);
        doacao.setId(id);
        String sql = """
                INSERT INTO DOACAO
                    (ID, LOTE_ID, INSTITUICAO_ID, STATUS, DATA_RESERVA, DATA_COLETA, DATA_ENTREGA, QUANTIDADE_ENTREGUE_KG)
                VALUES (?, ?, ?, ?, ?, NULL, NULL, NULL)
                """;
        jdbcTemplate.update(
                sql,
                id,
                doacao.getLoteId(),
                doacao.getInstituicaoId(),
                doacao.getStatus().name(),
                DatasJdbc.paraTimestamp(doacao.getDataReserva()));
    }

    public int registrarColeta(Long id, LocalDateTime dataColeta) {
        String sql = """
                UPDATE DOACAO
                   SET STATUS = ?, DATA_COLETA = ?
                 WHERE ID = ?
                   AND STATUS = ?
                """;
        return jdbcTemplate.update(
                sql,
                StatusDoacao.COLETADA.name(),
                DatasJdbc.paraTimestamp(dataColeta),
                id,
                StatusDoacao.RESERVADA.name());
    }

    public int registrarEntrega(Long id, LocalDateTime dataEntrega, BigDecimal quantidadeEntregueKg) {
        String sql = """
                UPDATE DOACAO
                   SET STATUS = ?, DATA_ENTREGA = ?, QUANTIDADE_ENTREGUE_KG = ?
                 WHERE ID = ?
                   AND STATUS = ?
                """;
        return jdbcTemplate.update(
                sql,
                StatusDoacao.ENTREGUE.name(),
                DatasJdbc.paraTimestamp(dataEntrega),
                quantidadeEntregueKg,
                id,
                StatusDoacao.COLETADA.name());
    }

    public int cancelar(Long id) {
        String sql = """
                UPDATE DOACAO
                   SET STATUS = ?
                 WHERE ID = ?
                   AND STATUS = ?
                """;
        return jdbcTemplate.update(sql, StatusDoacao.CANCELADA.name(), id, StatusDoacao.RESERVADA.name());
    }

    private Optional<Doacao> primeira(List<Doacao> linhas) {
        if (linhas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(linhas.get(0));
    }

    private static final RowMapper<Doacao> MAPEADOR = new RowMapper<>() {
        @Override
        public Doacao mapRow(ResultSet rs, int rowNum) throws SQLException {
            Doacao doacao = new Doacao();
            doacao.setId(rs.getLong("ID"));
            doacao.setLoteId(rs.getLong("LOTE_ID"));
            doacao.setInstituicaoId(rs.getLong("INSTITUICAO_ID"));
            doacao.setStatus(StatusDoacao.valueOf(rs.getString("STATUS")));
            doacao.setDataReserva(DatasJdbc.paraLocalDateTime(rs, "DATA_RESERVA"));
            doacao.setDataColeta(DatasJdbc.paraLocalDateTime(rs, "DATA_COLETA"));
            doacao.setDataEntrega(DatasJdbc.paraLocalDateTime(rs, "DATA_ENTREGA"));
            doacao.setQuantidadeEntregueKg(rs.getBigDecimal("QUANTIDADE_ENTREGUE_KG"));
            return doacao;
        }
    };
}
