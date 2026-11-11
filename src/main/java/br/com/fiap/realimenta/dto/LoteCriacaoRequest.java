package br.com.fiap.realimenta.dto;

import br.com.fiap.realimenta.model.CategoriaAlimento;
import br.com.fiap.realimenta.model.LoteAlimento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoteCriacaoRequest {

    @NotNull(message = "O estabelecimento é obrigatório.")
    @Positive(message = "O estabelecimento informado é inválido.")
    private Long estabelecimentoId;

    @NotBlank(message = "A descrição é obrigatória.")
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
    private String descricao;

    @NotNull(message = "A categoria é obrigatória.")
    private CategoriaAlimento categoria;

    @NotNull(message = "A quantidade em quilos é obrigatória.")
    @Positive(message = "A quantidade ofertada deve ser maior que zero.")
    @Digits(integer = 7, fraction = 3, message = "A quantidade deve ter no máximo 7 dígitos inteiros e 3 decimais.")
    private BigDecimal quantidadeKg;

    @NotNull(message = "O prazo de aproveitamento é obrigatório.")
    private LocalDateTime prazoAproveitamento;

    public LoteAlimento paraEntidade() {
        LoteAlimento lote = new LoteAlimento();
        lote.setEstabelecimentoId(estabelecimentoId);
        lote.setDescricao(descricao);
        lote.setCategoria(categoria);
        lote.setQuantidadeKg(quantidadeKg);
        lote.setPrazoAproveitamento(prazoAproveitamento);
        return lote;
    }

    public Long getEstabelecimentoId() {
        return estabelecimentoId;
    }

    public void setEstabelecimentoId(Long estabelecimentoId) {
        this.estabelecimentoId = estabelecimentoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public CategoriaAlimento getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaAlimento categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getQuantidadeKg() {
        return quantidadeKg;
    }

    public void setQuantidadeKg(BigDecimal quantidadeKg) {
        this.quantidadeKg = quantidadeKg;
    }

    public LocalDateTime getPrazoAproveitamento() {
        return prazoAproveitamento;
    }

    public void setPrazoAproveitamento(LocalDateTime prazoAproveitamento) {
        this.prazoAproveitamento = prazoAproveitamento;
    }
}
