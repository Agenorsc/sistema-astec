package br.com.astec.repository;

import br.com.astec.model.Equipamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:hsqldb:mem:astec-hsql-test",
        "spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.HSQLDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class HsqlDatabaseTest {

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Test
    void persistsAndReadsEquipment() {
        Equipamento equipamento = new Equipamento(null, "Prensa hidráulica", "PH-100", "HSQL-001");

        Equipamento salvo = equipamentoRepository.save(equipamento);

        assertTrue(equipamentoRepository.findById(salvo.getId()).isPresent());
    }
}
