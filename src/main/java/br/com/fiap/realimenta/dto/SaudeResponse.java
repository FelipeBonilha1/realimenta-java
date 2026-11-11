package br.com.fiap.realimenta.dto;

public class SaudeResponse {

    private String status;
    private String aplicacao;
    private String fuso;

    public SaudeResponse(String status, String aplicacao, String fuso) {
        this.status = status;
        this.aplicacao = aplicacao;
        this.fuso = fuso;
    }

    public String getStatus() {
        return status;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public String getFuso() {
        return fuso;
    }
}
