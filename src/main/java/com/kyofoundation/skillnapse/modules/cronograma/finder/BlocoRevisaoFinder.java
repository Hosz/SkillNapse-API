package com.kyofoundation.skillnapse.modules.cronograma.finder;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.MateriaFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.TopicoFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BlocoRevisaoFinder {

    private final UserFinder userFinder;
    private final TemplateSemanalFinder templateSemanalFinder;
    private final BlocoHorarioTemplateFinder blocoHorarioTemplateFinder;
    private final ExcecaoDiariaFinder excecaoDiariaFinder;
    private final MateriaFinder materiaFinder;
    private final TopicoFinder topicoFinder;

    public Usuario buscarUsuario(UUID usuarioId) {
        return userFinder.findById(usuarioId);
    }

    public TemplateSemanal buscarTemplate(UUID templateId) {
        return templateSemanalFinder.findById(templateId);
    }

    public Materia buscarMateriaOpcional(UUID materiaId) {
        if (materiaId == null) {
            return null;
        }
        return materiaFinder.findById(materiaId);
    }

    public List<Topico> buscarTopicos(Collection<UUID> topicoIds) {
        return topicoFinder.findAllByIds(topicoIds);
    }

    public BlocoHorarioTemplate buscarBlocoTemplate(UUID blocoId) {
        return blocoHorarioTemplateFinder.findById(blocoId);
    }

    public ExcecaoDiaria buscarExcecaoDiaria(UUID excecaoId) {
        return excecaoDiariaFinder.findById(excecaoId);
    }
}
