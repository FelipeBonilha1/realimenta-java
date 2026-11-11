package br.com.fiap.realimenta.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ErroResponse {

    private int status;
    private String mensagem;
    private String caminho;
    private OffsetDateTime instante;
    private List<ErroCampo> erros = new ArrayList<>();

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public OffsetDateTime getInstante() {
        return instante;
    }

    public void setInstante(OffsetDateTime instante) {
        this.instante = instante;
    }

    public List<ErroCampo> getErros() {
        return erros;
    }

    public void setErros(List<ErroCampo> erros) {
        this.erros = erros;
    }
}
