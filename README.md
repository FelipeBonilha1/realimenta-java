# ReAlimenta SP

## Ficha da CitizenTech

| Campo | Conteúdo |
| --- | --- |
| Nome | ReAlimenta SP |
| Cidade / bairro | São Paulo, bairro Bela Vista |
| Cidadão beneficiado | Pessoas atendidas por instituições sociais que podem receber alimento excedente de estabelecimentos. Não há entrevista, termo de consentimento nem medição com usuários reais neste repositório. |
| Problema | Estabelecimentos descartam alimento ainda aproveitável enquanto instituições sociais procuram doação. Falta um fluxo simples para publicar o excedente, reservá-lo por inteiro, registrar a coleta e confirmar o que foi entregue. |
| Funcionamento | O estabelecimento publica um lote. Uma instituição reserva o lote inteiro. A coleta e a entrega são confirmadas em etapas. O cancelamento, antes da coleta, encerra a doação e o lote. Os indicadores mostram quilos registrados na oferta, quilos entregues, doações entregues e instituições distintas que receberam entrega. |
| Responsabilidade dos módulos | Este repositório é o módulo da API Java. O schema Oracle do grupo foi criado e populado por Database Design. Interface de uso real, autenticação e aplicativo não estão neste escopo. Equipe: Felipe Bonilha (RM562356), Felipe Rodrigues (RM565341), Joan Campos (RM562913) |
| Repositório | https://github.com/FelipeBonilha1/realimenta-java |
| Indicador | Quilos registrados nas ofertas, quilos efetivamente entregues, número de doações entregues e número de instituições distintas com entrega. Não há equivalência em refeições, água ou CO2. Não há resultado de impacto medido em campo. |
| ODS | ODS 2 — Fome zero e agricultura sustentável. ODS 12 — Consumo e produção responsáveis. A relação é de enquadramento do problema. Não é um resultado comprovado. |

Integrantes e RMs: Felipe Bonilha (RM562356), Felipe Rodrigues (RM565341), Joan Campos (RM562913).

Professor: Rafael Desiderio.

URL de deploy: https://realimenta-java.onrender.com

---

API REST do fluxo de doação de alimentos excedentes. O código usa Spring Boot 4.1.2, Java 21 e JDBC explícito com `JdbcTemplate`. O `JdbcTemplate` é uma escolha deste projeto para deixar o SQL visível na camada DAO. O enunciado da disciplina não exige essa tecnologia específica.

Este arquivo e os Markdown em `docs/` são a base para o PDF acadêmico. Eles não substituem o PDF único pedido na entrega da faculdade.

## O que a API faz

1. Cadastra estabelecimentos e instituições.
2. Publica um lote com quantidade e prazo.
3. Reserva o lote inteiro para uma instituição.
4. Registra coleta, entrega ou cancelamento antes da coleta.
5. Expõe indicadores sem tratar reserva ou coleta como entrega.

Cada lote tem no máximo uma doação. Não há divisão entre instituições. O cancelamento não reabre o lote: uma nova oferta é um novo lote.

## Versões

| Ferramenta | Versão neste projeto |
| --- | --- |
| Java alvo do bytecode | 21 (`release 21`) |
| Spring Boot | 4.1.2 |
| Maven Wrapper | 3.9.11, gerado pelo Wrapper 3.3.2 |
| Spring Web | `spring-boot-starter-webmvc` |
| Validação | Jakarta Bean Validation, via `spring-boot-starter-validation` |
| Persistência | Spring JDBC `JdbcTemplate` e HikariCP |
| Driver Oracle | `com.oracle.database.jdbc:ojdbc17`, versão gerenciada pelo BOM do Spring Boot |
| Testes | JUnit Jupiter e Mockito, via `spring-boot-starter-test` |

A versão 4.1.2 foi conferida na documentação oficial em 11/11/2026: Spring Boot 4.1.2 exige no mínimo Java 17 e aceita até Java 27. O starter `spring-boot-starter-web` consta como depreciado em favor de `spring-boot-starter-webmvc`, que continua sendo Spring MVC com Tomcat.

Referências:

- https://docs.spring.io/spring-boot/system-requirements.html
- https://docs.spring.io/spring-boot/reference/using/build-systems.html

O driver `ojdbc17` é o artefato certificado pela Oracle para JDK 17 e JDK 21 e é o artefato apontado pelo JDBC do Spring Boot 4. A versão exata resolvida pelo Maven fica registrada abaixo, depois do build.

Versão resolvida do `ojdbc17` no build de 11/11/2026: **23.26.3.0.0**, vinda do BOM do Spring Boot 4.1.2.

## Convenção de data e hora

- A aplicação usa `America/Sao_Paulo`.
- Desde 2019 esse fuso permanece em UTC−3.
- `LocalDateTime` representa a hora civil, sem deslocamento no JSON.
- A coluna Oracle é `TIMESTAMP(6)`, sem fuso próprio. O prazo é cortado em microssegundos na entrada, na comparação e na gravação.
- A conversão fica em `DatasJdbc` e usa `Timestamp.valueOf`, sem transformar o valor para UTC.
- O prazo é um controle operacional do protótipo. Não certifica segurança alimentar.
- Um prazo igual ao instante atual já está fora da janela.

Para o JDBC e o Oracle enxergarem o mesmo relógio, a JVM precisa estar em `America/Sao_Paulo`. A classe principal define esse fuso antes de subir o Spring. A imagem Docker repete a configuração.

## Como executar

Pré-requisitos na máquina de desenvolvimento:

- JDK capaz de compilar `release 21` (JDK 21 recomendado; nesta máquina, em 19/11/2026, o PATH trazia JDK 17 e o build usou o JBR 25 do Android Studio com bytecode 21).
- Acesso de rede ao Maven Central na primeira execução.
- Schema Oracle do grupo já criado no ambiente FIAP (não rode scripts de carga neste schema compartilhado sem alinhar com Database Design).
- Variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` (senha só no ambiente ou em entrada protegida; nunca no repositório).

O Spring Boot não lê `.env` automaticamente. O arquivo `.env.example` é só um lembrete, sem senha real.

Forma recomendada no Windows (senha pedida no `Get-Credential`, sem gravar em disco):

```powershell
.\scripts\run-oracle-local.ps1
```

O script define `DB_URL` e `DB_USERNAME` para o Oracle FIAP (SID `ORCL`), opcionalmente ajusta `JAVA_HOME` para o JBR se o PATH tiver só JDK 17, e executa `.\mvnw.cmd spring-boot:run`.

Alternativa manual, na pasta do projeto:

```powershell
$env:DB_URL = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
$env:DB_USERNAME = "seu_usuario_rm"
# DB_PASSWORD: definir nesta sessão, sem commitar
$env:PORT = "8080"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

As variáveis do PowerShell valem apenas para aquela janela. Para gravá-las no usuário do Windows:

```powershell
[System.Environment]::SetEnvironmentVariable("DB_URL", "jdbc:oracle:thin:@//HOST:1521/SERVICO", "User")
[System.Environment]::SetEnvironmentVariable("DB_USERNAME", "USUARIO", "User")
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "SENHA", "User")
```

Feche e abra o terminal depois desse comando. Não commite a senha.

No Cursor ou no IntelliJ, abra a configuração de execução de `RealimentaApplication` e preencha as mesmas variáveis no painel de ambiente. Não coloque a senha em `application.properties`.

Formas comuns de URL, a confirmar com a FIAP e com Database Design:

```text
jdbc:oracle:thin:@//host:1521/nome_do_servico
jdbc:oracle:thin:@host:1521:SID
```

A senha não entra no repositório. Host, porta e SID do ambiente acadêmico constam em `.env.example` e no script `scripts/run-oracle-local.ps1`.

### Oracle

O Spring **não** executa scripts (`spring.sql.init.mode=never`). Os arquivos em `src/main/resources/db/` servem para alinhar com Database Design ou para um schema vazio de laboratório:

1. `01_estrutura.sql`
2. `02_dados_demonstracao.sql`
3. `03_limpeza_manual.sql` — só uso consciente; apaga dados

No schema do grupo (FIAP), a carga já foi aplicada pelo responsável de Database Design. **Não** recrie nem limpe esse schema pela API desta etapa.

Conexão da aplicação ao Oracle FIAP foi verificada em 19/11/2026: log de `ConexaoInicial`, leitura dos cadastros e `GET /api/indicadores` com **87** kg ofertados, **9,5** kg entregues, **1** doação entregue e **1** instituição com entrega (referência: 5 estabelecimentos, 5 instituições, 7 lotes, 5 doações). `GET /api/lotes?disponiveis=true` pode retornar lista vazia se todos os prazos já venceram.

Metadados de sequences (somente leitura, sem `NEXTVAL`): `.\scripts\consultar-sequences-oracle.ps1`.

### Testes

```powershell
.\mvnw.cmd test
```

`.\mvnw.cmd test` executa testes unitários e testes web com MockMvc. Os dois usam simulação e não abrem Oracle. Em 11/11/2026, às 19:01, esse comando compilou com `release 21` no OpenJDK 25.0.2 e concluiu 46 testes, 0 falhas. Isso não é execução comprovada em um JDK 21.

A integração Oracle fica de fora desse comando. Ela só roda com schema exclusivo e usuário diferente do schema do grupo:

```powershell
$env:DB_IT_URL = "jdbc:oracle:thin:@//HOST:1521/SERVICO"
$env:DB_IT_USERNAME = "USUARIO_EXCLUSIVO_DE_TESTE"
$env:DB_IT_PASSWORD = "SENHA"
$env:DB_IT_SCHEMA_EXCLUSIVO = "true"
.\mvnw.cmd -Poracle-it test
```

Esse perfil não foi executado nesta revisão. Não existe H2 nem outro banco no lugar do Oracle.

## Fluxo para demonstrar

Com a API no ar e os dados de demonstração carregados:

1. `GET /api/saude`
2. `GET /api/lotes?disponiveis=true` — o lote 1 entra; o lote 2 está vencido e fica de fora.
3. `POST /api/doacoes` com `{"loteId":1,"instituicaoId":1}` e guarde o `id` da resposta.
4. Repetir o POST e observar 409.
5. `PUT /api/doacoes/{id}/coleta` usando o id do passo 3. A doação 1 da carga é outra reserva e não entra neste fluxo.
6. `PUT /api/doacoes/{id}/entrega` com quantidade menor ou igual à ofertada, no mesmo id.
7. `GET /api/indicadores`

O passo a passo com comandos está em `docs/roteiro-demonstracao.md`.

## GitHub

Repositório: https://github.com/FelipeBonilha1/realimenta-java

Confira que `.env` não existe ou não foi adicionado. `.gitignore` já o ignora. Não commite senha Oracle.

## Render

O `Dockerfile` na raiz compila com **Eclipse Temurin JDK 21**, roda `mvnw package` (testes unitários e HTTP com mock, sem Oracle) e executa `target/realimenta-sp.jar` em **JRE 21**. O Tomcat escuta em `${PORT:8080}`.

O `render.yaml` declara um Web Service **Docker** no plano **gratuito**, health check `/api/saude`, `DB_URL`/`DB_USERNAME` fixos e `DB_PASSWORD` com `sync: false` (valor só no painel).

### Passo a passo no painel

1. [dashboard.render.com](https://dashboard.render.com) → **New** → **Blueprint** (com `render.yaml` no repositório) **ou** **Web Service** conectado a `FelipeBonilha1/realimenta-java`, branch **main**.
2. **Runtime:** Docker. **Root Directory:** *(vazio)*. **Dockerfile Path:** `./Dockerfile`. **Docker Command:** deixar o `ENTRYPOINT` do Dockerfile. **Health Check Path:** `/api/saude`.
3. Variáveis:

| Variável | Valor |
| --- | --- |
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USERNAME` | `rm565341` |
| `DB_PASSWORD` | Secret no painel (não commitar) |

Não crie PostgreSQL neste projeto. `PORT` é injetada pelo Render.

4. Validação pública após deploy:

```text
GET https://<nome-do-servico>.onrender.com/api/saude
GET https://<nome-do-servico>.onrender.com/api/estabelecimentos
GET https://<nome-do-servico>.onrender.com/api/indicadores
```

`/api/saude` sozinho não prova Oracle. `GET /api/estabelecimentos` com HTTP 200 e JSON confirma leitura no schema do grupo.

5. Se o deploy falhar na subida, leia os logs do Render: credencial, SID JDBC, DNS ou bloqueio de IP externo ao Oracle FIAP são causas possíveis — não inferir sem o log.

### Docker local (opcional)

```powershell
docker build -t realimenta-sp .
docker run --rm -p 8080:8080 -e PORT=8080 -e DB_URL -e DB_USERNAME -e DB_PASSWORD realimenta-sp
```

## Documentos

| Arquivo | Uso |
| --- | --- |
| `docs/documentacao-java.md` | Base do PDF acadêmico |
| `docs/endpoints.md` | Tabela e exemplos de endpoints |
| `docs/diagrama-classes.puml` | Diagrama de classes |
| `docs/der.mmd` | DER |
| `docs/prototipos.html` | Telas estáticas |
| `docs/roteiro-demonstracao.md` | Roteiro de reserva, coleta e entrega |
| `docs/checklist-entrega.md` | Conferência dos requisitos |
| `prompts.md` | Pedido que originou o projeto e espaço para as próximas interações |
| `scripts/run-oracle-local.ps1` | Sobe a API com JDBC FIAP e senha protegida |
| `scripts/consultar-sequences-oracle.ps1` | Consulta MAX(ID) e `USER_SEQUENCES` (somente leitura) |
| `render.yaml` | Blueprint do Web Service Docker no Render |

Para exportar imagens ao PDF: abra o `.puml` em https://www.plantuml.com/plantuml e o `.mmd` em https://mermaid.live. As imagens não estão geradas neste repositório.

## Limitações que precisam aparecer na apresentação

- Protótipo com dados fictícios. Sem autenticação. Não usar com dado real de estabelecimento ou instituição.
- O lote é indivisível e aceita uma única doação.
- Cancelar não devolve o lote para `DISPONIVEL`.
- O prazo não é laudo sanitário.
- Exclusão física de doação não existe.
- Estabelecimento com lote, instituição com doação e lote com doação não são apagados.
- O comando `.\mvnw.cmd test` não abre Oracle (MockMvc e simulação). A API em execução (`spring-boot:run`) conectou ao Oracle FIAP em 19/11/2026.
- O perfil `oracle-it` (`FluxoOracleIT`) não foi executado no schema do grupo; exige usuário e schema exclusivos.
- O deploy no Render ainda não foi executado.
