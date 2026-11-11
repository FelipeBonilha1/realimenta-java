package br.com.fiap.realimenta.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Doacao {

    private Long id;
    private Long loteId;
    private Long instituicaoId;
    private StatusDoacao status;
    private LocalDateTime dataReserva;
    private LocalDateTime dataColeta;
    private LocalDateTime dataEntrega;
    private BigDecimal quantidadeEntregueKg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Long getInstituicaoId() {
        return instituicaoId;
    }

    public void setInstituicaoId(Long instituicaoId) {
        this.instituicaoId = instituicaoId;
    }

    public StatusDoacao getStatus() {
        return status;
    }

    public void setStatus(StatusDoacao status) {
        this.status = status;
    }

    public LocalDateTime getDataReserva() {
        return dataReserva;
    }

    public void setDataReserva(LocalDateTime dataReserva) {
        this.dataReserva = dataReserva;
    }

    public LocalDateTime getDataColeta() {
        return dataColeta;
    }

    public void setDataColeta(LocalDateTime dataColeta) {
        this.dataColeta = dataColeta;
    }

    public LocalDateTime getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public BigDecimal getQuantidadeEntregueKg() {
        return quantidadeEntregueKg;
    }

    public void setQuantidadeEntregueKg(BigDecimal quantidadeEntregueKg) {
        this.quantidadeEntregueKg = quantidadeEntregueKg;
    }
}
