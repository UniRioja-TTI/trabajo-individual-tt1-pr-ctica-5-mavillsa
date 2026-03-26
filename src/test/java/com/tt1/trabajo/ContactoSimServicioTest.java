package com.tt1.trabajo;

import modelo.DatosSimulation;
import modelo.DatosSolicitud;
import modelo.Entidad;
import servicios.ContactoSimService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContactoSimServiceTest {

    private ContactoSimService contactoSimService;

    @BeforeEach
    void setUp() {
        // 1. Simulamos el Logger para poder pasárselo al constructor
        Logger mockLogger = Mockito.mock(Logger.class);
        
        // 2. Instanciamos tu servicio antes de cada test
        contactoSimService = new ContactoSimService(mockLogger);
    }

    @Test
    void testGetEntities() {
        // Ejecución
        List<Entidad> entidades = contactoSimService.getEntities();

        // Verificaciones
        assertNotNull(entidades, "La lista de entidades no debería ser null");
        assertEquals(3, entidades.size(), "El servicio debe inicializar exactamente 3 entidades");
        assertEquals("Parámetro 1", entidades.get(0).getName(), "El nombre de la primera entidad debe coincidir");
    }

    @Test
    void testIsValidEntityId() {
        // Verificamos los IDs válidos (1, 2 y 3 se crean en tu método inicializarEntidades)
        assertTrue(contactoSimService.isValidEntityId(1), "El ID 1 debería ser válido");
        assertTrue(contactoSimService.isValidEntityId(2), "El ID 2 debería ser válido");
        assertTrue(contactoSimService.isValidEntityId(3), "El ID 3 debería ser válido");

        // Verificamos un ID inválido
        assertFalse(contactoSimService.isValidEntityId(99), "El ID 99 debería ser inválido");
    }

    @Test
    void testSolicitarSimulation() {
        // Preparación: Usamos el constructor real de tu clase pasándole un mapa
        Map<Integer, Integer> parametrosMapa = new HashMap<>();
        parametrosMapa.put(1, 20); // Podemos meterle un dato de prueba cualquiera
        DatosSolicitud solicitud = new DatosSolicitud(parametrosMapa);
        
        // Ejecución
        int token = contactoSimService.solicitarSimulation(solicitud);

        // Verificaciones
        // El token debe estar entre 1000 y 9999 (1000 + random.nextInt(9000))
        assertTrue(token >= 1000 && token < 10000, "El token devuelto debe estar entre 1000 y 9999");
    }

    @Test
    void testDescargarDatos() {
        // Como actualmente tu método devuelve null, el test debe verificar que devuelve null.
        // OJO: Si más adelante implementas la llamada a la máquina virtual, este test fallará y tendrás que cambiarlo.
        DatosSimulation resultado = contactoSimService.descargarDatos(1234);
        
        assertNull(resultado, "Actualmente el método descargarDatos debe devolver null");
    }
}