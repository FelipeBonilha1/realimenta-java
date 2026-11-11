-- ATENCAO: script manual e destrutivo.
-- Apaga os dados ficticios e, na secao final, os objetos do ReAlimenta neste schema.
-- A aplicacao Spring nao executa este arquivo. Nao rode no fluxo normal de demonstracao.
-- Use somente quando quiser recriar a estrutura de proposito.

ALTER SESSION SET TIME_ZONE = 'America/Sao_Paulo';

DELETE FROM DOACAO;
DELETE FROM LOTE_ALIMENTO;
DELETE FROM INSTITUICAO;
DELETE FROM ESTABELECIMENTO;
COMMIT;

-- Secao opcional de remocao dos objetos. Descomente apenas se for recriar 01_estrutura.sql.
-- DROP TABLE DOACAO;
-- DROP TABLE LOTE_ALIMENTO;
-- DROP TABLE INSTITUICAO;
-- DROP TABLE ESTABELECIMENTO;
-- DROP SEQUENCE SEQ_DOACAO;
-- DROP SEQUENCE SEQ_LOTE_ALIMENTO;
-- DROP SEQUENCE SEQ_INSTITUICAO;
-- DROP SEQUENCE SEQ_ESTABELECIMENTO;
