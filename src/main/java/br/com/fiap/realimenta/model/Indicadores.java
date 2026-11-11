package br.com.fiap.realimenta.model;

import java.math.BigDecimal;

/**
 * Leitura dos quatro indicadores pedidos.
 * Quilos ofertados somam os registros de oferta e nao equivalem a quilos entregues.
 */
public class Indicadores {

    private BigDecimal kgOfertados;
    private BigDecimal kgEntregues;
    private long doacoesEntregues;
    private long instituicoesComEntrega;

    public BigDecimal getKgOfertados() {
        return kgOfertados;
    }

    public void setKgOfertados(BigDecimal kgOfertados) {
        this.kgOfertados = kgOfertados;
    }

    public BigDecimal getKgEntregues() {
        return kgEntregues;
    }

    public void setKgEntregues(BigDecimal kgEntregues) {
        this.kgEntregues = kgEntregues;
    }

    public long getDoacoesEntregues() {
        return doacoesEntregues;
    }

    public void setDoacoesEntregues(long doacoesEntregues) {
        this.doacoesEntregues = doacoesEntregues;
    }

    public long getInstituicoesComEntrega() {
        return instituicoesComEntrega;
    }

    public void setInstituicoesComEntrega(long instituicoesComEntrega) {
        this.instituicoesComEntrega = instituicoesComEntrega;
    }
}
