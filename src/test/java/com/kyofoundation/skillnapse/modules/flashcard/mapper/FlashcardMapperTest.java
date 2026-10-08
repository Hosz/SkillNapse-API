package com.kyofoundation.skillnapse.modules.flashcard.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.RevisarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.FlashcardResponse;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.HistoricoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.flashcard.entity.HistoricoRevisaoFlashcard;
import com.kyofoundation.skillnapse.modules.flashcard.enums.ClassificacaoResposta;
import com.kyofoundation.skillnapse.modules.flashcard.support.SrsAlgorithmSupport;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlashcardMapperTest {

    @Test
    @DisplayName("Deve converter CriarFlashcardRequest em entidade Flashcard com defaults do SM-2")
    void deveConverterCriarRequestEmEntidade() {
        Baralho baralho = Baralho.builder().id(UUID.randomUUID()).titulo("Baralho").build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).titulo("Tópico").build();
        CriarFlashcardRequest request = new CriarFlashcardRequest(baralho.getId(), topico.getId(), "Frente", "Verso");

        Flashcard entity = FlashcardMapper.toEntity(request, baralho, topico);

        assertThat(entity).isNotNull();
        assertThat(entity.getBaralho()).isEqualTo(baralho);
        assertThat(entity.getTopico()).isEqualTo(topico);
        assertThat(entity.getFrente()).isEqualTo("Frente");
        assertThat(entity.getVerso()).isEqualTo("Verso");
        assertThat(entity.getFatorFacilidade()).isEqualByComparingTo("2.50");
        assertThat(entity.getIntervaloDias()).isEqualTo(0);
        assertThat(entity.getRepeticoes()).isEqualTo(0);
        assertThat(entity.getProximaRevisao()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Deve atualizar campos frente, verso e topico de entidade Flashcard")
    void deveAtualizarEntidadeExistente() {
        Flashcard entity = Flashcard.builder()
                .frente("Frente Antiga")
                .verso("Verso Antigo")
                .build();

        Topico novoTopico = Topico.builder().id(UUID.randomUUID()).titulo("Novo Tópico").build();
        AtualizarFlashcardRequest request = new AtualizarFlashcardRequest(novoTopico.getId(), "Frente Nova", "Verso Novo");

        FlashcardMapper.updateEntity(entity, request, novoTopico);

        assertThat(entity.getFrente()).isEqualTo("Frente Nova");
        assertThat(entity.getVerso()).isEqualTo("Verso Novo");
        assertThat(entity.getTopico()).isEqualTo(novoTopico);
    }

    @Test
    @DisplayName("Deve aplicar resultado do algoritmo SM-2 na entidade Flashcard")
    void deveAplicarResultadoSrs() {
        Flashcard entity = Flashcard.builder()
                .fatorFacilidade(new BigDecimal("2.50"))
                .intervaloDias(1)
                .repeticoes(1)
                .build();

        LocalDate proxima = LocalDate.now().plusDays(6);
        SrsAlgorithmSupport.SrsResultadoCalculo resultado = new SrsAlgorithmSupport.SrsResultadoCalculo(
                new BigDecimal("2.60"), 6, 2, proxima
        );

        FlashcardMapper.aplicarResultadoSrs(entity, resultado);

        assertThat(entity.getFatorFacilidade()).isEqualTo(new BigDecimal("2.60"));
        assertThat(entity.getIntervaloDias()).isEqualTo(6);
        assertThat(entity.getRepeticoes()).isEqualTo(2);
        assertThat(entity.getProximaRevisao()).isEqualTo(proxima);
        assertThat(entity.getUltimaRevisao()).isNotNull();
    }

    @Test
    @DisplayName("Deve converter entidade Flashcard em FlashcardResponse")
    void deveConverterEntidadeEmResponse() {
        UUID id = UUID.randomUUID();
        Baralho baralho = Baralho.builder().id(UUID.randomUUID()).titulo("Título Baralho").build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).titulo("Título Tópico").build();

        Flashcard entity = Flashcard.builder()
                .id(id)
                .baralho(baralho)
                .topico(topico)
                .frente("Frente")
                .verso("Verso")
                .fatorFacilidade(new BigDecimal("2.50"))
                .intervaloDias(6)
                .repeticoes(2)
                .proximaRevisao(LocalDate.now())
                .build();

        FlashcardResponse response = FlashcardMapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.baralhoId()).isEqualTo(baralho.getId());
        assertThat(response.baralhoTitulo()).isEqualTo("Título Baralho");
        assertThat(response.topicoId()).isEqualTo(topico.getId());
        assertThat(response.topicoTitulo()).isEqualTo("Título Tópico");
        assertThat(response.frente()).isEqualTo("Frente");
        assertThat(response.verso()).isEqualTo("Verso");
    }

    @Test
    @DisplayName("Deve converter requisicao de revisao em HistoricoRevisaoFlashcard e HistoricoRevisaoResponse")
    void deveConverterHistorico() {
        Flashcard card = Flashcard.builder().id(UUID.randomUUID()).build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        RevisarFlashcardRequest request = new RevisarFlashcardRequest(ClassificacaoResposta.BOM, 5);

        HistoricoRevisaoFlashcard historico = FlashcardMapper.toHistoricoEntity(card, usuario, request);

        assertThat(historico).isNotNull();
        assertThat(historico.getFlashcard()).isEqualTo(card);
        assertThat(historico.getUsuario()).isEqualTo(usuario);
        assertThat(historico.getClassificacaoResposta()).isEqualTo(ClassificacaoResposta.BOM);
        assertThat(historico.getTempoRespostaSegundos()).isEqualTo(5);

        HistoricoRevisaoResponse response = FlashcardMapper.toHistoricoResponse(historico);
        assertThat(response).isNotNull();
        assertThat(response.flashcardId()).isEqualTo(card.getId());
        assertThat(response.classificacaoResposta()).isEqualTo(ClassificacaoResposta.BOM);
        assertThat(response.tempoRespostaSegundos()).isEqualTo(5);
    }
}
