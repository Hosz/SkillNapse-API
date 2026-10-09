-- =========================================================================
-- V3__alterar_nota_redacao_escala_1000.sql
-- Expande a precisão da coluna nota_geral para DECIMAL(6,2),
-- comportando a escala de notas do ENEM e concursos (0.00 a 1000.00).
-- =========================================================================

ALTER TABLE submissoes_redacao ALTER COLUMN nota_geral TYPE DECIMAL(6,2);
