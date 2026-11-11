package br.com.fiap.realimenta.dto;

import br.com.fiap.realimenta.model.Instituicao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InstituicaoRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    private String email;

    @NotBlank(message = "O telefone é obrigatório.")
    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
    private String telefone;

    @NotBlank(message = "O endereço é obrigatório.")
    @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres.")
    private String endereco;

    @NotBlank(message = "O bairro é obrigatório.")
    @Size(max = 80, message = "O bairro deve ter no máximo 80 caracteres.")
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória.")
    @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres.")
    private String cidade;

    public Instituicao paraEntidade() {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome(nome);
        instituicao.setEmail(email);
        instituicao.setTelefone(telefone);
        instituicao.setEndereco(endereco);
        instituicao.setBairro(bairro);
        instituicao.setCidade(cidade);
        return instituicao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
}
