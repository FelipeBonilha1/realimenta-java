package br.com.fiap.realimenta.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class DoacaoEntregaRequest {

    @NotNull(message = "A quantidade entregue é obrigatória.")
    @Positive(message = "A quantidade entregue deve ser maior que zero.")
    @Digits(integer = 7, fraction = 3, message = "A quantidade deve ter no máximo 7 dígitos inteiros e 3 decimais.")
    private BigDecimal quantidadeEntregueKg;

    public BigDecimal getQuantidadeEntregueKg() {
        return quantidadeEntregueKg;
    }

    public void setQuantidadeEntregueKg(BigDecimal quantidadeEntregueKg) {
        this.quantidadeEntregueKg = quantidadeEntregueKg;
    }
}
