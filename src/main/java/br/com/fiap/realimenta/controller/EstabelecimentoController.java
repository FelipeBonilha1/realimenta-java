package br.com.fiap.realimenta.controller;

import br.com.fiap.realimenta.bo.EstabelecimentoBO;
import br.com.fiap.realimenta.dto.EstabelecimentoRequest;
import br.com.fiap.realimenta.dto.EstabelecimentoResponse;
import br.com.fiap.realimenta.model.Estabelecimento;
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
@RequestMapping(value = "/api/estabelecimentos", produces = MediaType.APPLICATION_JSON_VALUE)
public class EstabelecimentoController {

    private final EstabelecimentoBO estabelecimentoBO;

    public EstabelecimentoController(EstabelecimentoBO estabelecimentoBO) {
        this.estabelecimentoBO = estabelecimentoBO;
    }

    @GetMapping
    public List<EstabelecimentoResponse> listar() {
        return estabelecimentoBO.listar().stream().map(EstabelecimentoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public EstabelecimentoResponse buscar(@PathVariable Long id) {
        return EstabelecimentoResponse.de(estabelecimentoBO.buscar(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EstabelecimentoResponse> criar(@Valid @RequestBody EstabelecimentoRequest requisicao) {
        Estabelecimento criado = estabelecimentoBO.criar(requisicao.paraEntidade());
        URI local = URI.create("/api/estabelecimentos/" + criado.getId());
        return ResponseEntity.created(local).body(EstabelecimentoResponse.de(criado));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EstabelecimentoResponse atualizar(@PathVariable Long id,
                                             @Valid @RequestBody EstabelecimentoRequest requisicao) {
        return EstabelecimentoResponse.de(estabelecimentoBO.atualizar(id, requisicao.paraEntidade()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        estabelecimentoBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
