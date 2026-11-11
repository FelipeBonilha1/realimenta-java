package br.com.fiap.realimenta.controller;

import br.com.fiap.realimenta.bo.LoteAlimentoBO;
import br.com.fiap.realimenta.dto.LoteAtualizacaoRequest;
import br.com.fiap.realimenta.dto.LoteCriacaoRequest;
import br.com.fiap.realimenta.dto.LoteResponse;
import br.com.fiap.realimenta.model.LoteAlimento;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/lotes", produces = MediaType.APPLICATION_JSON_VALUE)
public class LoteAlimentoController {

    private final LoteAlimentoBO loteAlimentoBO;

    public LoteAlimentoController(LoteAlimentoBO loteAlimentoBO) {
        this.loteAlimentoBO = loteAlimentoBO;
    }

    @GetMapping
    public List<LoteResponse> listar(
            @RequestParam(name = "disponiveis", required = false, defaultValue = "false") boolean disponiveis) {
        List<LoteAlimento> lotes = disponiveis ? loteAlimentoBO.listarDisponiveis() : loteAlimentoBO.listar();
        return lotes.stream().map(LoteResponse::de).toList();
    }

    @GetMapping("/{id}")
    public LoteResponse buscar(@PathVariable Long id) {
        return LoteResponse.de(loteAlimentoBO.buscar(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoteResponse> criar(@Valid @RequestBody LoteCriacaoRequest requisicao) {
        LoteAlimento criado = loteAlimentoBO.criar(requisicao.paraEntidade());
        URI local = URI.create("/api/lotes/" + criado.getId());
        return ResponseEntity.created(local).body(LoteResponse.de(criado));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoteResponse atualizar(@PathVariable Long id, @Valid @RequestBody LoteAtualizacaoRequest requisicao) {
        return LoteResponse.de(loteAlimentoBO.atualizar(id, requisicao.paraEntidade()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        loteAlimentoBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
