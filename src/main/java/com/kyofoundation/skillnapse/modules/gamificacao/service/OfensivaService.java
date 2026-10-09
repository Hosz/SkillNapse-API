package com.kyofoundation.skillnapse.modules.gamificacao.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.StatusOfensivaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.OfensivaUsuario;
import com.kyofoundation.skillnapse.modules.gamificacao.finder.OfensivaUsuarioFinder;
import com.kyofoundation.skillnapse.modules.gamificacao.mapper.GamificacaoMapper;
import com.kyofoundation.skillnapse.modules.gamificacao.repository.OfensivaUsuarioRepository;
import com.kyofoundation.skillnapse.modules.gamificacao.support.CalculoOfensivaSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfensivaService {

    private final UserFinder userFinder;
    private final UsuarioValidator usuarioValidator;
    private final OfensivaUsuarioFinder ofensivaUsuarioFinder;
    private final OfensivaUsuarioRepository ofensivaUsuarioRepository;
    private final CalculoOfensivaSupport calculoOfensivaSupport;

    @Transactional
    public StatusOfensivaResponse obterStatusOfensiva(UUID usuarioId) {
        Usuario usuario = userFinder.findById(usuarioId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        OfensivaUsuario ofensiva = ofensivaUsuarioFinder.buscarOuCriar(usuario);

        LocalDate hoje = LocalDate.now();
        boolean quebra = calculoOfensivaSupport.revalidarStreak(ofensiva, hoje);
        if (quebra) {
            ofensiva = ofensivaUsuarioRepository.save(ofensiva);
        }

        boolean estudouHoje = calculoOfensivaSupport.isEstudouHoje(ofensiva, hoje);
        boolean ofensivaAtiva = calculoOfensivaSupport.isOfensivaAtiva(ofensiva, hoje);

        return GamificacaoMapper.toStatusResponse(ofensiva, estudouHoje, ofensivaAtiva);
    }

    @Transactional
    public StatusOfensivaResponse registrarEstudo(UUID usuarioId, LocalDate dataEstudo) {
        Usuario usuario = userFinder.findById(usuarioId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        LocalDate data = dataEstudo != null ? dataEstudo : LocalDate.now();

        OfensivaUsuario ofensiva = ofensivaUsuarioFinder.buscarOuCriar(usuario);
        calculoOfensivaSupport.registrarEstudo(ofensiva, data);
        OfensivaUsuario salva = ofensivaUsuarioRepository.save(ofensiva);

        LocalDate hoje = LocalDate.now();
        boolean estudouHoje = calculoOfensivaSupport.isEstudouHoje(salva, hoje);
        boolean ofensivaAtiva = calculoOfensivaSupport.isOfensivaAtiva(salva, hoje);

        return GamificacaoMapper.toStatusResponse(salva, estudouHoje, ofensivaAtiva);
    }

    @Transactional
    public void registrarEstudoSilencioso(Usuario usuario, LocalDate dataEstudo) {
        if (usuario == null) {
            return;
        }
        usuarioValidator.validarUsuarioAtivo(usuario);

        LocalDate data = dataEstudo != null ? dataEstudo : LocalDate.now();
        OfensivaUsuario ofensiva = ofensivaUsuarioFinder.buscarOuCriar(usuario);
        calculoOfensivaSupport.registrarEstudo(ofensiva, data);
        ofensivaUsuarioRepository.save(ofensiva);
    }
}
