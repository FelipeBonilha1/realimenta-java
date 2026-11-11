# Roteiro de demonstração

Use dados fictícios. Confirme que `01_estrutura.sql` e `02_dados_demonstracao.sql` já foram executados e que a API subiu com as variáveis do Oracle.

Os ids 1 a 4 são os da carga fictícia. Um lote criado pela API recebe id a partir de 5, porque as sequences começam em 5.

Abra também `docs/prototipos.html` no navegador para mostrar as telas. Deixe claro que o HTML não fala com a API.

## 1. Saúde e cadastros

```powershell
curl.exe http://localhost:8080/api/saude
curl.exe http://localhost:8080/api/estabelecimentos
curl.exe http://localhost:8080/api/instituicoes
```

Espere 200. `/api/saude` não prova o banco. A lista de estabelecimentos prova.

## 2. Disponibilidade real

```powershell
curl.exe "http://localhost:8080/api/lotes?disponiveis=true"
curl.exe http://localhost:8080/api/lotes
```

Na carga intacta, o filtro mostra o lote 1. O lote 2 continua `DISPONIVEL` no cadastro, mas o prazo já passou, então não entra no filtro. O lote 3 está reservado. O lote 4 está entregue.

## 3. Reserva, coleta e entrega

A doação 1 da carga já está reservada para outro lote. Este roteiro cria uma reserva nova, lê o `id` da resposta e usa esse mesmo id na coleta e na entrega.

O lote 1 tem 8,5 kg. A primeira entrega pede 9 kg e deve falhar. A segunda pede 8,0 kg.

```powershell
$reservaJson = '{"loteId":1,"instituicaoId":1}'
$reserva = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/doacoes -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($reservaJson))
$id = $reserva.id
Write-Output "Doacao criada: $id"

try {
    Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/doacoes -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($reservaJson))
} catch {
    Write-Output "Segunda reserva recusada, como esperado."
}

Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/doacoes/$id/coleta"

try {
    Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/doacoes/$id/cancelamento"
} catch {
    Write-Output "Cancelamento apos a coleta recusado, como esperado."
}

$entregaAlta = '{"quantidadeEntregueKg":9}'
try {
    Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/doacoes/$id/entrega" -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($entregaAlta))
} catch {
    Write-Output "Entrega acima do ofertado recusada, como esperado."
}

$entrega = '{"quantidadeEntregueKg":8.0}'
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/doacoes/$id/entrega" -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($entrega))
```

Espere 201 na primeira reserva, 409 na segunda, doação `COLETADA` depois da coleta, 409 no cancelamento tardio, 409 na quantidade 9 e 200 na quantidade 8,0. A doação e o lote terminam `ENTREGUE`.

## 6. Indicadores

```powershell
curl.exe http://localhost:8080/api/indicadores
```

Antes de qualquer operação nova, a carga sozinha responde 39,500 kg ofertados, 9,500 kg entregues, 1 doação entregue e 1 instituição. Depois da entrega do passo 5, os quilos entregues e a contagem sobem. Explique que os quilos ofertados continuam sendo a soma das ofertas cadastradas.

## 7. O que não fazer na demo

- Não diga que o HTML gravou alguma coisa.
- Não apresente reserva ou coleta como entrega.
- Não converta o indicador em refeições, água ou carbono.
- Não afirme que o Render está no ar se `/api/saude` público não foi aberto.
- Não mostre senha, connection string completa com usuário embutido, nem stack trace.
