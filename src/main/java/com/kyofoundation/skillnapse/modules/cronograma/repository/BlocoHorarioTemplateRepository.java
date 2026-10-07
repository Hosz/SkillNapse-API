package com.kyofoundation.skillnapse.modules.cronograma.repository;

import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface BlocoHorarioTemplateRepository extends JpaRepository<BlocoHorarioTemplate, UUID> {

    Page<BlocoHorarioTemplate> findByTemplateSemanal(TemplateSemanal templateSemanal, Pageable pageable);

    List<BlocoHorarioTemplate> findAllByTemplateSemanalOrderByDiaSemanaAscHoraInicioAsc(TemplateSemanal templateSemanal);

    List<BlocoHorarioTemplate> findAllByTemplateSemanalAndDiaSemanaOrderByHoraInicioAsc(TemplateSemanal templateSemanal, DiaSemana diaSemana);

    @Query("""
            SELECT COUNT(b) > 0 FROM BlocoHorarioTemplate b
            WHERE b.templateSemanal = :template
              AND b.diaSemana = :diaSemana
              AND b.horaInicio < :horaFim
              AND b.horaFim > :horaInicio
            """)
    boolean existeSobreposicaoHorario(
            @Param("template") TemplateSemanal template,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );

    @Query("""
            SELECT COUNT(b) > 0 FROM BlocoHorarioTemplate b
            WHERE b.templateSemanal = :template
              AND b.diaSemana = :diaSemana
              AND b.id <> :blocoId
              AND b.horaInicio < :horaFim
              AND b.horaFim > :horaInicio
            """)
    boolean existeSobreposicaoHorarioDesconsiderandoBloco(
            @Param("template") TemplateSemanal template,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("blocoId") UUID blocoId,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );
}
