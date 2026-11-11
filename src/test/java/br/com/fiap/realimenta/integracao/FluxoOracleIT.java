package br.com.fiap.realimenta.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.fiap.realimenta.bo.DoacaoBO;
import br.com.fiap.realimenta.bo.EstabelecimentoBO;
import br.com.fiap.realimenta.bo.InstituicaoBO;
import br.com.fiap.realimenta.bo.LoteAlimentoBO;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.model.CategoriaAlimento;
import br.com.fiap.realimenta.model.Estabelecimento;
import br.com.fiap.realimenta.model.Instituicao;
import br.com.fiap.realimenta.model.LoteAlimento;
import br.com.fiap.realimenta.model.StatusLote;
import br.com.fiap.realimenta.model.TipoEstabelecimento;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integracao real com Oracle, separada de mvnw test.
 * So sobe se DB_IT_SCHEMA_EXCLUSIVO=true. Nao usa H2.
 * O schema precisa ser exclusivo de teste e ja ter 01_estrutura.sql aplicado.
 * Este arquivo nao executa script destrutivo no schema do grupo.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_IT_SCHEMA_EXCLUSIVO", matches = "true")
class FluxoOracleIT {

    private static final String GATILHO = "TR_REALIMENTA_IT_FALHA";

    @Autowired
    private EstabelecimentoBO estabelecimentoBO;

    @Autowired
    private InstituicaoBO instituicaoBO;

    @Autowired
    private LoteAlimentoBO loteAlimentoBO;

    @Autowired
    private DoacaoBO doacaoBO;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DataSource dataSource;

    private final List<Long> estabelecimentos = new ArrayList<>();
    private final List<Long> instituicoes = new ArrayList<>();
    private final List<Long> lotes = new ArrayList<>();

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", () -> System.getenv("DB_IT_URL"));
        registry.add("DB_USERNAME", () -> System.getenv("DB_IT_USERNAME"));
        registry.add("DB_PASSWORD", () -> System.getenv("DB_IT_PASSWORD"));
        registry.add("spring.datasource.url", () -> System.getenv("DB_IT_URL"));
        registry.add("spring.datasource.username", () -> System.getenv("DB_IT_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv("DB_IT_PASSWORD"));
    }

    @BeforeEach
    void recusarSchemaCompartilhado() {
        String usuarioTeste = System.getenv("DB_IT_USERNAME");
        String usuarioAplicacao = System.getenv("DB_USERNAME");
        if (usuarioAplicacao != null && usuarioAplicacao.equalsIgnoreCase(usuarioTeste)) {
            throw new IllegalStateException(
                    "DB_IT_USERNAME nao pode ser o usuario do schema compartilhado do grupo.");
        }
    }

    @AfterEach
    void limparSomenteAsLinhasDesteTeste() {
        jdbc.execute("BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER " + GATILHO + "'; EXCEPTION WHEN OTHERS THEN NULL; END;");
        for (Long loteId : lotes) {
            jdbc.update("DELETE FROM DOACAO WHERE LOTE_ID = ?", loteId);
            jdbc.update("DELETE FROM LOTE_ALIMENTO WHERE ID = ?", loteId);
        }
        for (Long instituicaoId : instituicoes) {
            jdbc.update("DELETE FROM INSTITUICAO WHERE ID = ?", instituicaoId);
        }
        for (Long estabelecimentoId : estabelecimentos) {
            jdbc.update("DELETE FROM ESTABELECIMENTO WHERE ID = ?", estabelecimentoId);
        }
        lotes.clear();
        instituicoes.clear();
        estabelecimentos.clear();
    }

    @Test
    void duasReservasSimultaneasDeixamUmaUnicaDoacao() throws Exception {
        long estabelecimentoId = novoEstabelecimento();
        long instituicaoId = novaInstituicao();
        long loteId = novoLote(estabelecimentoId, "IT_CONCORRENCIA_RESERVA", new BigDecimal("4.000"));
        CountDownLatch prontos = new CountDownLatch(2);
        CountDownLatch vai = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Object> primeira = executor.submit(() -> reservar(loteId, instituicaoId, prontos, vai));
            Future<Object> segunda = executor.submit(() -> reservar(loteId, instituicaoId, prontos, vai));
            assertTrue(prontos.await(10, TimeUnit.SECONDS));
            vai.countDown();
            Object resultadoA = primeira.get(30, TimeUnit.SECONDS);
            Object resultadoB = segunda.get(30, TimeUnit.SECONDS);
            int sucessos = contar(resultadoA, resultadoB, true);
            int conflitos = contar(resultadoA, resultadoB, false);
            assertEquals(1, sucessos);
            assertEquals(1, conflitos);
        } finally {
            executor.shutdownNow();
        }

        Integer doacoes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM DOACAO WHERE LOTE_ID = ?", Integer.class, loteId);
        String status = jdbc.queryForObject(
                "SELECT STATUS FROM LOTE_ALIMENTO WHERE ID = ?", String.class, loteId);
        assertEquals(1, doacoes);
        assertEquals(StatusLote.RESERVADO.name(), status);
    }

    @Test
    void edicaoEsperaATravaENaoAlteraQuantidadeDeLoteReservado() throws Exception {
        long estabelecimentoId = novoEstabelecimento();
        long loteId = novoLote(estabelecimentoId, "IT_CONCORRENCIA_EDICAO", new BigDecimal("5.000"));
        CountDownLatch edicaoTerminada = new CountDownLatch(1);
        AtomicReference<Throwable> falha = new AtomicReference<>();

        try (Connection conexao = dataSource.getConnection()) {
            conexao.setAutoCommit(false);
            try (PreparedStatement consulta = conexao.prepareStatement(
                    "SELECT ID FROM LOTE_ALIMENTO WHERE ID = ? FOR UPDATE")) {
                consulta.setLong(1, loteId);
                try (ResultSet linhas = consulta.executeQuery()) {
                    assertTrue(linhas.next());
                }
            }

            Thread edicao = new Thread(() -> {
                try {
                    LoteAlimento dados = new LoteAlimento();
                    dados.setEstabelecimentoId(estabelecimentoId);
                    dados.setDescricao("IT_CONCORRENCIA_EDICAO");
                    dados.setCategoria(CategoriaAlimento.PREPARADO);
                    dados.setQuantidadeKg(new BigDecimal("9.000"));
                    dados.setPrazoAproveitamento(LocalDateTime.now().plusDays(2));
                    loteAlimentoBO.atualizar(loteId, dados);
                } catch (Throwable ex) {
                    falha.set(ex);
                } finally {
                    edicaoTerminada.countDown();
                }
            });
            edicao.start();
            Thread.sleep(500);
            try (PreparedStatement atualizacao = conexao.prepareStatement(
                    "UPDATE LOTE_ALIMENTO SET STATUS = 'RESERVADO' WHERE ID = ? AND STATUS = 'DISPONIVEL'")) {
                atualizacao.setLong(1, loteId);
                assertEquals(1, atualizacao.executeUpdate());
            }
            conexao.commit();
            assertTrue(edicaoTerminada.await(20, TimeUnit.SECONDS));
        }

        assertTrue(falha.get() instanceof ConflitoNegocioException);
        BigDecimal quantidade = jdbc.queryForObject(
                "SELECT QUANTIDADE_KG FROM LOTE_ALIMENTO WHERE ID = ?", BigDecimal.class, loteId);
        String status = jdbc.queryForObject(
                "SELECT STATUS FROM LOTE_ALIMENTO WHERE ID = ?", String.class, loteId);
        assertEquals(0, new BigDecimal("5.000").compareTo(quantidade));
        assertEquals(StatusLote.RESERVADO.name(), status);
    }

    @Test
    void falhaEntreDoacaoELoteDesfazAsDuasGravacoes() {
        long estabelecimentoId = novoEstabelecimento();
        long instituicaoId = novaInstituicao();
        long loteId = novoLote(estabelecimentoId, "IT_ROLLBACK", new BigDecimal("3.000"));
        jdbc.execute("""
                CREATE OR REPLACE TRIGGER TR_REALIMENTA_IT_FALHA
                BEFORE UPDATE OF STATUS ON LOTE_ALIMENTO
                FOR EACH ROW
                BEGIN
                  IF :NEW.STATUS = 'RESERVADO' AND :OLD.DESCRICAO = 'IT_ROLLBACK' THEN
                    RAISE_APPLICATION_ERROR(-20001, 'falha entre gravacoes');
                  END IF;
                END;
                """);
        try {
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> doacaoBO.reservar(loteId, instituicaoId));
        } finally {
            jdbc.execute("DROP TRIGGER " + GATILHO);
        }

        Integer doacoes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM DOACAO WHERE LOTE_ID = ?", Integer.class, loteId);
        String status = jdbc.queryForObject(
                "SELECT STATUS FROM LOTE_ALIMENTO WHERE ID = ?", String.class, loteId);
        assertEquals(0, doacoes);
        assertEquals(StatusLote.DISPONIVEL.name(), status);
    }

    private Object reservar(long loteId, long instituicaoId, CountDownLatch prontos, CountDownLatch vai)
            throws InterruptedException {
        prontos.countDown();
        vai.await(10, TimeUnit.SECONDS);
        try {
            return doacaoBO.reservar(loteId, instituicaoId);
        } catch (ConflitoNegocioException ex) {
            return ex;
        }
    }

    private int contar(Object primeiro, Object segundo, boolean sucesso) {
        int total = 0;
        if (sucesso == !(primeiro instanceof ConflitoNegocioException)) {
            total++;
        }
        if (sucesso == !(segundo instanceof ConflitoNegocioException)) {
            total++;
        }
        return total;
    }

    private long novoEstabelecimento() {
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setNome("IT " + UUID.randomUUID());
        estabelecimento.setTipo(TipoEstabelecimento.OUTRO);
        estabelecimento.setEmail("it-" + UUID.randomUUID() + "@example.com");
        estabelecimento.setTelefone("11900000000");
        estabelecimento.setEndereco("Rua do teste");
        estabelecimento.setBairro("Teste");
        estabelecimento.setCidade("São Paulo");
        long id = estabelecimentoBO.criar(estabelecimento).getId();
        estabelecimentos.add(id);
        return id;
    }

    private long novaInstituicao() {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("IT " + UUID.randomUUID());
        instituicao.setEmail("it-" + UUID.randomUUID() + "@example.com");
        instituicao.setTelefone("11900000001");
        instituicao.setEndereco("Rua do teste");
        instituicao.setBairro("Teste");
        instituicao.setCidade("São Paulo");
        long id = instituicaoBO.criar(instituicao).getId();
        instituicoes.add(id);
        return id;
    }

    private long novoLote(long estabelecimentoId, String descricao, BigDecimal quantidade) {
        LoteAlimento lote = new LoteAlimento();
        lote.setEstabelecimentoId(estabelecimentoId);
        lote.setDescricao(descricao);
        lote.setCategoria(CategoriaAlimento.PREPARADO);
        lote.setQuantidadeKg(quantidade);
        lote.setPrazoAproveitamento(LocalDateTime.now().plusDays(2));
        long id = loteAlimentoBO.criar(lote).getId();
        lotes.add(id);
        return id;
    }
}
