package br.com.fiap.realimenta.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.realimenta.bo.LoteAlimentoBO;
import br.com.fiap.realimenta.config.RelogioConfig;
import br.com.fiap.realimenta.model.CategoriaAlimento;
import br.com.fiap.realimenta.model.LoteAlimento;
import br.com.fiap.realimenta.model.StatusLote;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Inverte as propriedades de Jackson para provar que elas mudam a resposta HTTP.
 * Nao basta conferir o texto de application.properties.
 */
@WebMvcTest(controllers = LoteAlimentoController.class)
@Import(RelogioConfig.class)
@TestPropertySource(properties = {
        "spring.jackson.deserialization.FAIL_ON_UNKNOWN_PROPERTIES=false",
        "spring.jackson.datatype.datetime.WRITE_DATES_AS_TIMESTAMPS=true"
})
class ApiHttpJacksonTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoteAlimentoBO loteAlimentoBO;

    @Test
    void propriedadeDesligaARecusaDeCampoDesconhecido() throws Exception {
        LoteAlimento lote = lote();
        when(loteAlimentoBO.atualizar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(lote);

        mockMvc.perform(put("/api/lotes/15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estabelecimentoId": 1,
                                  "descricao": "Paes do dia",
                                  "categoria": "PANIFICADOS",
                                  "quantidadeKg": 8.5,
                                  "prazoAproveitamento": "2026-11-13T18:00:00",
                                  "status": "ENTREGUE"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void propriedadeLigaASerializacaoDeDataComoNumero() throws Exception {
        when(loteAlimentoBO.buscar(15L)).thenReturn(lote());

        mockMvc.perform(get("/api/lotes/15"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("2026-11-13T18:00:00"))));
    }

    private LoteAlimento lote() {
        LoteAlimento lote = new LoteAlimento();
        lote.setId(15L);
        lote.setEstabelecimentoId(1L);
        lote.setDescricao("Paes do dia");
        lote.setCategoria(CategoriaAlimento.PANIFICADOS);
        lote.setQuantidadeKg(new BigDecimal("8.500"));
        lote.setPrazoAproveitamento(LocalDateTime.of(2026, 11, 13, 18, 0));
        lote.setStatus(StatusLote.DISPONIVEL);
        return lote;
    }
}
