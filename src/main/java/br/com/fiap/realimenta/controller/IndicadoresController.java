package br.com.fiap.realimenta.controller;

import br.com.fiap.realimenta.bo.IndicadoresBO;
import br.com.fiap.realimenta.dto.IndicadoresResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/indicadores", produces = MediaType.APPLICATION_JSON_VALUE)
public class IndicadoresController {

    private final IndicadoresBO indicadoresBO;

    public IndicadoresController(IndicadoresBO indicadoresBO) {
        this.indicadoresBO = indicadoresBO;
    }

    @GetMapping
    public IndicadoresResponse consultar() {
        return IndicadoresResponse.de(indicadoresBO.consultar());
    }
}
