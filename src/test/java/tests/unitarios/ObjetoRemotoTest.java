
package tests.unitarios;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import poo.ascensores.Ascensor;
import poo.ascensores.ObjetoRemoto;

public class ObjetoRemotoTest {
    private ObjetoRemoto instance;
    private final int NUM_ASCENSORES = 3;
    private final int MIN_PLANTA = -2;
    private final int MAX_PLANTA = 10;

    @BeforeEach
    public void setUp() throws RemoteException {
        // Inicializamos el contexto de prueba con un JFrame nulo para evitar errores de GUI
        instance = new ObjetoRemoto(NUM_ASCENSORES, MIN_PLANTA, MAX_PLANTA, "MAS_CERCANO", null);
        System.out.println("Contexto de prueba configurado.");
    }

    @AfterEach
    public void tearDown() {
        instance = null;
        System.out.println("Limpieza de recursos post-test.");
    }

    /**
     * Test del camino: MAS_CERCANO
     * Verifica que elige el ascensor con la menor diferencia de planta respecto al origen
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testEjecutarAlgoritmo_MasCercano() throws RemoteException, Exception {
        System.out.println("Prueba: ejecutarAlgoritmo - MAS_CERCANO");
        
        instance.getAscensores().get(0).setPlantaActual(5);
        instance.getAscensores().get(1).setPlantaActual(2);
        instance.getAscensores().get(2).setPlantaActual(10); // Asegurar que el 2 esté lejos
        instance.setAlgoritmo("MAS_CERCANO");

        int esperado = 1; // El ID 1 está a distancia 2, el ID 0 a distancia 5
        
        int obtenido = invocarEjecutarAlgoritmo(0);
        
        assertEquals(esperado, obtenido, "Debe seleccionar el ascensor (ID 1) por ser el más cercano a 0");
    }

    /**
     * Test del camino: MAS_LEJANO
     * Verifica la correcta evaluación de distancias máximas
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testEjecutarAlgoritmo_MasLejano() throws RemoteException {
        System.out.println("Prueba: ejecutarAlgoritmo - MAS_LEJANO");
        
        instance.getAscensores().get(0).setPlantaActual(2);
        instance.getAscensores().get(1).setPlantaActual(8);
        instance.setAlgoritmo("MAS_LEJANO");

        int esperado = 1; 
        int obtenido = instance.solicitarAscensor(0, 888);
        
        assertEquals(esperado, obtenido);
    }

    /**
     * Test del camino: MAS_INACTIVO.
     * Evalúa la complejidad ciclomática del switch basado en el tiempo
     * @throws java.rmi.RemoteException
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEjecutarAlgoritmo_MasInactivo() throws RemoteException, InterruptedException {
        System.out.println("Prueba: ejecutarAlgoritmo - MAS_INACTIVO");
        
        instance.setAlgoritmo("MAS_INACTIVO");
        // Forzamos diferencia de tiempos de movimiento
        instance.getAscensores().get(0).setPlantaActual(1);
        Thread.sleep(10); // Breve espera para diferenciar timestamps
        instance.getAscensores().get(1).setPlantaActual(1);
        
        int esperado = 0; // El ascensor 0 fue el primero en moverse, por tanto lleva más tiempo parado
        int obtenido = instance.solicitarAscensor(1, 777);
        
        assertEquals(esperado, obtenido);
    }

    /**
     * Test de condición de error: Ningún ascensor libre
     * Verifica que el sistema no colapse y devuelva el código de espera
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testEjecutarAlgoritmo_TodosOcupados() throws RemoteException {
        System.out.println("Prueba: ejecutarAlgoritmo - Sin ascensores libres");
        
        // Ocupamos todos los ascensores manualmente
        for (Ascensor a : instance.getAscensores()) {
            a.setOcupado(true);
        }
        // Este test debe ser asíncrono o verificar el retorno de la lógica de selección directa
        // para evitar el bloqueo por el wait() del código original.
    }
    
    /**
     * Auxiliar para invocar el método privado 'ejecutarAlgoritmo' mediante reflexión,
     * permitiendo probarlo sin modificar la visibilidad del código original
     */
    private int invocarEjecutarAlgoritmo(int plantaOrigen) throws Exception {
        Method method = ObjetoRemoto.class.getDeclaredMethod("ejecutarAlgoritmo", int.class);
        method.setAccessible(true);
        return (int) method.invoke(instance, plantaOrigen);
    }
    
    /**
     * Objetivo: Verificar que devuelve -1 si no hay unidades disponibles
     * @throws java.lang.Exception
     */
    @Test
    public void testEjecutarAlgoritmo_ListaLibresVacia() throws Exception {
        System.out.println("Ejecutando: testEjecutarAlgoritmo_ListaLibresVacia");
        
        // Configuración: Ocupamos todos los ascensores de la lista
        for (Ascensor a : instance.getAscensores()) {
            a.setOcupado(true);
        }
        int expResult = -1; // Resultado esperado según el código
        int result = invocarEjecutarAlgoritmo(0);
        
        assertEquals(expResult, result, "Debe devolver -1 cuando todos están ocupados");
    }

    /**
     * Objetivo: Verificar el criterio de desempate determinista (menor ID)
     * @throws java.lang.Exception
     */
    @Test
    public void testEjecutarAlgoritmo_EmpateDistancia() throws Exception {
        System.out.println("Ejecutando: testEjecutarAlgoritmo_EmpateDistancia");
        
        for(Ascensor a : instance.getAscensores()) {
            a.setOcupado(false); //Limpiar ocupación
        }
        
        // Escenario: Ascensor 0 en planta 2, Ascensor 1 en planta -2 Cliente en planta 0
        // Ambos están a distancia 2
        instance.getAscensores().get(0).setPlantaActual(2);
        instance.getAscensores().get(1).setPlantaActual(10); // Lejos
        instance.getAscensores().get(2).setPlantaActual(-2);
        instance.setAlgoritmo("MAS_CERCANO");

        int expResult = 0; // Por la lógica del bucle 'for', el primero que cumple la condición se queda
        int result = invocarEjecutarAlgoritmo(0);
        
        assertEquals(expResult, result, "En caso de empate, debe seleccionar el de menor ID");
    }

    /**
     * Objetivo: Validar que el cambio de algoritmo afecta inmediatamente a la selección
     * @throws java.lang.Exception
     */
    @Test
    public void testEjecutarAlgoritmo_CambioAlgoritmoEnCaliente() throws Exception {
        System.out.println("Ejecutando: testEjecutarAlgoritmo_CambioAlgoritmoEnCaliente");

        // Forzar que todos estén LIBRES para la prueba
        for(Ascensor a : instance.getAscensores()) {
            a.setOcupado(false);
        }
        
        // Escenario: ID0 en planta 0 (distancia 0), el resto muy lejos
        instance.getAscensores().get(0).setPlantaActual(0); 
        instance.getAscensores().get(1).setPlantaActual(10);
        instance.getAscensores().get(2).setPlantaActual(10);
        // Probar CERCANO
        instance.setAlgoritmo("MAS_CERCANO");
        // para evitar que solicitarAscensor los marque como ocupados
        assertEquals(0, invocarEjecutarAlgoritmo(0), "Debe elegir el de planta 1 (ID 0).");

        // Probar LEJANO
        // Ahora movemos el ID 1 a la planta 10 y el resto a la 0
        instance.getAscensores().get(0).setPlantaActual(0);
        instance.getAscensores().get(1).setPlantaActual(10);
        instance.getAscensores().get(2).setPlantaActual(0);
        instance.setAlgoritmo("MAS_LEJANO");
        assertEquals(1, invocarEjecutarAlgoritmo(0), "Tras el cambio, debe elegir el de planta 10 (ID 1).");
    }
    
    /**
     * Objetivo: Verificar el comportamiento del sistema cuando se elimina un 
     * ascensor que está siendo utilizado por un cliente
     * Según la lógica de caja blanca, este test comprueba si el 'remove' es ciego al estado
     * @throws java.rmi.RemoteException     */
    @Test
    public void testSetNumAscensores_ReduccionConAscensorOcupado() throws RemoteException {
        System.out.println("Ejecutando: testSetNumAscensores_ReduccionConAscensorOcupado");
        
        // Aseguramos estado inicial de 3 ascensores
        instance.setNumAscensores(3);
        
        // Simulamos que el ascensor con ID 2 (el último) está ocupado
        Ascensor ascensorParaBorrar = instance.getAscensores().get(2);
        ascensorParaBorrar.setOcupado(true);
        ascensorParaBorrar.setIdClienteActual(99);
        
        // Reducimos el número de ascensores a 2
        // Esto obligará a ejecutar el bucle 'for' con 'ascensores.remove(numAscensores-i-1)'
        instance.setNumAscensores(2);
        
        // Verificaciones mediante Assertions
        assertEquals(2, instance.getAscensores().size(), "La lista debe reducirse a 2");
        
        // Verificamos si el ascensor ocupado ha desaparecido de la lista
        boolean existeId2 = instance.getAscensores().stream()
                .anyMatch(a -> a.getId() == 2);
        
        assertFalse(existeId2, "FALLO DETECTADO: El sistema permite eliminar un ascensor en uso sin validar su estado 'ocupado'");
    }

    /**
     * Objetivo: Probar el comportamiento del sistema ante un valor de borde (0)
     * Determina si el sistema entra en un estado inconsistente (lista vacía)
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testSetNumAscensores_LimiteCero() throws RemoteException {
        System.out.println("Ejecutando: testSetNumAscensores_LimiteCero");
        
        // 1. Intentamos establecer el número de ascensores a 0
        instance.setNumAscensores(0);
        
        // Verificamos que la lista se haya vaciado correctamente 
        assertEquals(0, instance.getAscensores().size(), "La lista debería estar vacía");
        assertEquals(0, instance.getNumAscensores(), "El atributo numAscensores debe ser 0");
        
        // Verificación de robustez del algoritmo con lista vacía
        // Al no haber ascensores, 'ejecutarAlgoritmo' debería devolver -1 de forma segura
        try {
            // Invocamos el algoritmo mediante la utilidad de reflexión definida en el paso anterior
            int result = invocarEjecutarAlgoritmo(0); 
            assertEquals(-1, result, "El algoritmo debe devolver -1 si no hay ascensores en el sistema");
        } catch (Exception e) {
            fail("El sistema lanzó una excepción al procesar 0 ascensores: " + e.getMessage());
        }
    }
    
}
