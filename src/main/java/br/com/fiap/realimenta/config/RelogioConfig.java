package br.com.fiap.realimenta.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelogioConfig {

    /**
     * Fuso civil da aplicacao. O Brasil nao usa horario de verao desde 2019,
     * entao America/Sao_Paulo permanece em UTC-3.
     */
    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Bean
    public Clock clock() {
        return Clock.system(FUSO);
    }
}
