package br.com.fiap.realimenta.controller;

import br.com.fiap.realimenta.config.RelogioConfig;
import br.com.fiap.realimenta.dto.SaudeResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/saude", produces = MediaType.APPLICATION_JSON_VALUE)
public class SaudeController {

    @GetMapping
    public SaudeResponse consultar() {
        return new SaudeResponse("UP", "ReAlimenta SP", RelogioConfig.FUSO.getId());
    }
}
