# Registro de prompts — ReAlimenta SP

Este arquivo guarda o pedido que originou a implementação e o espaço para as próximas interações relevantes do grupo. Não apague o histórico. Uma interação é relevante quando muda regra de negócio, modelo, endpoint, banco, deploy ou o texto que vai para o PDF.

## Como registrar a próxima interação

Acrescente uma seção nova, no topo da lista de interações, com:

- data e hora;
- o que foi pedido, de preferência o texto original;
- o que foi decidido;
- quais arquivos mudaram;
- o que ficou pendente, principalmente Oracle, GitHub, Render, nomes e RMs.

Não registre senha, connection string com credencial ou dado pessoal real.

## Interações

### 2026-11-11 19:01 — revisão de concorrência, prazo, testes e documentação

Pedido: corrigir o projeto existente sem recriá-lo. A edição do lote lia sem bloqueio enquanto a reserva usava `SELECT ... FOR UPDATE`. A comparação do prazo ignorava frações de segundo e a gravação persistia o valor completo. Os testes de controller não passavam pela camada HTTP. Faltavam teste de integração Oracle separado, documentação alinhada ao código e o aviso de que os scripts ainda são proposta de Database Design.

Decisões:

- `LoteAlimentoBO.atualizar` passa a travar a linha com `buscarPorIdParaAtualizacao` e valida o estado travado. A atualização cadastral não muda status.
- Prazo normalizado para microssegundo na entrada, na comparação e na persistência. Se a mudança estiver bloqueada e o prazo normalizado for o mesmo, o valor original permanece. `TIMESTAMP(6)` documenta a precisão padrão do Oracle. Não houve migração nem alteração de dados existentes.
- Testes web com MockMvc e BOs simulados. Teste de integração `FluxoOracleIT` só entra com `DB_IT_SCHEMA_EXCLUSIVO=true` e usuário diferente do schema do grupo. Ele não foi executado.
- Carga permanece em 2 estabelecimentos, 2 instituições, 4 lotes e 2 doações.
- Dependência de teste `spring-boot-starter-webmvc-test` adicionada porque o starter de teste do Spring Boot 4.1.2 não traz MockMvc. Nenhuma versão de dependência já existente foi alterada.

Verificação: `.\mvnw.cmd test` em 11/11/2026 19:01, OpenJDK 25.0.2, bytecode `release 21`, 46 testes, 0 falhas. O perfil `oracle-it` não rodou.

Pendências: schema Oracle exclusivo, conexão real, GitHub, Render, PDF, nomes e RMs. O bytecode `release 21` neste JDK 25 não comprova execução em Java 21.

### 2026-11-11 — implementação inicial

Pedido: implementar o projeto acadêmico completo ReAlimenta SP no repositório, com API Spring Boot, Oracle, camadas DAO e BO, documentação, diagramas, protótipos e testes. O texto integral está abaixo.

Decisões tomadas na implementação:

- Spring Boot 4.1.2 e Java 21, com `spring-boot-starter-webmvc`.
- `JdbcTemplate` como escolha do projeto, não como exigência do enunciado.
- Fuso `America/Sao_Paulo` e `LocalDateTime` gravado em `TIMESTAMP`.
- Uma doação por lote. Cancelamento não reabre o lote.
- Sequences a partir de 5. Dados fictícios usam ids 1 a 4.
- Testes de regra com Mockito e `Clock` fixo. Sem banco substituto.
- GitHub, Render e credenciais Oracle ficaram pendentes.

### Texto do pedido inicial

```text
Quero que você implemente um projeto acadêmico completo chamado ReAlimenta SP, para a disciplina Domain Driven Design — Java, do segundo ano de Engenharia de Software da FIAP.

Trabalhe diretamente nos arquivos do projeto. Não entregue apenas exemplos ou um planejamento: implemente, execute as verificações possíveis e registre claramente qualquer dependência externa que eu precise configurar.

CONTEXTO

O ReAlimenta SP conecta estabelecimentos que possuem alimentos excedentes a instituições sociais.

Esta API deve controlar o fluxo de publicação de um lote, reserva por uma instituição, coleta e confirmação de entrega.

O projeto deve ter qualidade, organização e coerência, mantendo complexidade adequada a um aluno do segundo ano, em seu primeiro ano de Java. Prefira código explícito e compreensível.

REQUISITOS OBRIGATÓRIOS DA FACULDADE

- API RESTful utilizando Spring Boot.
- Persistência no servidor Oracle da FIAP.
- Camada Model coerente com o banco.
- Camadas DAO e BO implementadas explicitamente.
- Controllers com GET, POST, PUT e DELETE.
- Tratamento centralizado de exceções.
- Preparação para deploy no Render.
- Diagrama de classes correspondente ao código.
- DER correspondente ao banco.
- Tabela de endpoints.
- Protótipos das telas relacionadas à API.
- Instruções de execução, ferramentas, versões, GitHub e deploy.
- Documentação final acadêmica em um único PDF, que será montado posteriormente.

ARQUITETURA PROPOSTA

Use Java 21 e Maven Wrapper.

Escolha uma versão estável do Spring Boot compatível com Java 21 e registre a versão escolhida. Consulte a documentação oficial quando necessário.

Use:
- Spring Web.
- Jakarta Bean Validation.
- Spring JDBC com JdbcTemplate.
- Driver Oracle compatível.
- Dependências de testes apropriadas.

A utilização de JdbcTemplate é uma escolha deste projeto para tornar a camada DAO explícita. Não alegue que essa tecnologia específica é exigida pelo enunciado.

Organize os pacotes em:
- model
- dto
- dao
- bo
- controller
- exception
- config, apenas quando necessário

Responsabilidades:
- Model: entidades e enums.
- DTO: entradas e saídas da API.
- DAO: SQL e persistência.
- BO: regras de negócio e transações.
- Controller: requisições e respostas HTTP.
- Exception: exceções de domínio e tratamento centralizado.

Não concentre regras de negócio nos controllers.
Não utilize Lombok, microserviços ou abstrações genéricas desnecessárias.
Não utilize dados em memória como substituição silenciosa do Oracle.

ESCOPO DAS ENTIDADES

1. Estabelecimento
- id
- nome
- tipo de estabelecimento
- email
- telefone
- endereço
- bairro
- cidade

2. Instituicao
- id
- nome
- email
- telefone
- endereço
- bairro
- cidade

3. LoteAlimento
- id
- estabelecimentoId
- descricao
- categoria
- quantidadeKg
- prazoAproveitamento
- status

Status do lote:
DISPONIVEL, RESERVADO, COLETADO, ENTREGUE, CANCELADO.

4. Doacao
- id
- loteId
- instituicaoId
- status
- dataReserva
- dataColeta
- dataEntrega
- quantidadeEntregueKg

Status da doação:
RESERVADA, COLETADA, ENTREGUE, CANCELADA.

Use BigDecimal para quantidades.
Documente a convenção de data, hora e fuso da aplicação.
Defina DTOs específicos para criação, atualização e operações do fluxo.

SIMPLIFICAÇÃO DO NEGÓCIO

Cada lote deve ser reservado integralmente por uma única instituição.
Não implementar divisão de um lote entre instituições.
Neste MVP, cada lote terá no máximo um registro de doação.

O cancelamento de uma reserva cancela a doação e o lote.
Para ofertar novamente os alimentos, o estabelecimento cria um novo lote.
Documente essa limitação.

REGRAS DE NEGÓCIO

- Quantidade ofertada deve ser maior que zero.
- Novo lote deve ter prazo de aproveitamento futuro.
- Estabelecimento e instituição referenciados devem existir.
- Lote só pode ser reservado se estiver DISPONIVEL e dentro do prazo.
- Impedir reserva duplicada, inclusive em requisições concorrentes.
- Aplicar restrição UNIQUE no lote da doação e controle transacional adequado.
- Coleta só pode ocorrer a partir de uma doação RESERVADA.
- Bloquear coleta após o prazo de aproveitamento.
- Entrega só pode ocorrer após a coleta.
- Bloquear confirmação de entrega após o prazo de aproveitamento.
- Quantidade entregue deve ser positiva e não superar a ofertada.
- Cancelamento só pode ocorrer antes da coleta.
- Atualização dos estados de lote e doação deve ocorrer na mesma transação.
- Não permitir que PUT genérico altere status e datas controlados pelo fluxo.
- Não permitir edição de quantidade, doador ou prazo de lote já reservado.
- Impedir exclusão de estabelecimentos ou instituições com histórico vinculado.
- Permitir exclusão física apenas de lote sem doação associada.
- Preservar registros de doações; cancelamento será uma operação de negócio, não exclusão física.

O prazo registrado é um controle operacional do protótipo, não uma certificação automática de segurança alimentar.

API

Implementar CRUD de estabelecimentos e instituições:
GET coleção
GET por id
POST
PUT por id
DELETE por id

Implementar para lotes:
GET /api/lotes
GET /api/lotes/{id}
POST /api/lotes
PUT /api/lotes/{id}
DELETE /api/lotes/{id}

A listagem de lotes deve permitir filtrar disponibilidade real:
status DISPONIVEL e prazo ainda não ultrapassado.

Implementar para doações:
POST /api/doacoes — reservar um lote
GET /api/doacoes
GET /api/doacoes/{id}
PUT /api/doacoes/{id}/coleta
PUT /api/doacoes/{id}/entrega
PUT /api/doacoes/{id}/cancelamento

Implementar:
GET /api/indicadores

Indicadores:
- kg ofertados, deixando claro que representam registros de oferta.
- kg efetivamente entregues.
- número de doações entregues.
- número de instituições distintas com entregas recebidas.

Não contabilizar reserva ou coleta como entrega.
Não inventar equivalência em refeições, água economizada ou CO2 evitado.

Usar status HTTP coerentes:
- 201 para criação.
- 200 para consultas e atualizações com resposta.
- 204 para exclusão sem corpo.
- 400 para dados inválidos.
- 404 para recurso inexistente.
- 409 para conflitos de estado ou integridade.
- 500 para falha inesperada, sem expor detalhes internos.

EXCEÇÕES E VALIDAÇÃO

Criar tratamento centralizado com RestControllerAdvice.
Padronizar respostas com status, mensagem, caminho, instante e erros de campos quando houver.

Tratar:
- DTO inválido.
- JSON malformado.
- identificadores inválidos.
- recurso inexistente.
- conflito de negócio.
- violação de integridade.
- falhas inesperadas.

Não devolver stack traces, SQL ou credenciais ao cliente.

ORACLE

Criar scripts SQL versionados para:
- tabelas.
- chaves primárias.
- chaves estrangeiras.
- constraints.
- índices justificados.
- sequences para geração dos IDs, se essa for a estratégia adotada.
- dados fictícios de demonstração.

Evitar scripts destrutivos executados automaticamente.
Não armazenar senhas no repositório.

Configurar conexão por variáveis de ambiente:
DB_URL
DB_USERNAME
DB_PASSWORD

Fornecer arquivo de exemplo sem credenciais reais.
Explicar como configurar variáveis no terminal e na IDE.
Não presumir que um arquivo .env seja carregado automaticamente pelo Spring Boot.

Alinhar nomes das tabelas, campos e tipos entre código, SQL e DER.
Registrar que o modelo deve ser compartilhado com o responsável por Database Design.

RENDER

Preparar Dockerfile e instruções de deploy.
Configurar a aplicação para utilizar a variável PORT disponibilizada pelo ambiente.
Documentar variáveis necessárias e como validar acesso ao Oracle a partir do ambiente hospedado.

Criar um endpoint simples de saúde sem dados sensíveis.

Não inventar URL de deploy.
Não afirmar que a aplicação foi publicada sem realmente publicar e verificar.
Se faltar acesso à conta ou ao Oracle, concluir toda a preparação possível e listar exatamente o que falta.

Não adicionar autenticação completa neste escopo.
Documentar que o protótipo usa dados fictícios e que controle de acesso é necessário antes de qualquer uso real.

TESTES

Escrever testes relevantes das regras de negócio:
- reserva válida.
- reserva duplicada.
- lote fora do prazo.
- coleta em estado incorreto.
- entrega sem coleta.
- quantidade entregue acima da ofertada.
- cancelamento após coleta.
- consistência das transições.

Usar Clock injetável se necessário para testar datas de forma determinística.
Distinguir testes unitários com mocks de testes reais de integração com Oracle.

Executar build e testes.
Se não houver credenciais, informar que persistência e conectividade Oracle permanecem sem validação.
Não usar um banco alternativo para afirmar que Oracle foi testado.

DOCUMENTAÇÃO

Criar:
- README.md.
- docs/documentacao-java.md.
- docs/endpoints.md.
- docs/diagrama-classes.puml.
- docs/der.mmd.
- docs/prototipos.html.
- docs/roteiro-demonstracao.md.
- docs/checklist-entrega.md.
- prompts.md.

README:
Começar pela Ficha da CitizenTech:
nome, cidade/bairro, cidadão beneficiado, problema, funcionamento, responsabilidade dos módulos, repositório, indicador e ODS.

Usar campos claramente pendentes para nomes, RMs, bairro, repositório e URL do deploy.
Não inventar entrevistas, evidências ou resultados de impacto.

O documento-base acadêmico deve conter:
capa, índice, descrição, justificativa, objetivos, arquitetura, modelos, instruções, endpoints, referência aos protótipos e limitações.

Criar protótipos estáticos de:
- cadastro de lote.
- consulta e reserva de lotes.
- acompanhamento e confirmação de entrega.
- painel de indicadores.

Identificar os protótipos como demonstração visual, sem fingir integração funcional.

Registrar este prompt em prompts.md e orientar o registro das próximas interações relevantes.

ENTREGA E VERIFICAÇÃO

O trabalho de Java terá ZIP com um único PDF acadêmico e links do GitHub e Render.
O PDF será produzido depois com base na implementação final.

Não apresentar documentação Markdown como se já fosse o PDF exigido.
Não criar arquivos PDF falsos.

Ao terminar:
1. Informe o que foi implementado.
2. Liste arquivos importantes.
3. Informe comandos de execução.
4. Apresente resultados reais de build e testes.
5. Liste pendências externas.
6. Explique o fluxo Controller → BO → DAO → Oracle.
7. Explique como demonstrar reserva, coleta e entrega.
8. Aponte limitações e decisões que eu preciso compreender para apresentar.

Comece inspecionando a pasta. Preserve arquivos existentes e implemente o projeto completo.
```
