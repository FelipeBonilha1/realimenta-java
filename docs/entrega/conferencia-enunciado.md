# Conferência com o enunciado oficial — pendente de arquivo

**Status (19/11/2026):** o PDF ou texto do enunciado da disciplina **não foi localizado** no repositório `realimenta-java` (somente `docs/documentacao-java.pdf` na pasta `docs/`, que não é o enunciado FIAP). **Não há conformidade integral com o enunciado formal até o arquivo ser anexado.**

**Ação necessária:** coloque o enunciado em `docs/entrega/` (ex.: `enunciado-java.pdf`) ou informe o caminho no workspace para preencher a coluna «Requisito (enunciado)» com os itens literais da disciplina.

## Lista de conferência (rascunho × PDF `ReAlimenta_SP_Documentacao_Java.pdf`, 17 páginas)

| Requisito (item do grupo / esperado) | Seção / página no PDF | Situação |
| --- | --- | --- |
| Capa (disciplina, integrantes, RMs) | Capa — **p. 1** | Atendido |
| Sumário com numeração real | Sumário — **p. 2** (itens 1→3 … 9→17) | Atendido (`gerar-pdf.js`, 2 passagens) |
| Apresentação e problema | §1 — **p. 3** | Atendido |
| Justificativa e objetivos | §1 — **p. 3** | Atendido |
| Arquitetura e stack | §2 — **p. 4** | Atendido |
| Diagrama de classes (figura legível) | §3 — **p. 5**, Figura 1 | Atendido (`fig-classes.png`) |
| DER / banco / sequences | §4 — **pp. 6–8**, Figura 2 | Atendido (`fig-der.png`) |
| Protótipos de telas (não confundir com front integrado) | §5 — **pp. 9–12**, Figuras 3–6 | Atendido (capturas de `docs/prototipos.html`) |
| Tabela de endpoints | §6 — **pp. 13–14** | Atendido |
| Execução local / Docker / variáveis | §7 — **p. 15** | Atendido (sem senha; só nomes de variáveis) |
| Deploy e URL pública | §8 — **p. 16** | Atendido (`realimenta-java.onrender.com`) |
| Verificação de testes | §9 — **p. 17** | Atendido |
| Conformidade **literal** com enunciado FIAP | — | **Pendente** (falta o arquivo oficial) |

## Verificações automáticas executadas

- `capturar-prototipos.js` — 4 PNGs em `docs/entrega/`
- `gerar-pdf.js` — PDF final + atualização do sumário no HTML
- `verificar-pdf.js` — 17 páginas; seções 3–17; legendas Figuras 1–6; sumário × seções **OK**; sem senha literal no texto extraído

## Revisão visual recomendada (feita pelo gerador + amostragem de texto)

- Nenhuma página com menos de 80 caracteres extraídos (sem páginas «em branco»).
- §4 continua em múltiplas páginas por causa do DER e do texto de sequences (esperado).
- Protótipos: uma subtela por bloco com quebra de página antes dos protótipos 2–4.

*Após anexar o enunciado, duplicar a tabela acima com uma linha por requisito do professor e marcar «Atendido» / «Pendente» com página exata.*