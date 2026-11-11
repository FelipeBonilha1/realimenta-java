# Checklist da entrega de Java

Marque só o que estiver de fato pronto. Itens de nome, repositório, Oracle e Render dependem do grupo.

## Código

- [x] API Spring Boot com CRUD e o fluxo de doação
- [x] Pacotes model, dto, dao, bo, controller e exception
- [x] DAO com SQL explícito em JdbcTemplate
- [x] Regras no BO, com transação nas mudanças de lote e doação
- [x] GET, POST, PUT e DELETE nos cadastros e nos lotes
- [x] Tratamento centralizado de exceções, sem stack trace na resposta
- [x] Scripts Oracle versionados, sem execução automática e sem senha
- [x] Variáveis DB_URL, DB_USERNAME e DB_PASSWORD
- [x] Dockerfile e leitura de PORT
- [x] Endpoint /api/saude sem dado sensível
- [x] Testes de regra com relógio fixo
- [x] Testes executados em 11/11/2026, 19:01: 46 testes, 0 falhas. Desses, 33 unitários e 13 web com MockMvc. JDK da máquina: OpenJDK 25.0.2, bytecode release 21. Isso não comprova runtime Java 21
- [x] Teste de integração Oracle escrito em FluxoOracleIT e separado pelo perfil oracle-it
- [ ] Teste de integração Oracle executado em schema exclusivo
- [ ] Conexão Oracle da aplicação executada de verdade
- [ ] Aplicação publicada no Render e URL aberta no navegador

## Documentos para o PDF

- [x] Base acadêmica em docs/documentacao-java.md
- [x] Tabela de endpoints
- [x] Diagrama de classes em PlantUML
- [x] DER em Mermaid
- [x] Protótipos estáticos
- [x] Instruções de execução e tabela de endpoints dentro de docs/documentacao-java.md
- [x] Scripts marcados como proposta de Database, com carga 2, 2, 4 e 2
- [ ] PDF, GitHub e Render concluídos
- [ ] Exportar as figuras do diagrama e do DER
- [ ] Capturar as quatro telas do protótipo
- [ ] Montar um único PDF
- [ ] Preencher nomes, RMs, bairro e professor
- [ ] Colar a URL real do GitHub
- [ ] Colar a URL real do Render, somente se o serviço tiver sido publicado e testado

## Conferência rápida antes de zipar

- [ ] O ZIP do trabalho de Java contém o PDF, não este Markdown no lugar do PDF
- [ ] O ZIP não contém `.env`, senha ou target/
- [ ] O grupo consegue explicar Controller, BO, DAO e Oracle sem ler a resposta pronta
- [ ] O grupo não afirma que JdbcTemplate foi exigido pelo enunciado
- [ ] O grupo não apresenta kg ofertado como kg entregue
