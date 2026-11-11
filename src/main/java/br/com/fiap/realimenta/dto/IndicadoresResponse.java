package br.com.fiap.realimenta.dto;

import br.com.fiap.realimenta.model.Indicadores;
import java.math.BigDecimal;

public class IndicadoresResponse {

    private BigDecimal kgOfertados;
    private BigDecimal kgEntregues;
    private long doacoesEntregues;
    private long instituicoesComEntrega;

    public static IndicadoresResponse de(Indicadores origem) {
        IndicadoresResponse resposta = new IndicadoresResponse();
        resposta.kgOfertados = origem.getKgOfertados();
        resposta.kgEntregues = origem.getKgEntregues();
        resposta.doacoesEntregues = origem.getDoacoesEntregues();
        resposta.instituicoesComEntrega = origem.getInstituicoesComEntrega();
        return resposta;
    }

    public BigDecimal getKgOfertados() {
        return kgOfertados;
    }

    public BigDecimal getKgEntregues() {
        return kgEntregues;
    }

    public long getDoacoesEntregues() {
        return doacoesEntregues;
    }

    public long getInstituicoesComEntrega() {
        return instituicoesComEntrega;
    }
}
