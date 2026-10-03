package com.kyofoundation.skillnapse.modules.flashcard.entity;

import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "flashcards")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "fatorFacilidade", "intervaloDias", "repeticoes", "proximaRevisao"})
@EqualsAndHashCode(of = "id")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baralho_id", nullable = false)
    private Baralho baralho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topico_id")
    private Topico topico;

    @Column(name = "frente", columnDefinition = "TEXT", nullable = false)
    private String frente;

    @Column(name = "verso", columnDefinition = "TEXT", nullable = false)
    private String verso;

    @Builder.Default
    @Column(name = "fator_facilidade", precision = 4, scale = 2, nullable = false)
    private BigDecimal fatorFacilidade = new BigDecimal("2.50");

    @Builder.Default
    @Column(name = "intervalo_dias", nullable = false)
    private Integer intervaloDias = 0;

    @Builder.Default
    @Column(name = "repeticoes", nullable = false)
    private Integer repeticoes = 0;

    @Builder.Default
    @Column(name = "proxima_revisao", nullable = false)
    private LocalDate proximaRevisao = LocalDate.now();

    @Column(name = "ultima_revisao")
    private Instant ultimaRevisao;

    @Builder.Default
    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoricoRevisaoFlashcard> historicoRevisoes = new ArrayList<>();
}
