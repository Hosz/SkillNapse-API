package com.kyofoundation.skillnapse.modules.gamificacao.entity;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ofensivas_usuario")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "diasConsecutivosAtual", "maiorSequenciaDias", "dataUltimoEstudo"})
@EqualsAndHashCode(of = "id")
public class OfensivaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Builder.Default
    @Column(name = "dias_consecutivos_atual", nullable = false)
    private Integer diasConsecutivosAtual = 0;

    @Builder.Default
    @Column(name = "maior_sequencia_dias", nullable = false)
    private Integer maiorSequenciaDias = 0;

    @Column(name = "data_ultimo_estudo")
    private LocalDate dataUltimoEstudo;
}
