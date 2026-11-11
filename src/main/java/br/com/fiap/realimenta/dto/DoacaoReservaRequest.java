package br.com.fiap.realimenta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DoacaoReservaRequest {

    @NotNull(message = "O lote é obrigatório.")
    @Positive(message = "O lote informado é inválido.")
    private Long loteId;

    @NotNull(message = "A instituição é obrigatória.")
    @Positive(message = "A instituição informada é inválida.")
    private Long instituicaoId;

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
}
