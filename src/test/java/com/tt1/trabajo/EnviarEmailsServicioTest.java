package com.tt1.trabajo;
import static org.junit.jupiter.api.Assertions.*;
import servicios.EnviarEmails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import modelo.Destinatario;

class EnviarEmailsServiceTest {

    private EnviarEmails servicioEmails;

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger(EnviarEmails.class);
        servicioEmails = new EnviarEmails(logger);
    }

    @Test
    void testEnviarEmailLoggeaCorrectamente() {
        Destinatario destMock = null; 
        boolean resultado = servicioEmails.enviarEmail(destMock, "¡Hola, esto es una prueba!");
        assertTrue(resultado, "El método debería devolver true al simular el envío");
    }
}