package br.com.fiap.realimenta.dto;

import br.com.fiap.realimenta.model.Estabelecimento;
import br.com.fiap.realimenta.model.TipoEstabelecimento;

public class EstabelecimentoResponse {

    private Long id;
    private String nome;
    private TipoEstabelecimento tipo;
    private String email;
    private String telefone;
    private String endereco;
    private String bairro;
    private String cidade;

    public static EstabelecimentoResponse de(Estabelecimento origem) {
        EstabelecimentoResponse resposta = new EstabelecimentoResponse();
        resposta.id = origem.getId();
        resposta.nome = origem.getNome();
        resposta.tipo = origem.getTipo();
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

    public TipoEstabelecimento getTipo() {
        return tipo;
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
