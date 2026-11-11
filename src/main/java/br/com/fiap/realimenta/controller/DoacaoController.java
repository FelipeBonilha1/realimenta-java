package br.com.fiap.realimenta.controller;

import br.com.fiap.realimenta.bo.DoacaoBO;
import br.com.fiap.realimenta.dto.DoacaoEntregaRequest;
import br.com.fiap.realimenta.dto.DoacaoReservaRequest;
import br.com.fiap.realimenta.dto.DoacaoResponse;
import br.com.fiap.realimenta.model.Doacao;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/doacoes", produces = MediaType.APPLICATION_JSON_VALUE)
public class DoacaoController {

    private final DoacaoBO doacaoBO;

    public DoacaoController(DoacaoBO doacaoBO) {
        this.doacaoBO = doacaoBO;
    }

    @GetMapping
    public List<DoacaoResponse> listar() {
        return doacaoBO.listar().stream().map(DoacaoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public DoacaoResponse buscar(@PathVariable Long id) {
        return DoacaoResponse.de(doacaoBO.buscar(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DoacaoResponse> reservar(@Valid @RequestBody DoacaoReservaRequest requisicao) {
        Doacao criada = doacaoBO.reservar(requisicao.getLoteId(), requisicao.getInstituicaoId());
        URI local = URI.create("/api/doacoes/" + criada.getId());
        return ResponseEntity.created(local).body(DoacaoResponse.de(criada));
    }

    @PutMapping("/{id}/coleta")
    public DoacaoResponse coletar(@PathVariable Long id) {
        return DoacaoResponse.de(doacaoBO.coletar(id));
    }

    @PutMapping(value = "/{id}/entrega", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DoacaoResponse entregar(@PathVariable Long id, @Valid @RequestBody DoacaoEntregaRequest requisicao) {
        return DoacaoResponse.de(doacaoBO.entregar(id, requisicao.getQuantidadeEntregueKg()));
    }

    @PutMapping("/{id}/cancelamento")
    public DoacaoResponse cancelar(@PathVariable Long id) {
        return DoacaoResponse.de(doacaoBO.cancelar(id));
    }
}
