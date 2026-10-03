package com.kyofoundation.skillnapse.modules.questao.entity;

import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questoes")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "assuntoGeral", "topicoReferencia", "dificuldade", "banca", "ano"})
@EqualsAndHashCode(of = "id")
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "assunto_geral", length = 100, nullable = false)
    private String assuntoGeral;

    @Column(name = "topico_referencia", length = 150, nullable = false)
    private String topicoReferencia;

    @Column(name = "enunciado", columnDefinition = "TEXT", nullable = false)
    private String enunciado;

    @Column(name = "hash_enunciado", length = 64, nullable = false, unique = true)
    private String hashEnunciado;

    @Column(name = "explicacao_gabarito", columnDefinition = "TEXT", nullable = false)
    private String explicacaoGabarito;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "dificuldade", length = 15, nullable = false)
    private DificuldadeQuestao dificuldade = DificuldadeQuestao.MEDIA;

    @Column(name = "banca", length = 50)
    private String banca;

    @Column(name = "ano")
    private Integer ano;

    @Builder.Default
    @Column(name = "gerada_por_ia", nullable = false)
    private Boolean geradaPorIa = false;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Builder.Default
    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlternativaQuestao> alternativas = new ArrayList<>();
}
