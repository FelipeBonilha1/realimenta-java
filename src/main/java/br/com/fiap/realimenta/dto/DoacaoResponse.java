package br.com.fiap.realimenta.dto;

import br.com.fiap.realimenta.model.Doacao;
import br.com.fiap.realimenta.model.StatusDoacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DoacaoResponse {

    private Long id;
    private Long loteId;
    private Long instituicaoId;
    private StatusDoacao status;
    private LocalDateTime dataReserva;
    private LocalDateTime dataColeta;
    private LocalDateTime dataEntrega;
    private BigDecimal quantidadeEntregueKg;

    public static DoacaoResponse de(Doacao origem) {
        DoacaoResponse resposta = new DoacaoResponse();
        resposta.id = origem.getId();
        resposta.loteId = origem.getLoteId();
        resposta.instituicaoId = origem.getInstituicaoId();
        resposta.status = origem.getStatus();
        resposta.dataReserva = origem.getDataReserva();
        resposta.dataColeta = origem.getDataColeta();
        resposta.dataEntrega = origem.getDataEntrega();
        resposta.quantidadeEntregueKg = origem.getQuantidadeEntregueKg();
        return resposta;
    }

    public Long getId() {
        return id;
    }

    public Long getLoteId() {
        return loteId;
    }

    public Long getInstituicaoId() {
        return instituicaoId;
    }

    public StatusDoacao getStatus() {
        return status;
    }

    public LocalDateTime getDataReserva() {
        return dataReserva;
    }

    public LocalDateTime getDataColeta() {
        return dataColeta;
    }

    public LocalDateTime getDataEntrega() {
        return dataEntrega;
    }

    public BigDecimal getQuantidadeEntregueKg() {
        return quantidadeEntregueKg;
    }
}
