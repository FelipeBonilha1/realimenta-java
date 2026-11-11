package br.com.fiap.realimenta;

import br.com.fiap.realimenta.config.RelogioConfig;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RealimentaApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(RelogioConfig.FUSO));
        SpringApplication.run(RealimentaApplication.class, args);
    }
}
