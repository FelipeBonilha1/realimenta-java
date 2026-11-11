package br.com.fiap.realimenta.controller;

import br.com.fiap.realimenta.bo.InstituicaoBO;
import br.com.fiap.realimenta.dto.InstituicaoRequest;
import br.com.fiap.realimenta.dto.InstituicaoResponse;
import br.com.fiap.realimenta.model.Instituicao;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/instituicoes", produces = MediaType.APPLICATION_JSON_VALUE)
public class InstituicaoController {

    private final InstituicaoBO instituicaoBO;

    public InstituicaoController(InstituicaoBO instituicaoBO) {
        this.instituicaoBO = instituicaoBO;
    }

    @GetMapping
    public List<InstituicaoResponse> listar() {
        return instituicaoBO.listar().stream().map(InstituicaoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public InstituicaoResponse buscar(@PathVariable Long id) {
        return InstituicaoResponse.de(instituicaoBO.buscar(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InstituicaoResponse> criar(@Valid @RequestBody InstituicaoRequest requisicao) {
        Instituicao criada = instituicaoBO.criar(requisicao.paraEntidade());
        URI local = URI.create("/api/instituicoes/" + criada.getId());
        return ResponseEntity.created(local).body(InstituicaoResponse.de(criada));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public InstituicaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody InstituicaoRequest requisicao) {
        return InstituicaoResponse.de(instituicaoBO.atualizar(id, requisicao.paraEntidade()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        instituicaoBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
