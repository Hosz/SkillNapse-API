package com.kyofoundation.skillnapse.modules.cronograma.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExcecaoDiariaRepository extends JpaRepository<ExcecaoDiaria, UUID> {

    List<ExcecaoDiaria> findAllByUsuarioAndDataExcecao(Usuario usuario, LocalDate dataExcecao);

    Optional<ExcecaoDiaria> findByUsuarioAndDataExcecaoAndBlocoTemplateOrigem(Usuario usuario, LocalDate dataExcecao, BlocoHorarioTemplate blocoTemplateOrigem);

    boolean existsByUsuarioAndDataExcecaoAndBlocoTemplateOrigem(Usuario usuario, LocalDate dataExcecao, BlocoHorarioTemplate blocoTemplateOrigem);

    boolean existsByUsuarioAndDataExcecaoAndBlocoTemplateOrigemAndIdNot(Usuario usuario, LocalDate dataExcecao, BlocoHorarioTemplate blocoTemplateOrigem, UUID id);

    void deleteAllByUsuarioAndDataExcecao(Usuario usuario, LocalDate dataExcecao);
}
