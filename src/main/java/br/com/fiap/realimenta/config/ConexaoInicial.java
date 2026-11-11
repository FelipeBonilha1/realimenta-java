package br.com.fiap.realimenta.config;

import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Abre uma conexao ao terminar a subida.
 * Se o Oracle estiver inacessivel, a aplicacao encerra em vez de parecer saudavel.
 */
@Component
public class ConexaoInicial implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ConexaoInicial.class);

    private final DataSource dataSource;

    public ConexaoInicial(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection conexao = dataSource.getConnection()) {
            if (!conexao.isValid(5)) {
                throw new IllegalStateException("A conexão com o Oracle não ficou válida.");
            }
        }
        LOG.info("Conexao com o Oracle confirmada na inicializacao.");
    }
}
