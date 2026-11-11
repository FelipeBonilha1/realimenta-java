package br.com.fiap.realimenta.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.realimenta.bo.DoacaoBO;
import br.com.fiap.realimenta.bo.EstabelecimentoBO;
import br.com.fiap.realimenta.bo.LoteAlimentoBO;
import br.com.fiap.realimenta.config.RelogioConfig;
import br.com.fiap.realimenta.exception.ConflitoNegocioException;
import br.com.fiap.realimenta.exception.RecursoNaoEncontradoException;
import br.com.fiap.realimenta.model.CategoriaAlimento;
import br.com.fiap.realimenta.model.Estabelecimento;
import br.com.fiap.realimenta.model.LoteAlimento;
import br.com.fiap.realimenta.model.StatusLote;
import br.com.fiap.realimenta.model.TipoEstabelecimento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        LoteAlimentoController.class,
        EstabelecimentoController.class,
        DoacaoController.class
})
@Import(RelogioConfig.class)
class ApiHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoteAlimentoBO loteAlimentoBO;

    @MockitoBean
    private EstabelecimentoBO estabelecimentoBO;

    @MockitoBean
    private DoacaoBO doacaoBO;

    @Test
    void criacaoValidaResponde201ComLocation() throws Exception {
        when(loteAlimentoBO.criar(any())).thenReturn(lote(15L));

        mockMvc.perform(post("/api/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loteJson("8.500")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/lotes/15"))
                .andExpect(jsonPath("$.id").value(15));
    }

    @Test
    void campoObrigatorioAusenteResponde400() throws Exception {
        mockMvc.perform(post("/api/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estabelecimentoId": 1,
                                  "categoria": "PANIFICADOS",
                                  "quantidadeKg": 8.5,
                                  "prazoAproveitamento": "2026-11-13T18:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Existem campos inválidos."))
                .andExpect(jsonPath("$.erros[?(@.campo == 'descricao')]").exists());
    }

    @Test
    void quantidadeZeroNegativaOuComPrecisaoExcedidaResponde400() throws Exception {
        mockMvc.perform(post("/api/lotes").contentType(MediaType.APPLICATION_JSON).content(loteJson("0")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("maior que zero")));
        mockMvc.perform(post("/api/lotes").contentType(MediaType.APPLICATION_JSON).content(loteJson("-1")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("maior que zero")));
        mockMvc.perform(post("/api/lotes").contentType(MediaType.APPLICATION_JSON).content(loteJson("1.1234")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("3 decimais")));
    }

    @Test
    void jsonMalformadoResponde400() throws Exception {
        mockMvc.perform(post("/api/lotes").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O JSON enviado é inválido ou contém um valor não reconhecido."));
    }

    @Test
    void enumInvalidoResponde400() throws Exception {
        mockMvc.perform(post("/api/estabelecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Padaria",
                                  "tipo": "BANCA",
                                  "email": "contato@padaria.example",
                                  "telefone": "11999999999",
                                  "endereco": "Rua A, 10",
                                  "bairro": "Vila Mariana",
                                  "cidade": "São Paulo"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O JSON enviado é inválido ou contém um valor não reconhecido."));
    }

    @Test
    void campoDesconhecidoResponde400() throws Exception {
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O JSON enviado é inválido ou contém um valor não reconhecido."));
    }

    @Test
    void serializaDataComoTextoIso() throws Exception {
        when(loteAlimentoBO.buscar(15L)).thenReturn(lote(15L));

        mockMvc.perform(get("/api/lotes/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prazoAproveitamento").value("2026-11-13T18:00:00"))
                .andExpect(content().string(not(containsString("[2026"))));
    }

    @Test
    void recursoInexistenteResponde404() throws Exception {
        when(loteAlimentoBO.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Lote não encontrado."));

        mockMvc.perform(get("/api/lotes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.caminho").value("/api/lotes/99"))
                .andExpect(jsonPath("$.instante").value(containsString("-03:00")));
    }

    @Test
    void conflitoDeNegocioResponde409() throws Exception {
        when(loteAlimentoBO.atualizar(any(), any())).thenThrow(new ConflitoNegocioException(
                "Quantidade, estabelecimento e prazo não podem ser alterados depois que o lote deixa de estar DISPONIVEL."));

        mockMvc.perform(put("/api/lotes/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loteJson("9")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void erroInesperadoResponde500SemSqlNemStack() throws Exception {
        when(loteAlimentoBO.buscar(7L))
                .thenThrow(new RuntimeException("ORA-00942 SELECT SENHA FROM USUARIO"));

        mockMvc.perform(get("/api/lotes/7"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Ocorreu uma falha inesperada. Tente novamente mais tarde."))
                .andExpect(content().string(not(containsString("ORA"))))
                .andExpect(content().string(not(containsString("SELECT"))))
                .andExpect(content().string(not(containsString("SENHA"))))
                .andExpect(content().string(not(containsString("RuntimeException"))));
    }

    @Test
    void exclusaoValidaResponde204() throws Exception {
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(4L);
        estabelecimento.setNome("Padaria");
        estabelecimento.setTipo(TipoEstabelecimento.PADARIA);
        when(estabelecimentoBO.buscar(4L)).thenReturn(estabelecimento);

        mockMvc.perform(delete("/api/estabelecimentos/4"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    private String loteJson(String quantidade) {
        return """
                {
                  "estabelecimentoId": 1,
                  "descricao": "Paes do dia",
                  "categoria": "PANIFICADOS",
                  "quantidadeKg": %s,
                  "prazoAproveitamento": "2026-11-13T18:00:00"
                }
                """.formatted(quantidade);
    }

    private LoteAlimento lote(Long id) {
        LoteAlimento lote = new LoteAlimento();
        lote.setId(id);
        lote.setEstabelecimentoId(1L);
        lote.setDescricao("Paes do dia");
        lote.setCategoria(CategoriaAlimento.PANIFICADOS);
        lote.setQuantidadeKg(new BigDecimal("8.500"));
        lote.setPrazoAproveitamento(LocalDateTime.of(2026, 11, 13, 18, 0));
        lote.setStatus(StatusLote.DISPONIVEL);
        return lote;
    }
}
