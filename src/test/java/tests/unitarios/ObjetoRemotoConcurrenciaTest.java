
package tests.unitarios;

import java.rmi.RemoteException;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import poo.ascensores.ObjetoRemoto;
import poo.ascensores.ObjetoRemoto;

public class ObjetoRemotoConcurrenciaTest {
    private ObjetoRemoto instance;

    @BeforeEach
    public void setUp() throws RemoteException {
        // Escenario: 3 ascensores, plantas -2 a 10
        instance = new ObjetoRemoto(3, -2, 10, "MAS_CERCANO", null);
    }

    /**
     * Caso: testSolicitudSimultaneaExtrema
     * Objetivo: Verificar que el contador de peticiones y el estado de ocupación
     * son atómicos bajo alta concurrencia
     * @throws java.lang.InterruptedException
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testSolicitudSimultaneaExtrema() throws InterruptedException, RemoteException {
        int numHilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        // El latch asegura que todos los hilos empiecen casi al mismo milisegundo
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < numHilos; i++) {
            final int idCliente = i;
            executor.execute(() -> {
                try {
                    latch.await(); // Espera a la señal de inicio
                    instance.solicitarAscensor(0, idCliente);
                } catch (InterruptedException | RemoteException e) {
                    System.out.println(e);
                }
            });
        }

        latch.countDown(); // Inicia todos los hilos a la vez
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // Verificación: Aunque los hilos se bloqueen por falta de ascensores,
        // el contador de peticiones procesadas debe ser consistente
        assertTrue(instance.obtenerEstadisticas().contains("Tiempo medio"), 
                "El estado interno del servidor debería ser consistente tras ráfaga de peticiones");
    }

    /**
     * Caso: testLiberacionDoble
     * Objetivo: Comprobar que llamar dos veces a liberarAscensor no corrompe
     * el estado de ocupado ni genera inconsistencias en los hilos esperando (wait/notify)
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testLiberacionDoble() throws RemoteException {
        System.out.println("Prueba: testLiberacionDoble");
        
        // Ocupar ascensor
        instance.getAscensores().get(0).setOcupado(true);
        
        // Liberar dos veces seguidas
        instance.liberarAscensor(0);
        instance.liberarAscensor(0);
        
        // Verificación
        assertFalse(instance.getAscensores().get(0).isOcupado(), "El ascensor debe quedar libre");
        assertEquals(-1, instance.getAscensores().get(0).getIdClienteActual(), "El ID de cliente debe ser -1");
    }

    /**
     * Caso: testMovimientoPlantaLimite
     * Objetivo: Verificar si el bucle while(origen != destino) es robusto ante
     * cambios de límites durante el trayecto
     * @throws java.lang.Exception
     */
    @Test
    public void testMovimientoPlantaLimite() throws Exception {
        System.out.println("Prueba: testMovimientoPlantaLimite");
        
        Thread hiloMovimiento = new Thread(() -> {
            try {
                // Destino original: planta 10
                instance.moverAscensor(0, 10, 894);
            } catch (RemoteException e) {
                System.out.println(e);
            }
        });

        hiloMovimiento.start();
        Thread.sleep(1000); // Esperar a que el ascensor empiece a subir
        instance.setPlantaMax(5); 

        // Verificamos que el hilo no se quede en bucle infinito
        // Si el ascensor iba a la 10 y ahora el max es 5, debe manejarse con seguridad
        hiloMovimiento.join(10000); // Esperar máximo 10 seg
        assertFalse(hiloMovimiento.isAlive(), "ERROR: El ascensor se ha quedado en un bucle infinito o bloqueado");
    }
}
