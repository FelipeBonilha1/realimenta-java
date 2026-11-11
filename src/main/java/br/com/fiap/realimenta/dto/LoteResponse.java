package br.com.fiap.realimenta.dto;

import br.com.fiap.realimenta.model.CategoriaAlimento;
import br.com.fiap.realimenta.model.LoteAlimento;
import br.com.fiap.realimenta.model.StatusLote;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoteResponse {

    private Long id;
    private Long estabelecimentoId;
    private String descricao;
    private CategoriaAlimento categoria;
    private BigDecimal quantidadeKg;
    private LocalDateTime prazoAproveitamento;
    private StatusLote status;

    public static LoteResponse de(LoteAlimento origem) {
        LoteResponse resposta = new LoteResponse();
        resposta.id = origem.getId();
        resposta.estabelecimentoId = origem.getEstabelecimentoId();
        resposta.descricao = origem.getDescricao();
        resposta.categoria = origem.getCategoria();
        resposta.quantidadeKg = origem.getQuantidadeKg();
        resposta.prazoAproveitamento = origem.getPrazoAproveitamento();
        resposta.status = origem.getStatus();
        return resposta;
    }

    public Long getId() {
        return id;
    }

    public Long getEstabelecimentoId() {
        return estabelecimentoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public CategoriaAlimento getCategoria() {
        return categoria;
    }

    public BigDecimal getQuantidadeKg() {
        return quantidadeKg;
    }

    public LocalDateTime getPrazoAproveitamento() {
        return prazoAproveitamento;
    }

    public StatusLote getStatus() {
        return status;
    }
}
