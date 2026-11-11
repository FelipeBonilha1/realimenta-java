-- ReAlimenta SP — dados ficticios de demonstracao.
-- Executar depois de 01_estrutura.sql. Nao apaga dados existentes.
-- Se for executado de novo sem limpeza, as chaves primarias 1 a 4 vao falhar.
-- Os prazos usam o relogio do banco convertido para America/Sao_Paulo,
-- para a demonstracao continuar valida depois da data em que o script foi escrito.

ALTER SESSION SET TIME_ZONE = 'America/Sao_Paulo';

INSERT INTO ESTABELECIMENTO (ID, NOME, TIPO, EMAIL, TELEFONE, ENDERECO, BAIRRO, CIDADE)
VALUES (1, 'Padaria Sol Nascente', 'PADARIA', 'contato@padariasolnascente.example', '11987654321',
        'Rua das Flores, 120', 'Vila Mariana', 'São Paulo');

INSERT INTO ESTABELECIMENTO (ID, NOME, TIPO, EMAIL, TELEFONE, ENDERECO, BAIRRO, CIDADE)
VALUES (2, 'Mercado Bairro Verde', 'SUPERMERCADO', 'contato@bairroverde.example', '11912345678',
        'Avenida das Acacias, 800', 'Pinheiros', 'São Paulo');

INSERT INTO INSTITUICAO (ID, NOME, EMAIL, TELEFONE, ENDERECO, BAIRRO, CIDADE)
VALUES (1, 'Instituto Acolhe', 'contato@institutoacolhe.example', '1133334444',
        'Rua da Esperanca, 45', 'Bela Vista', 'São Paulo');

INSERT INTO INSTITUICAO (ID, NOME, EMAIL, TELEFONE, ENDERECO, BAIRRO, CIDADE)
VALUES (2, 'Casa do Pao', 'contato@casadopao.example', '1144445555',
        'Rua da Harmonia, 90', 'Liberdade', 'São Paulo');

-- Lote 1: disponivel e dentro do prazo. Serve para a reserva da demonstracao.
INSERT INTO LOTE_ALIMENTO (ID, ESTABELECIMENTO_ID, DESCRICAO, CATEGORIA, QUANTIDADE_KG, PRAZO_APROVEITAMENTO, STATUS)
VALUES (1, 1, 'Paes do dia sem comercializacao', 'PANIFICADOS', 8.500,
        CAST((SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo') AS TIMESTAMP) + INTERVAL '2' DAY,
        'DISPONIVEL');

-- Lote 2: status DISPONIVEL, mas prazo vencido. Nao entra no filtro disponiveis=true.
INSERT INTO LOTE_ALIMENTO (ID, ESTABELECIMENTO_ID, DESCRICAO, CATEGORIA, QUANTIDADE_KG, PRAZO_APROVEITAMENTO, STATUS)
VALUES (2, 2, 'Hortifruti de vespera', 'IN_NATURA', 15.000,
        CAST((SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo') AS TIMESTAMP) - INTERVAL '1' DAY,
        'DISPONIVEL');

-- Lote 3: ja reservado. Nao pode ser reservado de novo nem editado na quantidade.
INSERT INTO LOTE_ALIMENTO (ID, ESTABELECIMENTO_ID, DESCRICAO, CATEGORIA, QUANTIDADE_KG, PRAZO_APROVEITAMENTO, STATUS)
VALUES (3, 1, 'Refeicoes prontas do almoco', 'PREPARADO', 6.000,
        CAST((SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo') AS TIMESTAMP) + INTERVAL '1' DAY,
        'RESERVADO');

-- Lote 4: entrega ja confirmada, para o painel de indicadores nao comecar zerado.
INSERT INTO LOTE_ALIMENTO (ID, ESTABELECIMENTO_ID, DESCRICAO, CATEGORIA, QUANTIDADE_KG, PRAZO_APROVEITAMENTO, STATUS)
VALUES (4, 2, 'Arroz e feijao cozidos', 'PREPARADO', 10.000,
        CAST((SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo') AS TIMESTAMP) - INTERVAL '12' HOUR,
        'ENTREGUE');

INSERT INTO DOACAO (ID, LOTE_ID, INSTITUICAO_ID, STATUS, DATA_RESERVA, DATA_COLETA, DATA_ENTREGA, QUANTIDADE_ENTREGUE_KG)
VALUES (1, 3, 1, 'RESERVADA',
        CAST((SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo') AS TIMESTAMP) - INTERVAL '2' HOUR,
        NULL, NULL, NULL);

INSERT INTO DOACAO (ID, LOTE_ID, INSTITUICAO_ID, STATUS, DATA_RESERVA, DATA_COLETA, DATA_ENTREGA, QUANTIDADE_ENTREGUE_KG)
VALUES (2, 4, 2, 'ENTREGUE',
        CAST((SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo') AS TIMESTAMP) - INTERVAL '3' DAY,
        CAST((SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo') AS TIMESTAMP) - INTERVAL '2' DAY,
        CAST((SYSTIMESTAMP AT TIME ZONE 'America/Sao_Paulo') AS TIMESTAMP) - INTERVAL '1' DAY,
        9.500);

COMMIT;
