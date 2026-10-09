package com.kyofoundation.skillnapse.modules.questao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.questao.entity.AlternativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.repository.AlternativaQuestaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlternativaQuestaoFinderTest {

    @Mock
    private AlternativaQuestaoRepository repository;

    @InjectMocks
    private AlternativaQuestaoFinder finder;

    @Test
    @DisplayName("Deve encontrar alternativa por ID com sucesso")
    void deveEncontrarPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        AlternativaQuestao alt = AlternativaQuestao.builder().id(id).build();

        when(repository.findById(id)).thenReturn(Optional.of(alt));

        AlternativaQuestao resultado = finder.findById(id);

        assertThat(resultado).isEqualTo(alt);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao não encontrar alternativa por ID")
    void deveLancarResourceNotFoundPorId() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Alternativa não encontrada.");
    }

    @Test
    @DisplayName("Deve encontrar alternativa por ID e questaoId com sucesso")
    void deveEncontrarPorIdEQuestaoId() {
        UUID altId = UUID.randomUUID();
        UUID questaoId = UUID.randomUUID();
        AlternativaQuestao alt = AlternativaQuestao.builder().id(altId).build();

        when(repository.findByIdAndQuestaoId(altId, questaoId)).thenReturn(Optional.of(alt));

        AlternativaQuestao resultado = finder.findByIdAndQuestaoId(altId, questaoId);

        assertThat(resultado).isEqualTo(alt);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando alternativa não pertencer à questão")
    void deveLancarResourceNotFoundPorIdEQuestaoId() {
        UUID altId = UUID.randomUUID();
        UUID questaoId = UUID.randomUUID();

        when(repository.findByIdAndQuestaoId(altId, questaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findByIdAndQuestaoId(altId, questaoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Alternativa informada não pertence à questão indicada.");
    }

    @Test
    @DisplayName("Deve encontrar alternativa correta por questaoId com sucesso")
    void deveEncontrarCorretaPorQuestaoId() {
        UUID questaoId = UUID.randomUUID();
        AlternativaQuestao alt = AlternativaQuestao.builder().id(UUID.randomUUID()).correta(true).build();

        when(repository.findByQuestaoIdAndCorretaTrue(questaoId)).thenReturn(Optional.of(alt));

        AlternativaQuestao resultado = finder.findCorretaPorQuestaoId(questaoId);

        assertThat(resultado).isEqualTo(alt);
    }

    @Test
    @DisplayName("Deve listar alternativas ordenadas por letra")
    void deveListarPorQuestaoOrdenada() {
        UUID questaoId = UUID.randomUUID();
        List<AlternativaQuestao> lista = List.of(AlternativaQuestao.builder().letra("A").build());

        when(repository.findByQuestaoIdOrderByLetraAsc(questaoId)).thenReturn(lista);

        List<AlternativaQuestao> resultado = finder.findByQuestaoIdOrderByLetraAsc(questaoId);

        assertThat(resultado).isEqualTo(lista);
    }
}
