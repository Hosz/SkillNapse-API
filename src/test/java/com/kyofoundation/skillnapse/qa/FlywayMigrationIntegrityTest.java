package com.kyofoundation.skillnapse.qa;

import com.kyofoundation.skillnapse.TestcontainersConfiguration;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlywayMigrationIntegrityTest {

    @Autowired
    private Flyway flyway;

    @Test
    @DisplayName("Deve verificar que todas as migrações do Flyway foram aplicadas com sucesso no PostgreSQL")
    void deveVerificarIntegridadeDasMigracoesFlyway() {
        MigrationInfo[] migrations = flyway.info().all();

        assertThat(migrations)
                .as("O Flyway deve possuir migrações registradas")
                .isNotEmpty();

        for (MigrationInfo migration : migrations) {
            assertThat(migration.getState())
                    .as("A migração " + migration.getScript() + " deve estar no estado SUCCESS")
                    .isEqualTo(MigrationState.SUCCESS);
        }

        // Verifica que as 4 migrações principais (V1, V2, V3, V4) estão presentes
        assertThat(migrations).anyMatch(m -> "1".equals(m.getVersion().getVersion()));
        assertThat(migrations).anyMatch(m -> "2".equals(m.getVersion().getVersion()));
        assertThat(migrations).anyMatch(m -> "3".equals(m.getVersion().getVersion()));
        assertThat(migrations).anyMatch(m -> "4".equals(m.getVersion().getVersion()));
    }
}
