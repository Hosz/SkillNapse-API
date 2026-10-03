-- =============================================================================
-- SkillNapse — Migração Baseline Flyway V1 (Padrão KyoFoundation)
-- Banco de Dados: PostgreSQL 16
-- Convenção: Identificadores em UUID, Nomenclatura 100% em Português
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- 1. MÓDULO: USUÁRIOS E AUTENTICAÇÃO
-- =============================================================================

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE INDEX idx_usuarios_email ON usuarios(email);

CREATE TABLE tokens_atualizacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    revogado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tokens_atualizacao_token UNIQUE (token),
    CONSTRAINT fk_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_tokens_token ON tokens_atualizacao(token);
CREATE INDEX idx_tokens_usuario_valido ON tokens_atualizacao(usuario_id, revogado, expira_em);

-- =============================================================================
-- 2. MÓDULO: ÁRVORE DE CONHECIMENTO E EDITAIS
-- =============================================================================

CREATE TABLE planos_estudo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_planos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_planos_usuario_ativo ON planos_estudo(usuario_id, ativo);

CREATE TABLE materias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plano_estudo_id UUID NOT NULL,
    nome VARCHAR(100) NOT NULL,
    cor_hex VARCHAR(7),
    ordem INT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_materias_plano FOREIGN KEY (plano_estudo_id) REFERENCES planos_estudo(id) ON DELETE CASCADE
);

CREATE INDEX idx_materias_plano_ordem ON materias(plano_estudo_id, ordem);

CREATE TABLE topicos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    materia_id UUID NOT NULL,
    topico_pai_id UUID,
    titulo VARCHAR(200) NOT NULL,
    nivel_proficiencia VARCHAR(20) NOT NULL DEFAULT 'INICIANTE',
    peso_edital INT NOT NULL DEFAULT 1,
    concluido BOOLEAN NOT NULL DEFAULT FALSE,
    ordem INT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_topicos_proficiencia CHECK (nivel_proficiencia IN ('INICIANTE', 'INTERMEDIARIO', 'AVANCADO')),
    CONSTRAINT chk_topicos_peso CHECK (peso_edital BETWEEN 1 AND 5),
    CONSTRAINT fk_topicos_materia FOREIGN KEY (materia_id) REFERENCES materias(id) ON DELETE CASCADE,
    CONSTRAINT fk_topicos_pai FOREIGN KEY (topico_pai_id) REFERENCES topicos(id) ON DELETE CASCADE
);

CREATE INDEX idx_topicos_materia ON topicos(materia_id);
CREATE INDEX idx_topicos_pai ON topicos(topico_pai_id);
CREATE INDEX idx_topicos_proficiencia ON topicos(materia_id, nivel_proficiencia, peso_edital);

CREATE TABLE rascunhos_editais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    plano_estudo_id UUID,
    nome_arquivo VARCHAR(255) NOT NULL,
    conteudo_extraido_json JSONB NOT NULL,
    status VARCHAR(25) NOT NULL DEFAULT 'PROCESSANDO',
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_editais_status CHECK (status IN ('PROCESSANDO', 'AGUARDANDO_APROVACAO', 'CONVERTIDO', 'FALHA')),
    CONSTRAINT fk_editais_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_editais_plano FOREIGN KEY (plano_estudo_id) REFERENCES planos_estudo(id) ON DELETE SET NULL
);

CREATE INDEX idx_editais_usuario_status ON rascunhos_editais(usuario_id, status);

-- =============================================================================
-- 3. MÓDULO: CRONOGRAMA HÍBRIDO (TEMPLATES + EXCEÇÕES DIÁRIAS)
-- =============================================================================

CREATE TABLE templates_semanais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    nome VARCHAR(100) NOT NULL DEFAULT 'Padrão',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_templates_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_templates_usuario_ativo ON templates_semanais(usuario_id, ativo);

CREATE TABLE blocos_horario_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_semanal_id UUID NOT NULL,
    dia_semana VARCHAR(15) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    tipo_bloco VARCHAR(20) NOT NULL DEFAULT 'FOCO_TEORIA',
    materia_id UUID,
    topico_id UUID,
    CONSTRAINT chk_blocos_dia_semana CHECK (dia_semana IN ('SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO', 'DOMINGO')),
    CONSTRAINT chk_blocos_tipo CHECK (tipo_bloco IN ('FOCO_TEORIA', 'REVISAO', 'SIMULADO', 'DESCANSO')),
    CONSTRAINT chk_blocos_horario CHECK (hora_fim > hora_inicio),
    CONSTRAINT fk_blocos_template FOREIGN KEY (template_semanal_id) REFERENCES templates_semanais(id) ON DELETE CASCADE,
    CONSTRAINT fk_blocos_materia FOREIGN KEY (materia_id) REFERENCES materias(id) ON DELETE SET NULL,
    CONSTRAINT fk_blocos_topico FOREIGN KEY (topico_id) REFERENCES topicos(id) ON DELETE SET NULL
);

CREATE INDEX idx_blocos_template_dia ON blocos_horario_template(template_semanal_id, dia_semana, hora_inicio);

CREATE TABLE excecoes_diarias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    data_excecao DATE NOT NULL,
    bloco_template_origem_id UUID,
    tipo_acao VARCHAR(25) NOT NULL,
    hora_inicio TIME,
    hora_fim TIME,
    tipo_bloco VARCHAR(20),
    materia_id UUID,
    topico_id UUID,
    CONSTRAINT chk_excecoes_acao CHECK (tipo_acao IN ('CANCELAR_BLOCO', 'SUBSTITUIR_HORARIO', 'BLOCO_AVULSO')),
    CONSTRAINT fk_excecoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_excecoes_bloco_origem FOREIGN KEY (bloco_template_origem_id) REFERENCES blocos_horario_template(id) ON DELETE SET NULL,
    CONSTRAINT fk_excecoes_materia FOREIGN KEY (materia_id) REFERENCES materias(id) ON DELETE SET NULL,
    CONSTRAINT fk_excecoes_topico FOREIGN KEY (topico_id) REFERENCES topicos(id) ON DELETE SET NULL
);

CREATE INDEX idx_excecoes_usuario_data ON excecoes_diarias(usuario_id, data_excecao);

-- =============================================================================
-- 4. MÓDULO: EXECUÇÃO DE ESTUDOS E CANVAS
-- =============================================================================

CREATE TABLE sessoes_estudo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    topico_id UUID NOT NULL,
    iniciado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    finalizado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    duracao_liquida_segundos INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONCLUIDA',
    observacoes TEXT,
    CONSTRAINT chk_sessoes_status CHECK (status IN ('CONCLUIDA', 'INTERROMPIDA')),
    CONSTRAINT fk_sessoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_sessoes_topico FOREIGN KEY (topico_id) REFERENCES topicos(id) ON DELETE CASCADE
);

CREATE INDEX idx_sessoes_usuario_periodo ON sessoes_estudo(usuario_id, iniciado_em);
CREATE INDEX idx_sessoes_topico ON sessoes_estudo(topico_id);

CREATE TABLE rascunhos_canvas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    topico_id UUID NOT NULL,
    dados_desenho_json JSONB NOT NULL,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_canvas_usuario_topico UNIQUE (usuario_id, topico_id),
    CONSTRAINT fk_canvas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_canvas_topico FOREIGN KEY (topico_id) REFERENCES topicos(id) ON DELETE CASCADE
);

CREATE INDEX idx_canvas_usuario_topico ON rascunhos_canvas(usuario_id, topico_id);

-- =============================================================================
-- 5. MÓDULO: REPETIÇÃO ESPAÇADA (FLASHCARDS SRS / SM-2)
-- =============================================================================

CREATE TABLE baralhos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    materia_id UUID,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_baralhos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_baralhos_materia FOREIGN KEY (materia_id) REFERENCES materias(id) ON DELETE SET NULL
);

CREATE INDEX idx_baralhos_usuario ON baralhos(usuario_id);

CREATE TABLE flashcards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    baralho_id UUID NOT NULL,
    topico_id UUID,
    frente TEXT NOT NULL,
    verso TEXT NOT NULL,
    fator_facilidade DECIMAL(4,2) NOT NULL DEFAULT 2.50,
    intervalo_dias INT NOT NULL DEFAULT 0,
    repeticoes INT NOT NULL DEFAULT 0,
    proxima_revisao DATE NOT NULL DEFAULT CURRENT_DATE,
    ultima_revisao TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_flashcards_baralho FOREIGN KEY (baralho_id) REFERENCES baralhos(id) ON DELETE CASCADE,
    CONSTRAINT fk_flashcards_topico FOREIGN KEY (topico_id) REFERENCES topicos(id) ON DELETE SET NULL
);

CREATE INDEX idx_flashcards_baralho_revisao ON flashcards(baralho_id, proxima_revisao);
CREATE INDEX idx_flashcards_topico ON flashcards(topico_id);

CREATE TABLE historico_revisoes_flashcard (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flashcard_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    classificacao_resposta VARCHAR(15) NOT NULL,
    tempo_resposta_segundos INT,
    revisado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_revisoes_classificacao CHECK (classificacao_resposta IN ('ERRO', 'DIFICIL', 'BOM', 'FACIL')),
    CONSTRAINT fk_revisoes_flashcard FOREIGN KEY (flashcard_id) REFERENCES flashcards(id) ON DELETE CASCADE,
    CONSTRAINT fk_revisoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_revisoes_flashcard_data ON historico_revisoes_flashcard(flashcard_id, revisado_em);

-- =============================================================================
-- 6. MÓDULO: BANCO CENTRAL DE QUESTÕES E SIMULADOS (COM REUSO E IA)
-- =============================================================================

CREATE TABLE questoes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assunto_geral VARCHAR(100) NOT NULL,
    topico_referencia VARCHAR(150) NOT NULL,
    enunciado TEXT NOT NULL,
    hash_enunciado VARCHAR(64) NOT NULL,
    explicacao_gabarito TEXT NOT NULL,
    dificuldade VARCHAR(15) NOT NULL DEFAULT 'MEDIA',
    banca VARCHAR(50),
    ano INT,
    gerada_por_ia BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_questoes_hash UNIQUE (hash_enunciado),
    CONSTRAINT chk_questoes_dificuldade CHECK (dificuldade IN ('FACIL', 'MEDIA', 'DIFICIL'))
);

CREATE INDEX idx_questoes_busca_acervo ON questoes(assunto_geral, topico_referencia, banca, dificuldade);
CREATE INDEX idx_questoes_hash ON questoes(hash_enunciado);

CREATE TABLE alternativas_questao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    questao_id UUID NOT NULL,
    letra CHAR(1) NOT NULL,
    texto TEXT NOT NULL,
    correta BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_alternativas_questao_letra UNIQUE (questao_id, letra),
    CONSTRAINT fk_alternativas_questao FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE
);

CREATE INDEX idx_alternativas_questao ON alternativas_questao(questao_id);

CREATE TABLE simulados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    concluido BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_simulados_tipo CHECK (tipo IN ('MANUAL', 'ADAPTATIVO_IA')),
    CONSTRAINT fk_simulados_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_simulados_usuario ON simulados(usuario_id, criado_em);

CREATE TABLE tentativas_questoes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    questao_id UUID NOT NULL,
    topico_id UUID,
    simulado_id UUID,
    alternativa_escolhida_id UUID NOT NULL,
    acertou BOOLEAN NOT NULL,
    tempo_gasto_segundos INT,
    respondido_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tentativas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_tentativas_questao FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE,
    CONSTRAINT fk_tentativas_topico FOREIGN KEY (topico_id) REFERENCES topicos(id) ON DELETE SET NULL,
    CONSTRAINT fk_tentativas_simulado FOREIGN KEY (simulado_id) REFERENCES simulados(id) ON DELETE SET NULL,
    CONSTRAINT fk_tentativas_alternativa FOREIGN KEY (alternativa_escolhida_id) REFERENCES alternativas_questao(id) ON DELETE RESTRICT
);

CREATE INDEX idx_tentativas_usuario_questao ON tentativas_questoes(usuario_id, questao_id);
CREATE INDEX idx_tentativas_analise_lacunas ON tentativas_questoes(usuario_id, topico_id, acertou);

-- =============================================================================
-- 7. MÓDULO: GAMIFICAÇÃO E HÁBITOS
-- =============================================================================

CREATE TABLE ofensivas_usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    dias_consecutivos_atual INT NOT NULL DEFAULT 0,
    maior_sequencia_dias INT NOT NULL DEFAULT 0,
    data_ultimo_estudo DATE,
    CONSTRAINT uk_ofensivas_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_ofensivas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_ofensivas_usuario ON ofensivas_usuario(usuario_id);

CREATE TABLE metas_diarias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    meta_minutos_estudo INT NOT NULL DEFAULT 120,
    meta_questoes_resolvidas INT NOT NULL DEFAULT 15,
    CONSTRAINT uk_metas_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_metas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_metas_usuario ON metas_diarias(usuario_id);

-- =============================================================================
-- 8. MÓDULO: LABORATÓRIO DE REDAÇÃO
-- =============================================================================

CREATE TABLE temas_redacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plano_estudo_id UUID,
    titulo VARCHAR(200) NOT NULL,
    textos_motivadores TEXT NOT NULL,
    criterios_avaliacao TEXT,
    gerado_por_ia BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_temas_plano FOREIGN KEY (plano_estudo_id) REFERENCES planos_estudo(id) ON DELETE SET NULL
);

CREATE TABLE submissoes_redacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    tema_redacao_id UUID NOT NULL,
    texto_aluno TEXT NOT NULL,
    nota_geral DECIMAL(5,2),
    feedback_ia_json JSONB,
    corrigido_em TIMESTAMP WITH TIME ZONE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_submissoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_submissoes_tema FOREIGN KEY (tema_redacao_id) REFERENCES temas_redacao(id) ON DELETE CASCADE
);

CREATE INDEX idx_submissoes_usuario ON submissoes_redacao(usuario_id, criado_em);
