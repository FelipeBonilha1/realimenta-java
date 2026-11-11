# Endpoints — ReAlimenta SP

Base local: `http://localhost:8080`

Datas e horas são texto ISO-8601 sem deslocamento, no horário civil de `America/Sao_Paulo`. Exemplo: `2026-11-13T18:00:00`.

Campos desconhecidos no JSON são recusados com 400. Por isso um PUT não consegue enviar `status`, `dataColeta` ou `dataEntrega`: esses nomes não existem nos DTOs de entrada e o SQL correspondente não grava essas colunas.

## Códigos usados

| Código | Situação |
| --- | --- |
| 200 | Consulta ou atualização com corpo |
| 201 | Criação, com cabeçalho `Location` |
| 204 | Exclusão, sem corpo |
| 400 | JSON inválido, campo inválido ou identificador malformado |
| 404 | Recurso inexistente |
| 409 | Conflito de estado ou de integridade |
| 405 | Método não aceito. Não há DELETE de doação |
| 415 | Conteúdo que não é JSON |
| 500 | Falha inesperada, sem detalhe interno |

Corpo de erro:

```json
{
  "status": 409,
  "mensagem": "O lote só pode ser reservado quando está DISPONIVEL e dentro do prazo de aproveitamento.",
  "caminho": "/api/doacoes",
  "instante": "2026-11-11T12:00:00-03:00",
  "erros": []
}
```

Quando a falha vem da validação dos campos, `erros` traz `campo` e `mensagem`.

## Saúde

| Método | Caminho | Sucesso |
| --- | --- | --- |
| GET | `/api/saude` | 200 |

Resposta: `status`, `aplicacao`, `fuso`. Não consulta o banco e não devolve URL, usuário ou senha.

Para saber se o Oracle respondeu, chame `GET /api/estabelecimentos`. Se a conexão falhar, a resposta é 500 genérica. O detalhe fica somente no log do servidor.

## Estabelecimentos

| Método | Caminho | Sucesso |
| --- | --- | --- |
| GET | `/api/estabelecimentos` | 200 |
| GET | `/api/estabelecimentos/{id}` | 200 |
| POST | `/api/estabelecimentos` | 201 |
| PUT | `/api/estabelecimentos/{id}` | 200 |
| DELETE | `/api/estabelecimentos/{id}` | 204 |

POST e PUT:

```json
{
  "nome": "Padaria Sol Nascente",
  "tipo": "PADARIA",
  "email": "contato@padariasolnascente.example",
  "telefone": "11987654321",
  "endereco": "Rua das Flores, 120",
  "bairro": "Vila Mariana",
  "cidade": "São Paulo"
}
```

`tipo`: `RESTAURANTE`, `PADARIA`, `SUPERMERCADO`, `HOTEL`, `COZINHA_INDUSTRIAL`, `OUTRO`.

O e-mail é gravado em minúsculas. E-mail repetido responde 409. DELETE responde 409 se existir lote vinculado.

## Instituições

| Método | Caminho | Sucesso |
| --- | --- | --- |
| GET | `/api/instituicoes` | 200 |
| GET | `/api/instituicoes/{id}` | 200 |
| POST | `/api/instituicoes` | 201 |
| PUT | `/api/instituicoes/{id}` | 200 |
| DELETE | `/api/instituicoes/{id}` | 204 |

O corpo é o mesmo do estabelecimento, sem `tipo`. DELETE responde 409 se existir doação vinculada, inclusive cancelada.

## Lotes

| Método | Caminho | Sucesso |
| --- | --- | --- |
| GET | `/api/lotes` | 200 |
| GET | `/api/lotes?disponiveis=true` | 200 |
| GET | `/api/lotes/{id}` | 200 |
| POST | `/api/lotes` | 201 |
| PUT | `/api/lotes/{id}` | 200 |
| DELETE | `/api/lotes/{id}` | 204 |

`disponiveis=true` devolve somente `STATUS = DISPONIVEL` e `PRAZO_APROVEITAMENTO` posterior ao instante atual. Um lote com status disponível e prazo vencido aparece na listagem geral e some desse filtro.

POST:

```json
{
  "estabelecimentoId": 1,
  "descricao": "Paes do dia sem comercializacao",
  "categoria": "PANIFICADOS",
  "quantidadeKg": 8.5,
  "prazoAproveitamento": "2026-11-13T18:00:00"
}
```

`categoria`: `PREPARADO`, `IN_NATURA`, `PANIFICADOS`, `LATICINIOS`, `MERCEARIA`, `OUTROS`.

O status inicial é sempre `DISPONIVEL`. Quantidade precisa ser maior que zero. O prazo precisa ser futuro. O estabelecimento precisa existir.

PUT aceita os mesmos campos e não aceita `status`. Quantidade, estabelecimento e prazo só mudam enquanto o lote está `DISPONIVEL`. Descrição e categoria ainda podem ser ajustadas depois. DELETE físico só ocorre se não houver doação, inclusive cancelada.

## Doações

Não existe DELETE. Cancelar é operação de negócio e preserva a linha.

| Método | Caminho | Sucesso | Efeito |
| --- | --- | --- | --- |
| POST | `/api/doacoes` | 201 | Reserva o lote inteiro |
| GET | `/api/doacoes` | 200 | Lista |
| GET | `/api/doacoes/{id}` | 200 | Consulta |
| PUT | `/api/doacoes/{id}/coleta` | 200 | Sem corpo. Exige doação `RESERVADA` e prazo vigente |
| PUT | `/api/doacoes/{id}/entrega` | 200 | Exige doação `COLETADA`, prazo vigente e quantidade válida |
| PUT | `/api/doacoes/{id}/cancelamento` | 200 | Sem corpo. Só antes da coleta |

Reserva:

```json
{
  "loteId": 1,
  "instituicaoId": 1
}
```

Entrega:

```json
{
  "quantidadeEntregueKg": 8.0
}
```

A resposta do POST traz `id`. Coleta, entrega e cancelamento usam esse id. A doação 1 da carga fictícia é outra reserva e não substitui a doação recém-criada.

A quantidade entregue precisa ser positiva e não pode superar a quantidade ofertada do lote. Reserva ou coleta não entram no indicador de entrega.

Transições:

| Ação | Doação | Lote |
| --- | --- | --- |
| Reservar | `RESERVADA` | `RESERVADO` |
| Coletar | `COLETADA` | `COLETADO` |
| Entregar | `ENTREGUE` | `ENTREGUE` |
| Cancelar | `CANCELADA` | `CANCELADO` |

O lote cancelado não volta a `DISPONIVEL`. Uma nova oferta é um novo lote.

## Indicadores

| Método | Caminho | Sucesso |
| --- | --- | --- |
| GET | `/api/indicadores` | 200 |

```json
{
  "kgOfertados": 39.500,
  "kgEntregues": 9.500,
  "doacoesEntregues": 1,
  "instituicoesComEntrega": 1
}
```

`kgOfertados` soma `QUANTIDADE_KG` de todos os lotes cadastrados. É registro de oferta, não quilo entregue. Os outros três campos consideram apenas doação `ENTREGUE`.

Os números acima valem para o script de demonstração intacto: 8,5 + 15 + 6 + 10 = 39,5 kg ofertados e 9,5 kg entregues.
