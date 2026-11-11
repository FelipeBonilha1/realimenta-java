package br.com.fiap.realimenta.dao;

import br.com.fiap.realimenta.model.CategoriaAlimento;
import br.com.fiap.realimenta.model.LoteAlimento;
import br.com.fiap.realimenta.model.StatusLote;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class LoteAlimentoDAO {

    private static final String COLUNAS = """
            ID, ESTABELECIMENTO_ID, DESCRICAO, CATEGORIA, QUANTIDADE_KG, PRAZO_APROVEITAMENTO, STATUS
            """;

    private final JdbcTemplate jdbcTemplate;

    public LoteAlimentoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LoteAlimento> listar() {
        String sql = "SELECT " + COLUNAS + " FROM LOTE_ALIMENTO ORDER BY ID";
        return jdbcTemplate.query(sql, MAPEADOR);
    }

    public List<LoteAlimento> listarDisponiveis(LocalDateTime agora) {
        String sql = """
                SELECT %s
                  FROM LOTE_ALIMENTO
                 WHERE STATUS = ?
                   AND PRAZO_APROVEITAMENTO > ?
                 ORDER BY PRAZO_APROVEITAMENTO, ID
                """.formatted(COLUNAS);
        return jdbcTemplate.query(sql, MAPEADOR, StatusLote.DISPONIVEL.name(), DatasJdbc.paraTimestamp(agora));
    }

    public Optional<LoteAlimento> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM LOTE_ALIMENTO WHERE ID = ?";
        return primeira(jdbcTemplate.query(sql, MAPEADOR, id));
    }

    /**
     * Trava a linha ate o fim da transacao aberta pelo BO.
     * Duas reservas concorrentes do mesmo lote esperam uma pela outra.
     */
    public Optional<LoteAlimento> buscarPorIdParaAtualizacao(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM LOTE_ALIMENTO WHERE ID = ? FOR UPDATE";
        return primeira(jdbcTemplate.query(sql, MAPEADOR, id));
    }

    public boolean existePorEstabelecimento(Long estabelecimentoId) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM LOTE_ALIMENTO WHERE ESTABELECIMENTO_ID = ?",
                Long.class,
                estabelecimentoId);
        return total != null && total > 0;
    }

    public void inserir(LoteAlimento lote) {
        Long id = jdbcTemplate.queryForObject("SELECT SEQ_LOTE_ALIMENTO.NEXTVAL FROM DUAL", Long.class);
        lote.setId(id);
        String sql = """
                INSERT INTO LOTE_ALIMENTO
                    (ID, ESTABELECIMENTO_ID, DESCRICAO, CATEGORIA, QUANTIDADE_KG, PRAZO_APROVEITAMENTO, STATUS)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                id,
                lote.getEstabelecimentoId(),
                lote.getDescricao(),
                lote.getCategoria().name(),
                lote.getQuantidadeKg(),
                DatasJdbc.paraTimestamp(lote.getPrazoAproveitamento()),
                lote.getStatus().name());
    }

    /**
     * Nao altera STATUS. Status e datas do fluxo ficam nas operacoes da doacao.
     */
    public int atualizarDados(LoteAlimento lote) {
        String sql = """
                UPDATE LOTE_ALIMENTO
                   SET ESTABELECIMENTO_ID = ?,
                       DESCRICAO = ?,
                       CATEGORIA = ?,
                       QUANTIDADE_KG = ?,
                       PRAZO_APROVEITAMENTO = ?
                 WHERE ID = ?
                """;
        return jdbcTemplate.update(
                sql,
                lote.getEstabelecimentoId(),
                lote.getDescricao(),
                lote.getCategoria().name(),
                lote.getQuantidadeKg(),
                DatasJdbc.paraTimestamp(lote.getPrazoAproveitamento()),
                lote.getId());
    }

    public int atualizarStatus(Long id, StatusLote novoStatus, StatusLote statusEsperado) {
        String sql = "UPDATE LOTE_ALIMENTO SET STATUS = ? WHERE ID = ? AND STATUS = ?";
        return jdbcTemplate.update(sql, novoStatus.name(), id, statusEsperado.name());
    }

    public int excluir(Long id) {
        return jdbcTemplate.update("DELETE FROM LOTE_ALIMENTO WHERE ID = ?", id);
    }

    private Optional<LoteAlimento> primeira(List<LoteAlimento> linhas) {
        if (linhas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(linhas.get(0));
    }

    private static final RowMapper<LoteAlimento> MAPEADOR = new RowMapper<>() {
        @Override
        public LoteAlimento mapRow(ResultSet rs, int rowNum) throws SQLException {
            LoteAlimento lote = new LoteAlimento();
            lote.setId(rs.getLong("ID"));
            lote.setEstabelecimentoId(rs.getLong("ESTABELECIMENTO_ID"));
            lote.setDescricao(rs.getString("DESCRICAO"));
            lote.setCategoria(CategoriaAlimento.valueOf(rs.getString("CATEGORIA")));
            lote.setQuantidadeKg(rs.getBigDecimal("QUANTIDADE_KG"));
            lote.setPrazoAproveitamento(DatasJdbc.paraLocalDateTime(rs, "PRAZO_APROVEITAMENTO"));
            lote.setStatus(StatusLote.valueOf(rs.getString("STATUS")));
            return lote;
        }
    };
}
