# Documentação-base — Domain Driven Design, Java

Este Markdown é o texto-base do PDF acadêmico. Ele não é o PDF. O PDF único da entrega deve ser montado depois, com capa diagramada, as figuras exportadas do diagrama de classes e do DER, e capturas dos protótipos.

## Capa

FIAP — Faculdade de Informática e Administração Paulista

Engenharia de Software — 2º ano

Disciplina: Domain Driven Design — Java

Projeto: ReAlimenta SP

API de publicação, reserva, coleta e entrega de alimentos excedentes

Integrantes: PENDENTE — nome e RM

Professor(a): PENDENTE

Cidade: São Paulo

Ano: 2026

Repositório GitHub: PENDENTE

URL no Render: PENDENTE — a aplicação não foi publicada

## Índice

1. Descrição
2. Justificativa
3. Objetivos
4. Arquitetura
5. Modelos e banco
6. Regras e limitações do negócio
7. Instruções de execução
8. Endpoints
9. Protótipos
10. Testes e verificação
11. Limitações e decisões para a apresentação
12. Integração com Database Design

## 1. Descrição

O ReAlimenta SP conecta estabelecimentos com alimento excedente a instituições sociais. Esta API controla quatro momentos: a publicação do lote, a reserva por uma instituição, a coleta e a confirmação da entrega.

O módulo deste repositório é somente a API. O modelo de tabelas deve ser o mesmo usado pelo responsável de Database Design. Nomes de tabelas, colunas, tipos e constraints estão alinhados entre o Java, os scripts SQL e o DER em `docs/der.mmd`.

Os dados incluídos são fictícios. Não houve entrevista, termo de uso nem medição de impacto com pessoas atendidas.

## 2. Justificativa

O desperdício e a demanda por doação acontecem ao mesmo tempo, mas em cadastros separados. Um fluxo único reduz a ambiguidade: o alimento deixa de ser uma mensagem solta e passa a ter estado, prazo e quantidade.

O indicador acompanha o que o sistema realmente registra. Quilo ofertado é a soma das ofertas cadastradas. Quilo entregue é a soma confirmada na entrega. Reserva e coleta não são apresentadas como se a comida já tivesse chegado.

## 3. Objetivos

- Publicar e consultar lotes de alimento excedente.
- Reservar cada lote, por inteiro, para uma única instituição.
- Impedir reserva duplicada, inclusive quando duas requisições chegam juntas.
- Registrar coleta e entrega somente na ordem permitida e dentro do prazo.
- Cancelar a reserva antes da coleta, sem apagar o histórico.
- Expor indicadores honestos.
- Tratar erros de forma única, sem devolver SQL, stack trace ou credencial.

## 4. Arquitetura

A API usa Spring Boot 4.1.2 e Java 21. A documentação oficial de 11/11/2026 indica que o Spring Boot 4.1.2 exige Java 17 ou superior e aceita Java 21. O starter de web adotado é `spring-boot-starter-webmvc`, porque `spring-boot-starter-web` está depreciado nessa versão. Os dois sobem Spring MVC. A escolha está registrada para a banca não interpretar o nome novo como outro framework.

Pacotes:

| Pacote | Responsabilidade |
| --- | --- |
| `model` | Entidades e enums |
| `dto` | Entrada e saída da API |
| `dao` | SQL e `JdbcTemplate` |
| `bo` | Regra de negócio e `@Transactional` |
| `controller` | HTTP |
| `exception` | Exceções de domínio e `RestControllerAdvice` |
| `config` | `Clock` no fuso de São Paulo |

O controller não decide se um lote pode ser reservado. Ele valida o formato do JSON com Bean Validation, chama o BO e devolve o status HTTP. O BO chama o DAO. O DAO não conhece regra de transição.

O `JdbcTemplate` foi escolhido neste projeto para o SQL ficar explícito na disciplina. Não é uma exigência do enunciado e não deve ser apresentado como se fosse.

`ConexaoInicial` abre uma conexão quando a aplicação termina de subir. Sem Oracle alcançável, o processo encerra. O log do servidor pode mostrar o erro do driver. A resposta HTTP, quando a falha acontece durante uma requisição, continua genérica.

Não há Lombok, microsserviço, banco em memória nem classe genérica de repositório.

Fluxo de uma reserva:

```text
POST /api/doacoes
        |
        v
DoacaoController
        |
        v
DoacaoBO.reservar   <-- transação única
        |
        +--> InstituicaoDAO.buscarPorId
        +--> LoteAlimentoDAO.buscarPorIdParaAtualizacao   (SELECT ... FOR UPDATE)
        +--> DoacaoDAO.buscarPorLoteId
        +--> DoacaoDAO.inserir
        +--> LoteAlimentoDAO.atualizarStatus
        |
        v
Oracle: DOACAO e LOTE_ALIMENTO mudam juntos ou voltam juntos
```

A trava `FOR UPDATE` segura a linha do lote até o fim da transação. A atualização cadastral usa a mesma trava antes de aceitar quantidade, estabelecimento ou prazo. Assim uma reserva não fica com o estado antigo que a edição leu sem bloqueio. A edição não trava a doação. Coleta e entrega travam a doação e só depois o lote, então as duas operações não invertem a ordem dos bloqueios.

A constraint `UK_DOACAO_LOTE` continua sendo a rede de segurança se duas sessões ainda assim tentarem gravar a mesma doação. O código de erro Oracle 1 vira resposta 409, sem mostrar o SQL.

## 5. Modelos e banco

Entidades: `Estabelecimento`, `Instituicao`, `LoteAlimento`, `Doacao`.

O identificador Java `estabelecimentoId` corresponde à coluna `ESTABELECIMENTO_ID`. A convenção é camelCase no Java e palavra em maiúsculas, separada por sublinhado, no Oracle. Os scripts não usam aspas nos nomes, então o Oracle guarda os identificadores em maiúsculas.

Quantidades são `BigDecimal` no Java e `NUMBER(10,3)` no banco.

Datas de negócio são `LocalDateTime`. O fuso da aplicação é `America/Sao_Paulo`. O JSON não traz deslocamento; o valor `2026-11-13T18:00:00` significa 18 horas em São Paulo. A coluna é `TIMESTAMP(6)`. No Oracle, `TIMESTAMP` sem precisão já significa 6 dígitos fracionários. O script explicita `TIMESTAMP(6)` para o colega de banco. Quem já criou a tabela com o script anterior não precisa de migração: a precisão padrão já era essa. Nenhum dado existente é alterado por este repositório.

O BO corta o prazo em microssegundos na entrada, na comparação e na gravação. Uma diferença só nos nanossegundos seguintes não conta como mudança e o prazo gravado permanece o original. Uma diferença de um microssegundo, ainda dentro do mesmo segundo, conta como mudança e continua bloqueada quando o lote saiu de `DISPONIVEL`. `DatasJdbc` converte com `Timestamp.valueOf` e `Timestamp.toLocalDateTime`, sem passar por UTC. A JVM é fixada nesse fuso na inicialização e na imagem Docker. O script SQL também executa `ALTER SESSION SET TIME_ZONE`.

O instante dos erros da API é `OffsetDateTime`, porque a resposta de erro informa quando a falha ocorreu e o deslocamento de São Paulo.

Sequences: `SEQ_ESTABELECIMENTO`, `SEQ_INSTITUICAO`, `SEQ_LOTE_ALIMENTO` e `SEQ_DOACAO`, todas iniciando em 5. Os dados fictícios usam os ids 1 a 4 de propósito, para o roteiro poder citar esses números.

Índices:

| Índice | Motivo |
| --- | --- |
| `UK_DOACAO_LOTE` | No máximo uma doação por lote, também sob concorrência |
| `IDX_LOTE_STATUS_PRAZO` | Filtro de lote disponível e dentro do prazo |
| `IDX_LOTE_ESTABELECIMENTO` | Histórico antes de excluir o estabelecimento e apoio da chave estrangeira |
| `IDX_DOACAO_INSTITUICAO` | Histórico da instituição e indicador de instituições distintas |
| `UK_ESTABELECIMENTO_EMAIL` e `UK_INSTITUICAO_EMAIL` | E-mail único em cada cadastro |

O DER está em `docs/der.mmd`. O diagrama de classes está em `docs/diagrama-classes.puml`. Os dois devem ser exportados como figura no PDF. O modelo deve ser conferido com o responsável de Database Design antes da entrega conjunta.

## 6. Regras e limitações do negócio

- Quantidade ofertada maior que zero.
- Lote novo com prazo futuro.
- Estabelecimento e instituição citados precisam existir.
- Reserva somente com lote `DISPONIVEL` e prazo ainda não alcançado.
- Coleta somente a partir de doação `RESERVADA`, e somente dentro do prazo.
- Entrega somente depois da coleta, dentro do prazo, com quantidade positiva e não superior à ofertada.
- Cancelamento somente antes da coleta. A doação fica `CANCELADA` e o lote fica `CANCELADO`.
- PUT de lote não altera status. Quantidade, doador e prazo não mudam depois que o lote sai de `DISPONIVEL`.
- Não se exclui estabelecimento ou instituição com histórico.
- Lote só é apagado fisicamente se não tiver doação. Doação não tem DELETE.

Limitação do MVP: o lote não é dividido. Para ofertar de novo depois de um cancelamento, cria-se outro lote. O prazo não substitui avaliação de segurança alimentar.

## 7. Instruções de execução

Ferramentas: JDK 21, Maven Wrapper do repositório e SQL Developer, ou ferramenta equivalente, em UTF-8. Uma conta no Render só entra quando o grupo for publicar. O PDF desta documentação, o repositório GitHub e o deploy ainda não estão concluídos.

O Spring não lê o arquivo `.env`. As variáveis precisam existir no processo.

| Variável | Uso |
| --- | --- |
| `DB_URL` | JDBC Oracle, por exemplo `jdbc:oracle:thin:@//host:1521/servico` |
| `DB_USERNAME` | Usuário do schema da aplicação |
| `DB_PASSWORD` | Senha desse usuário |
| `PORT` | Porta HTTP. Fora do Render, o padrão é 8080 |
| `DB_IT_URL` | JDBC de um schema exclusivo de teste |
| `DB_IT_USERNAME` | Usuário desse schema, diferente de `DB_USERNAME` |
| `DB_IT_PASSWORD` | Senha do schema de teste |
| `DB_IT_SCHEMA_EXCLUSIVO` | Só `true` autoriza o teste de integração |

`.env.example` mostra os nomes e não contém senha utilizável.

Ordem manual dos scripts, no schema escolhido, sem execução automática por este repositório:

1. `src/main/resources/db/01_estrutura.sql`
2. `src/main/resources/db/02_dados_demonstracao.sql`
3. `src/main/resources/db/03_limpeza_manual.sql` somente quando alguém decidir apagar os dados

Comandos, no diretório do projeto:

```powershell
$env:JAVA_HOME = "C:\caminho\do\jdk-21"
$env:DB_URL = "jdbc:oracle:thin:@//host:1521/servico"
$env:DB_USERNAME = "usuario"
$env:DB_PASSWORD = "senha"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

`.\mvnw.cmd test` não conecta no Oracle. A integração separada está na seção 10 e só roda com schema exclusivo.

Verificação local:

```powershell
Invoke-RestMethod http://localhost:8080/api/saude
```

A resposta esperada contém `UP`, `ReAlimenta SP` e `America/Sao_Paulo`. Ela não prova que o banco está acessível.

## 8. Endpoints

Códigos: 201 na criação, com cabeçalho `Location`; 200 na consulta e na atualização; 204 na exclusão; 400 para JSON inválido ou Bean Validation; 404 para recurso inexistente; 409 para conflito de negócio ou e-mail duplicado; 500 para falha inesperada, sem SQL, senha ou stack trace. Campo JSON desconhecido é rejeitado. Data de negócio sai como texto ISO-8601, não como vetor numérico.

| Método | Caminho | Função |
| --- | --- | --- |
| GET | `/api/estabelecimentos` | Lista estabelecimentos |
| GET | `/api/estabelecimentos/{id}` | Busca um estabelecimento |
| POST | `/api/estabelecimentos` | Cria estabelecimento |
| PUT | `/api/estabelecimentos/{id}` | Atualiza estabelecimento |
| DELETE | `/api/estabelecimentos/{id}` | Exclui se não houver lote |
| GET | `/api/instituicoes` | Lista instituições |
| GET | `/api/instituicoes/{id}` | Busca uma instituição |
| POST | `/api/instituicoes` | Cria instituição |
| PUT | `/api/instituicoes/{id}` | Atualiza instituição |
| DELETE | `/api/instituicoes/{id}` | Exclui se não houver doação |
| GET | `/api/lotes` | Lista lotes. `?disponiveis=true` restringe a `DISPONIVEL` e prazo futuro |
| GET | `/api/lotes/{id}` | Busca um lote |
| POST | `/api/lotes` | Cria lote |
| PUT | `/api/lotes/{id}` | Atualiza cadastro. Não muda status |
| DELETE | `/api/lotes/{id}` | Exclui só lote sem doação e ainda `DISPONIVEL` |
| GET | `/api/doacoes` | Lista doações |
| GET | `/api/doacoes/{id}` | Busca uma doação |
| POST | `/api/doacoes` | Reserva o lote inteiro e devolve o id da nova doação |
| PUT | `/api/doacoes/{id}/coleta` | Registra a coleta dessa doação |
| PUT | `/api/doacoes/{id}/entrega` | Registra a entrega e a quantidade recebida |
| PUT | `/api/doacoes/{id}/cancelamento` | Cancela antes da coleta |
| GET | `/api/indicadores` | `kgOfertados`, `kgEntregues`, `doacoesEntregues`, `instituicoesAtendidas` |
| GET | `/api/saude` | Saúde do processo, sem credencial nem dado de negócio |

Corpo mínimo da reserva:

```json
{ "loteId": 1, "instituicaoId": 1 }
```

A resposta traz `id`. Coleta e entrega usam esse id, não o id 1 da carga fictícia. Exemplos completos de cadastro estão em `docs/endpoints.md`. O roteiro copiável está em `docs/roteiro-demonstracao.md`.

## 9. Protótipos

O arquivo `docs/prototipos.html` mostra quatro telas:

- cadastro de lote;
- consulta e reserva;
- acompanhamento e confirmação de entrega;
- painel de indicadores.

São demonstração visual. O botão apenas revela um aviso de que nada foi enviado. Não há integração com a API. As capturas para o PDF devem ser feitas nesse arquivo, em navegador.

## 10. Testes e verificação

Há três grupos, e eles não se substituem:

| Grupo | O que prova | Como executar |
| --- | --- | --- |
| Unitário | Regra de negócio com DAO simulado e relógio fixo em 11/11/2026 12:00, horário de São Paulo | `.\mvnw.cmd test` |
| Web | HTTP com MockMvc e BO simulado, sem Oracle | O mesmo comando. Classes `ApiHttpTest` e `ApiHttpJacksonTest` |
| Integração Oracle | Duas reservas simultâneas, edição esperando a reserva e rollback entre doação e lote | Só com schema exclusivo: `.\mvnw.cmd -Poracle-it test` |

O comando padrão exclui `*IT.java`. O perfil `oracle-it` executa somente essa classe. Ela exige `DB_IT_SCHEMA_EXCLUSIVO=true` e um usuário diferente de `DB_USERNAME`. Ela apaga apenas as linhas que ela mesma criou e recusa o schema compartilhado do grupo. Não usa H2. Não executa os scripts de estrutura.

Resultado real de 11/11/2026, 19:01, horário de São Paulo, com `.\mvnw.cmd test`:

```text
Compiling 48 source files with javac [debug parameters release 21]
Compiling 10 source files with javac [debug parameters release 21]
Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Desses 46, 33 são unitários de regra, exceção e instanciação direta de controller. Os outros 13 passam pelo MockMvc. A instanciação direta antiga não substitui esses 13. A classe `FluxoOracleIT` compilou e não foi executada: não havia credencial nem schema exclusivo. O JDK do processo foi o OpenJDK 25.0.2. O bytecode foi gerado com `release 21`. Isso não é execução comprovada em um JDK 21.

Nenhum outro banco foi usado para afirmar que o Oracle foi validado. PDF, GitHub e Render continuam pendentes.

## 11. Limitações e decisões para a apresentação

Estas decisões precisam ser explicadas com as próprias palavras do grupo:

1. A camada DAO é explícita de propósito. `JdbcTemplate` não é “o jeito obrigatório” da disciplina; é a forma escolhida para mostrar o SQL.
2. A transação fica no BO, no método público. Se um método do próprio BO chamasse outro método do próprio BO, a anotação interna não abriria outra transação. Os controllers chamam o BO de fora, então a transação funciona.
3. O lote cancelado não volta ao cardápio. A nova oferta é outro insert.
4. `kgOfertados` inclui lote cancelado, reservado, coletado e entregue. Dizer que esse número é comida salva estaria errado.
5. O protótipo não tem login. Antes de qualquer uso real, é obrigatório controle de acesso e dados que não sejam fictícios.
6. O grupo ainda precisa preencher nomes, RMs, bairro, URL do GitHub e, se conseguir publicar, a URL real do Render. Não inventar esses campos.
7. A conectividade do Oracle da FIAP a partir do Render depende de rede e de credencial. Sem esse teste, a apresentação deve dizer que o deploy está preparado e não que está no ar.
8. Os scripts SQL são uma proposta para alinhar com Database Design. Não são o modelo aprovado pelo grupo.
9. O teste de integração Oracle está escrito e não foi executado.

## 12. Integração com Database Design

Os scripts deste repositório são uma proposta para discussão com o colega de Database Design. O Java usa exatamente as tabelas e colunas abaixo. Não há outras tabelas.

### ESTABELECIMENTO

| Coluna | Tipo | Restrição |
| --- | --- | --- |
| `ESTABELECIMENTO_ID` | `NUMBER(19)` | PK |
| `NOME` | `VARCHAR2(120)` | NOT NULL |
| `TIPO` | `VARCHAR2(30)` | NOT NULL, CHECK em RESTAURANTE, MERCADO, PADARIA, FEIRA, OUTRO |
| `EMAIL` | `VARCHAR2(160)` | NOT NULL, UNIQUE |
| `TELEFONE` | `VARCHAR2(20)` | NOT NULL |
| `ENDERECO` | `VARCHAR2(180)` | NOT NULL |
| `BAIRRO` | `VARCHAR2(80)` | NOT NULL |
| `CIDADE` | `VARCHAR2(80)` | NOT NULL, default SAO PAULO |

Sequence usada pelo DAO: `SEQ_ESTABELECIMENTO`.

### INSTITUICAO

Mesmas colunas de contato, sem `TIPO`. PK `INSTITUICAO_ID NUMBER(19)`. E-mail UNIQUE. Sequence: `SEQ_INSTITUICAO`.

### LOTE_ALIMENTO

| Coluna | Tipo | Restrição |
| --- | --- | --- |
| `LOTE_ID` | `NUMBER(19)` | PK |
| `ESTABELECIMENTO_ID` | `NUMBER(19)` | NOT NULL, FK para ESTABELECIMENTO |
| `DESCRICAO` | `VARCHAR2(240)` | NOT NULL |
| `CATEGORIA` | `VARCHAR2(30)` | NOT NULL, CHECK em PREPARADO, IN_NATURA, PADARIA, LATICINIO, MERCEARIA, OUTRO |
| `QUANTIDADE_KG` | `NUMBER(10,3)` | NOT NULL, CHECK maior que zero |
| `PRAZO_APROVEITAMENTO` | `TIMESTAMP(6)` | NOT NULL |
| `STATUS` | `VARCHAR2(20)` | NOT NULL, CHECK em DISPONIVEL, RESERVADO, COLETADO, ENTREGUE, CANCELADO |

Sequence: `SEQ_LOTE_ALIMENTO`. Índice `IDX_LOTE_STATUS_PRAZO` apoia o filtro de oferta aberta. `IDX_LOTE_ESTABELECIMENTO` apoia a FK e o histórico.

### DOACAO

| Coluna | Tipo | Restrição |
| --- | --- | --- |
| `DOACAO_ID` | `NUMBER(19)` | PK |
| `LOTE_ID` | `NUMBER(19)` | NOT NULL, FK, UNIQUE |
| `INSTITUICAO_ID` | `NUMBER(19)` | NOT NULL, FK |
| `DATA_RESERVA` | `TIMESTAMP(6)` | NOT NULL |
| `DATA_COLETA` | `TIMESTAMP(6)` | nula até a coleta |
| `DATA_ENTREGA` | `TIMESTAMP(6)` | nula até a entrega |
| `QUANTIDADE_ENTREGUE_KG` | `NUMBER(10,3)` | nula até a entrega, CHECK maior que zero |
| `STATUS` | `VARCHAR2(20)` | NOT NULL, CHECK em RESERVADA, COLETADA, ENTREGUE, CANCELADA |

Sequence: `SEQ_DOACAO`. `UK_DOACAO_LOTE` impede duas doações para o mesmo lote. `IDX_DOACAO_INSTITUICAO` apoia a FK e o indicador.

### Tempo, sequences e ordem

`TIMESTAMP(6)` guarda microssegundos. O Java normaliza o prazo nessa precisão antes de comparar e gravar. Não há conversão para UTC. As quatro sequences começam em 5 para não colidir com os ids 1 a 4 da carga fictícia.

Ordem de execução: `01_estrutura.sql`, depois `02_dados_demonstracao.sql`. `03_limpeza_manual.sql` é opcional, manual e destrutivo. Nenhum deles é executado pela aplicação.

### Carga atual

A carga de demonstração tem 2 estabelecimentos, 2 instituições, 4 lotes e 2 doações. Se essa carga for reaproveitada na entrega de Database Design, ela não atende sozinha ao mínimo de cinco registros por tabela previsto naquela disciplina. Ampliar a carga exige ajustar as sequences e os números esperados dos indicadores e do roteiro. Isso não foi feito aqui.
