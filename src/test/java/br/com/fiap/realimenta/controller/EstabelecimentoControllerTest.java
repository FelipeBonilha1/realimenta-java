package br.com.fiap.realimenta.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.realimenta.bo.EstabelecimentoBO;
import br.com.fiap.realimenta.dto.EstabelecimentoRequest;
import br.com.fiap.realimenta.dto.EstabelecimentoResponse;
import br.com.fiap.realimenta.model.Estabelecimento;
import br.com.fiap.realimenta.model.TipoEstabelecimento;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class EstabelecimentoControllerTest {

    @Test
    void criacaoResponde201ComLocalDoRecurso() {
        EstabelecimentoBO bo = mock(EstabelecimentoBO.class);
        EstabelecimentoController controller = new EstabelecimentoController(bo);
        Estabelecimento gravado = new Estabelecimento();
        gravado.setId(4L);
        gravado.setNome("Padaria Sol");
        gravado.setTipo(TipoEstabelecimento.PADARIA);
        gravado.setEmail("contato@padaria.example");
        gravado.setTelefone("11999999999");
        gravado.setEndereco("Rua A, 10");
        gravado.setBairro("Vila Mariana");
        gravado.setCidade("São Paulo");
        when(bo.criar(org.mockito.ArgumentMatchers.any())).thenReturn(gravado);

        ResponseEntity<EstabelecimentoResponse> resposta = controller.criar(requisicao());

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals("/api/estabelecimentos/4", resposta.getHeaders().getLocation().toString());
        assertEquals(4L, resposta.getBody().getId());
    }

    @Test
    void exclusaoResponde204SemCorpo() {
        EstabelecimentoBO bo = mock(EstabelecimentoBO.class);
        EstabelecimentoController controller = new EstabelecimentoController(bo);

        ResponseEntity<Void> resposta = controller.excluir(4L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        assertNull(resposta.getBody());
        verify(bo).excluir(4L);
    }

    private EstabelecimentoRequest requisicao() {
        EstabelecimentoRequest requisicao = new EstabelecimentoRequest();
        requisicao.setNome("Padaria Sol");
        requisicao.setTipo(TipoEstabelecimento.PADARIA);
        requisicao.setEmail("contato@padaria.example");
        requisicao.setTelefone("11999999999");
        requisicao.setEndereco("Rua A, 10");
        requisicao.setBairro("Vila Mariana");
        requisicao.setCidade("São Paulo");
        return requisicao;
    }
}
