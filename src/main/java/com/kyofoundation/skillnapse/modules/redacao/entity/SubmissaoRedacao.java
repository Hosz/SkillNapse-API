package com.kyofoundation.skillnapse.modules.redacao.entity;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissoes_redacao")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "notaGeral", "corrigidoEm", "criadoEm"})
@EqualsAndHashCode(of = "id")
public class SubmissaoRedacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tema_redacao_id", nullable = false)
    private TemaRedacao temaRedacao;

    @Column(name = "texto_aluno", columnDefinition = "TEXT", nullable = false)
    private String textoAluno;

    @Column(name = "nota_geral", precision = 5, scale = 2)
    private BigDecimal notaGeral;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feedback_ia_json", columnDefinition = "jsonb")
    private String feedbackIaJson;

    @Column(name = "corrigido_em")
    private Instant corrigidoEm;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
}
