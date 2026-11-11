# ReAlimenta SP

## Ficha da CitizenTech

| Campo | Conteúdo |
| --- | --- |
| Nome | ReAlimenta SP |
| Cidade / bairro | São Paulo. Bairro de atuação da equipe: **PENDENTE** |
| Cidadão beneficiado | Pessoas atendidas por instituições sociais que podem receber alimento excedente de estabelecimentos. Não há entrevista, termo de consentimento nem medição com usuários reais neste repositório. |
| Problema | Estabelecimentos descartam alimento ainda aproveitável enquanto instituições sociais procuram doação. Falta um fluxo simples para publicar o excedente, reservá-lo por inteiro, registrar a coleta e confirmar o que foi entregue. |
| Funcionamento | O estabelecimento publica um lote. Uma instituição reserva o lote inteiro. A coleta e a entrega são confirmadas em etapas. O cancelamento, antes da coleta, encerra a doação e o lote. Os indicadores mostram quilos registrados na oferta, quilos entregues, doações entregues e instituições distintas que receberam entrega. |
| Responsabilidade dos módulos | Este repositório é o módulo da API Java. O desenho físico do banco deve ser compartilhado com o responsável de Database Design. Interface de uso real, autenticação e aplicativo não estão neste escopo. Responsáveis nominais: **PENDENTE** |
| Repositório | **PENDENTE** |
| Indicador | Quilos registrados nas ofertas, quilos efetivamente entregues, número de doações entregues e número de instituições distintas com entrega. Não há equivalência em refeições, água ou CO2. Não há resultado de impacto medido em campo. |
| ODS | ODS 2 — Fome zero e agricultura sustentável. ODS 12 — Consumo e produção responsáveis. A relação é de enquadramento do problema. Não é um resultado comprovado. |

Integrantes e RMs: **PENDENTE**

URL de deploy: **PENDENTE**. A aplicação não foi publicada nesta entrega.

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

- JDK 21 instalado e visível no terminal (`java -version`).
- Nesta máquina, em 11/11/2026, o único JDK encontrado foi o OpenJDK 25.0.2 do Android Studio. O build usou esse JDK com `--release 21`. O bytecode gerado é de Java 21. Instale um JDK 21 se a disciplina exigir que o `java -version` mostre 21.
- Acesso de rede ao Maven Central na primeira execução.
- Schema Oracle com os scripts aplicados.
- Variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.

O Spring Boot não lê `.env` automaticamente. O arquivo `.env.example` é só um lembrete, sem senha real.

PowerShell, na pasta do projeto:

```powershell
$env:DB_URL = "jdbc:oracle:thin:@//HOST:1521/SERVICO"
$env:DB_USERNAME = "USUARIO"
$env:DB_PASSWORD = "SENHA"
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

Não há host, usuário nem senha verdadeiros neste repositório.

### Oracle

Execute manualmente, em UTF-8, nesta ordem:

1. `src/main/resources/db/01_estrutura.sql`
2. `src/main/resources/db/02_dados_demonstracao.sql`

O arquivo `03_limpeza_manual.sql` apaga os dados. A aplicação não o executa. A seção de `DROP` está comentada.

Os scripts não são disparados pelo Spring (`spring.sql.init.mode=never`). Eles são uma proposta para alinhar com Database Design, não o modelo aprovado pelo grupo.

Com os dados fictícios intactos, `GET /api/indicadores` tende a responder 39,500 kg ofertados, 9,500 kg entregues, 1 doação entregue e 1 instituição. A carga tem 2 estabelecimentos, 2 instituições, 4 lotes e 2 doações. Se ela for reaproveitada na entrega de Database, aquele trabalho pede pelo menos cinco registros por tabela. Os prazos da demonstração são relativos a `SYSTIMESTAMP`, então continuam válidos depois da data em que o script foi escrito.

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

Repositório: **PENDENTE**. Nada foi enviado a um remoto durante esta implementação.

Quando o grupo criar o repositório:

```powershell
git init
git add .
git commit -m "Implementa a API ReAlimenta SP"
git branch -M main
git remote add origin URL_DO_REPOSITORIO
git push -u origin main
```

Confira que `.env` não existe ou não foi adicionado. `.gitignore` já o ignora.

## Render

A imagem está descrita no `Dockerfile`. Ela compila com JDK 21, executa os testes unitários e sobe o JAR com JRE 21. A aplicação lê `PORT`.

Variáveis obrigatórias no painel do serviço:

| Variável | Função |
| --- | --- |
| `DB_URL` | JDBC do Oracle |
| `DB_USERNAME` | Usuário do schema |
| `DB_PASSWORD` | Senha do schema |
| `PORT` | Injetada pelo Render. Não é necessário fixá-la se o painel já a fornece |

Health check path sugerido: `/api/saude`.

Como validar o Oracle a partir do ambiente hospedado:

1. `ConexaoInicial` pede uma conexão ao fim da subida. Se o Oracle recusar, o processo encerra. A falha de credencial ou de rede aparece no log do Hikari, com o host, sem a senha na resposta HTTP.
2. Abra `/api/saude`. Isso prova que o processo respondeu, não que o SQL funcionou.
3. Abra `/api/estabelecimentos`. Uma lista 200 indica que o Oracle respondeu. Um 500 genérico indica falha, sem SQL na resposta. O detalhe fica no log do Render.

O Oracle da FIAP pode recusar o IP do Render. Essa liberação não foi testada. A URL pública do serviço fica **PENDENTE** até alguém publicar e abrir `/api/saude` de verdade.

Comando local da imagem, quando Docker e as variáveis existirem:

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

Para exportar imagens ao PDF: abra o `.puml` em https://www.plantuml.com/plantuml e o `.mmd` em https://mermaid.live. As imagens não estão geradas neste repositório.

## Limitações que precisam aparecer na apresentação

- Protótipo com dados fictícios. Sem autenticação. Não usar com dado real de estabelecimento ou instituição.
- O lote é indivisível e aceita uma única doação.
- Cancelar não devolve o lote para `DISPONIVEL`.
- O prazo não é laudo sanitário.
- Exclusão física de doação não existe.
- Estabelecimento com lote, instituição com doação e lote com doação não são apagados.
- Os testes automatizados não conectaram no Oracle.
- O deploy no Render não foi executado.
