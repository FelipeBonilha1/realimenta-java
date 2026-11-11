package br.com.fiap.realimenta.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoteAlimento {

    private Long id;
    private Long estabelecimentoId;
    private String descricao;
    private CategoriaAlimento categoria;
    private BigDecimal quantidadeKg;
    private LocalDateTime prazoAproveitamento;
    private StatusLote status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public StatusLote getStatus() {
        return status;
    }

    public void setStatus(StatusLote status) {
        this.status = status;
    }
}
