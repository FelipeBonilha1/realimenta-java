package br.com.fiap.realimenta.dao;

import br.com.fiap.realimenta.model.Estabelecimento;
import br.com.fiap.realimenta.model.TipoEstabelecimento;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class EstabelecimentoDAO {

    private static final String COLUNAS = """
            ID, NOME, TIPO, EMAIL, TELEFONE, ENDERECO, BAIRRO, CIDADE
            """;

    private final JdbcTemplate jdbcTemplate;

    public EstabelecimentoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Estabelecimento> listar() {
        String sql = "SELECT " + COLUNAS + " FROM ESTABELECIMENTO ORDER BY ID";
        return jdbcTemplate.query(sql, MAPEADOR);
    }

    public Optional<Estabelecimento> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM ESTABELECIMENTO WHERE ID = ?";
        List<Estabelecimento> linhas = jdbcTemplate.query(sql, MAPEADOR, id);
        return primeira(linhas);
    }

    public Optional<Estabelecimento> buscarPorEmail(String email) {
        String sql = "SELECT " + COLUNAS + " FROM ESTABELECIMENTO WHERE EMAIL = ?";
        List<Estabelecimento> linhas = jdbcTemplate.query(sql, MAPEADOR, email);
        return primeira(linhas);
    }

    public void inserir(Estabelecimento estabelecimento) {
        Long id = jdbcTemplate.queryForObject("SELECT SEQ_ESTABELECIMENTO.NEXTVAL FROM DUAL", Long.class);
        estabelecimento.setId(id);
        String sql = """
                INSERT INTO ESTABELECIMENTO
                    (ID, NOME, TIPO, EMAIL, TELEFONE, ENDERECO, BAIRRO, CIDADE)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                id,
                estabelecimento.getNome(),
                estabelecimento.getTipo().name(),
                estabelecimento.getEmail(),
                estabelecimento.getTelefone(),
                estabelecimento.getEndereco(),
                estabelecimento.getBairro(),
                estabelecimento.getCidade());
    }

    public int atualizar(Estabelecimento estabelecimento) {
        String sql = """
                UPDATE ESTABELECIMENTO
                   SET NOME = ?, TIPO = ?, EMAIL = ?, TELEFONE = ?, ENDERECO = ?, BAIRRO = ?, CIDADE = ?
                 WHERE ID = ?
                """;
        return jdbcTemplate.update(
                sql,
                estabelecimento.getNome(),
                estabelecimento.getTipo().name(),
                estabelecimento.getEmail(),
                estabelecimento.getTelefone(),
                estabelecimento.getEndereco(),
                estabelecimento.getBairro(),
                estabelecimento.getCidade(),
                estabelecimento.getId());
    }

    public int excluir(Long id) {
        return jdbcTemplate.update("DELETE FROM ESTABELECIMENTO WHERE ID = ?", id);
    }

    private Optional<Estabelecimento> primeira(List<Estabelecimento> linhas) {
        if (linhas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(linhas.get(0));
    }

    private static final RowMapper<Estabelecimento> MAPEADOR = new RowMapper<>() {
        @Override
        public Estabelecimento mapRow(ResultSet rs, int rowNum) throws SQLException {
            Estabelecimento estabelecimento = new Estabelecimento();
            estabelecimento.setId(rs.getLong("ID"));
            estabelecimento.setNome(rs.getString("NOME"));
            estabelecimento.setTipo(TipoEstabelecimento.valueOf(rs.getString("TIPO")));
            estabelecimento.setEmail(rs.getString("EMAIL"));
            estabelecimento.setTelefone(rs.getString("TELEFONE"));
            estabelecimento.setEndereco(rs.getString("ENDERECO"));
            estabelecimento.setBairro(rs.getString("BAIRRO"));
            estabelecimento.setCidade(rs.getString("CIDADE"));
            return estabelecimento;
        }
    };
}
