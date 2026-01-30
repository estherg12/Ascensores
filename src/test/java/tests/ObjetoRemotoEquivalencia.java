package tests;

import java.rmi.RemoteException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import poo.ascensores.ObjetoRemoto;

public class ObjetoRemotoEquivalencia {
    private ObjetoRemoto instance;

    @BeforeEach
    public void setUp() throws RemoteException {
        // Escenario estándar: 3 ascensores, plantas de -2 a 10
        instance = new ObjetoRemoto(3, -2, 10, "MAS_CERCANO", null);
    }

    /**
     * Caso: Inversión de límites
     * Detecta si el sistema permite un estado donde el mínimo es mayor que el máximo
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testSetLimitesInvertidos() throws RemoteException {
        System.out.println("Prueba: Inversión de límites");
        instance.setPlantaMax(5);
        instance.setPlantaMin(10); // Caso no válido
        
        // El sistema debería impedir esto o ajustar los límites.
        // Si permite PlantaMin > PlantaMax, los algoritmos de distancia fallarán
        assertTrue(instance.getPlantaMin() < instance.getPlantaMax(), 
                "ERROR: El sistema permitió que la planta mínima sea mayor que la máxima");
    }

    /**
     * Caso: ID Inexistente en liberación
     * Verifica que el sistema maneje el error al intentar acceder a un índice fuera del ArrayList
     */
    @Test
    public void testLiberarIDAscensorInexistente() {
        System.out.println("Prueba: Liberar ID inexistente");
        assertThrows(Exception.class, () -> {
            instance.liberarAscensor(99); 
        }, "Debe lanzar una excepción al intentar acceder al ID 99 cuando solo hay 3 ascensores");
    }

    /**
     * Caso: Algoritmo nulo o desconocido
     * Verifica la robustez del 'switch' ante entradas no contempladas
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testSetAlgoritmoInvalido() throws RemoteException {
        System.out.println("Prueba: Algoritmo nulo");
        instance.setAlgoritmo(null); 
        
        // Si el algoritmo es null, ejecutarAlgoritmo lanzará NullPointerException en el switch
        assertNotNull(instance.getAlgoritmo(), "El algoritmo no debería poder ser nulo");
    }

    /**
     * Caso: Número de ascensores negativo
     * Verifica que la gestión de memoria (ArrayList.remove) no intente procesar índices negativos
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testSetNumAscensoresNegativo() throws RemoteException {
        System.out.println("Prueba: Número de ascensores negativo");
        instance.setNumAscensores(-1);
        
        // Una cantidad negativa no tiene sentido físico. El sistema debería ignorarla o poner 1
        assertTrue(instance.getNumAscensores() >= 0, "El número de ascensores no puede ser negativo");
    }

    /**
     * Caso: Movimiento redundante (Destino = Origen)
     * Verifica que el bucle 'while (origen != destino)' no cause un bloqueo o comportamiento extraño
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testIrAPlantaMismoOrigen() throws RemoteException {
        System.out.println("Prueba: Destino igual a origen");
        int plantaActual = instance.getAscensores().get(0).getPlantaActual();
        
        // El método debería retornar casi instantáneamente sin entrar en el bucle de sueño (Thread.sleep)
        long inicio = System.currentTimeMillis();
        instance.irAPlanta(0, plantaActual, 123);
        long fin = System.currentTimeMillis();
        
        assertTrue((fin - inicio) < 1000, "ERROR: El ascensor tardó demasiado en 'moverse' a la misma planta");
    }

    /**
     * Caso: Liberación de ascensor ya libre (Suplantación/Consistencia)
     * Verifica si liberar un ascensor que ya está libre afecta a las estadísticas
     * @throws java.rmi.RemoteException     */
    @Test
    public void testLiberarAscensorYaLibre() throws RemoteException {
        System.out.println("Prueba: Doble liberación");
        instance.liberarAscensor(0); // Primera vez (válido)
        
        // Segunda vez: No debería causar errores ni decrementar contadores erróneamente
        assertDoesNotThrow(() -> {
            instance.liberarAscensor(0);
        });
    }
}
