package com.kyofoundation.skillnapse.modules.questao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import com.kyofoundation.skillnapse.modules.questao.repository.QuestaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestaoFinderTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @InjectMocks
    private QuestaoFinder questaoFinder;

    @Test
    @DisplayName("Deve encontrar questão por ID com sucesso")
    void deveEncontrarQuestaoPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Questao questao = Questao.builder().id(id).build();

        when(questaoRepository.findById(id)).thenReturn(Optional.of(questao));

        Questao resultado = questaoFinder.findById(id);

        assertThat(resultado).isEqualTo(questao);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando questão não existir")
    void deveLancarResourceNotFoundQuandoNaoExistir() {
        UUID id = UUID.randomUUID();

        when(questaoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questaoFinder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Questão não encontrada.");
    }

    @Test
    @DisplayName("Deve buscar questão por ID com alternativas com sucesso")
    void deveBuscarComAlternativasComSucesso() {
        UUID id = UUID.randomUUID();
        Questao questao = Questao.builder().id(id).build();

        when(questaoRepository.findByIdComAlternativas(id)).thenReturn(Optional.of(questao));

        Questao resultado = questaoFinder.findByIdComAlternativas(id);

        assertThat(resultado).isEqualTo(questao);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando busca com alternativas não encontrar registro")
    void deveLancarResourceNotFoundComAlternativas() {
        UUID id = UUID.randomUUID();

        when(questaoRepository.findByIdComAlternativas(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questaoFinder.findByIdComAlternativas(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Questão não encontrada.");
    }

    @Test
    @DisplayName("Deve buscar com filtros paginados delegando ao repositório")
    void deveBuscarComFiltrosPaginados() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Questao> pagina = new PageImpl<>(List.of(Questao.builder().id(UUID.randomUUID()).build()));

        when(questaoRepository.buscarComFiltros("Dir", "Topico", "CESPE", 2024, DificuldadeQuestao.MEDIA, "busca", pageable))
                .thenReturn(pagina);

        Page<Questao> resultado = questaoFinder.buscarComFiltros("Dir", "Topico", "CESPE", 2024, DificuldadeQuestao.MEDIA, "busca", pageable);

        assertThat(resultado).isEqualTo(pagina);
    }
}
