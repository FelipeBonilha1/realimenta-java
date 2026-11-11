package br.com.fiap.realimenta.dto;

import br.com.fiap.realimenta.model.Instituicao;

public class InstituicaoResponse {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String endereco;
    private String bairro;
    private String cidade;

    public static InstituicaoResponse de(Instituicao origem) {
        InstituicaoResponse resposta = new InstituicaoResponse();
        resposta.id = origem.getId();
        resposta.nome = origem.getNome();
        resposta.email = origem.getEmail();
        resposta.telefone = origem.getTelefone();
        resposta.endereco = origem.getEndereco();
        resposta.bairro = origem.getBairro();
        resposta.cidade = origem.getCidade();
        return resposta;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }
}
