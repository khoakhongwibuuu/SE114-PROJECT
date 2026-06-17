package com.carenest.backend.config.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class QaDemoSeedPropertiesTest {

    @Test
    void requireDefaultPassword_throwsWhenBlank() {
        QaDemoSeedProperties properties = new QaDemoSeedProperties();

        assertThrows(IllegalStateException.class, properties::requireDefaultPassword);
    }

    @Test
    void requireDefaultPassword_trimsConfiguredValue() {
        QaDemoSeedProperties properties = new QaDemoSeedProperties();
        properties.setDefaultPassword("  DemoPassword123!  ");

        assertEquals("DemoPassword123!", properties.requireDefaultPassword());
    }
}
