package br.com.fiap.realimenta.suporte;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class RelogioFixo {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 11, 12, 0);
    public static final Clock CLOCK = Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);

    private RelogioFixo() {
    }
}
