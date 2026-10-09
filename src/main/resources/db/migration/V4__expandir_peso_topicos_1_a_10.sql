-- =========================================================================
-- V4__expandir_peso_topicos_1_a_10.sql
-- Expande a escala de peso do tópico no edital de 1 a 5 para 1 a 10.
-- =========================================================================

ALTER TABLE topicos DROP CONSTRAINT chk_topicos_peso;
ALTER TABLE topicos ADD CONSTRAINT chk_topicos_peso CHECK (peso_edital BETWEEN 1 AND 10);
