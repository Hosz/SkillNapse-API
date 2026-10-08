-- =============================================================================
-- V2: ASSOCIAÇÃO DE MÚLTIPLOS TÓPICOS A BLOCOS DE REVISÃO DO CRONOGRAMA
-- =============================================================================

CREATE TABLE blocos_template_topicos_revisao (
    bloco_template_id UUID NOT NULL,
    topico_id UUID NOT NULL,
    PRIMARY KEY (bloco_template_id, topico_id),
    CONSTRAINT fk_bttr_bloco FOREIGN KEY (bloco_template_id) REFERENCES blocos_horario_template(id) ON DELETE CASCADE,
    CONSTRAINT fk_bttr_topico FOREIGN KEY (topico_id) REFERENCES topicos(id) ON DELETE CASCADE
);

CREATE INDEX idx_bttr_bloco ON blocos_template_topicos_revisao(bloco_template_id);
CREATE INDEX idx_bttr_topico ON blocos_template_topicos_revisao(topico_id);

CREATE TABLE excecoes_topicos_revisao (
    excecao_id UUID NOT NULL,
    topico_id UUID NOT NULL,
    PRIMARY KEY (excecao_id, topico_id),
    CONSTRAINT fk_etr_excecao FOREIGN KEY (excecao_id) REFERENCES excecoes_diarias(id) ON DELETE CASCADE,
    CONSTRAINT fk_etr_topico FOREIGN KEY (topico_id) REFERENCES topicos(id) ON DELETE CASCADE
);

CREATE INDEX idx_etr_excecao ON excecoes_topicos_revisao(excecao_id);
CREATE INDEX idx_etr_topico ON excecoes_topicos_revisao(topico_id);
