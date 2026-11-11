package br.com.fiap.realimenta.dao;

import br.com.fiap.realimenta.model.Instituicao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class InstituicaoDAO {

    private static final String COLUNAS = """
            ID, NOME, EMAIL, TELEFONE, ENDERECO, BAIRRO, CIDADE
            """;

    private final JdbcTemplate jdbcTemplate;

    public InstituicaoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Instituicao> listar() {
        String sql = "SELECT " + COLUNAS + " FROM INSTITUICAO ORDER BY ID";
        return jdbcTemplate.query(sql, MAPEADOR);
    }

    public Optional<Instituicao> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM INSTITUICAO WHERE ID = ?";
        return primeira(jdbcTemplate.query(sql, MAPEADOR, id));
    }

    public Optional<Instituicao> buscarPorEmail(String email) {
        String sql = "SELECT " + COLUNAS + " FROM INSTITUICAO WHERE EMAIL = ?";
        return primeira(jdbcTemplate.query(sql, MAPEADOR, email));
    }

    public void inserir(Instituicao instituicao) {
        Long id = jdbcTemplate.queryForObject("SELECT SEQ_INSTITUICAO.NEXTVAL FROM DUAL", Long.class);
        instituicao.setId(id);
        String sql = """
                INSERT INTO INSTITUICAO
                    (ID, NOME, EMAIL, TELEFONE, ENDERECO, BAIRRO, CIDADE)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                id,
                instituicao.getNome(),
                instituicao.getEmail(),
                instituicao.getTelefone(),
                instituicao.getEndereco(),
                instituicao.getBairro(),
                instituicao.getCidade());
    }

    public int atualizar(Instituicao instituicao) {
        String sql = """
                UPDATE INSTITUICAO
                   SET NOME = ?, EMAIL = ?, TELEFONE = ?, ENDERECO = ?, BAIRRO = ?, CIDADE = ?
                 WHERE ID = ?
                """;
        return jdbcTemplate.update(
                sql,
                instituicao.getNome(),
                instituicao.getEmail(),
                instituicao.getTelefone(),
                instituicao.getEndereco(),
                instituicao.getBairro(),
                instituicao.getCidade(),
                instituicao.getId());
    }

    public int excluir(Long id) {
        return jdbcTemplate.update("DELETE FROM INSTITUICAO WHERE ID = ?", id);
    }

    private Optional<Instituicao> primeira(List<Instituicao> linhas) {
        if (linhas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(linhas.get(0));
    }

    private static final RowMapper<Instituicao> MAPEADOR = new RowMapper<>() {
        @Override
        public Instituicao mapRow(ResultSet rs, int rowNum) throws SQLException {
            Instituicao instituicao = new Instituicao();
            instituicao.setId(rs.getLong("ID"));
            instituicao.setNome(rs.getString("NOME"));
            instituicao.setEmail(rs.getString("EMAIL"));
            instituicao.setTelefone(rs.getString("TELEFONE"));
            instituicao.setEndereco(rs.getString("ENDERECO"));
            instituicao.setBairro(rs.getString("BAIRRO"));
            instituicao.setCidade(rs.getString("CIDADE"));
            return instituicao;
        }
    };
}
