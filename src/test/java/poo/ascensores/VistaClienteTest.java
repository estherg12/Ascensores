
package poo.ascensores;

import java.rmi.RemoteException;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VistaClienteTest {
    private VistaCliente vista;
    private InterfazRemota mockServidor;
    private final int ID_CLIENTE = 894;
    
    public VistaClienteTest() {
    }
    
    @BeforeEach
    public void setUp() throws RemoteException {
        // Crea los objetos para la prueba
        vista = new VistaCliente();
        mockServidor = createMock(InterfazRemota.class);
        vista.setInterfazRemota(mockServidor);
        vista.setIdCliente(ID_CLIENTE);
    }

    /**
     * Prueba de integración del flujo completo: Solicitar -> Ir -> Liberar
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testIntegracionBotonesFlujoCompleto() throws RemoteException {
        System.out.println("Caso Integración: Ciclo de vida de botones (Subir -> Ir)");

        // Esperamos que al pulsar 'Subir', se llame al servidor
        expect(mockServidor.solicitarAscensor(0, ID_CLIENTE)).andReturn(0);
        
        // Esperamos que al pulsar 'Ir', se mueva y luego se libere
        mockServidor.irAPlanta(0, 10, ID_CLIENTE);
        expectLastCall(); // Método void
        mockServidor.liberarAscensor(0);
        expectLastCall();

        // PASAR A ESTADO DE ESCUCHA
        replay(mockServidor);

        // EJECUTAR PRUEBA LÓGICA (Simulación de clics)
        vista.solicitarAscensor(); 
        assertTrue(vista.getIrButton().isEnabled(), "El botón IR debería habilitarse tras recibir ascensor");
        assertFalse(vista.getSubirButton().isEnabled(), "Botones de solicitud deben bloquearse");

        // Configurar destino y simular clic en "Ir"
        vista.getPlantasSpinner().setValue(10);
        vista.getIrButton().doClick(); // Ejecuta irAPlanta y liberarAscensor

        // VERIFICACIÓN FINAL
        verify(mockServidor);
        assertFalse(vista.getIrButton().isEnabled(), "El botón IR debe volver a bloquearse");
        assertTrue(vista.getSubirButton().isEnabled(), "Botones de solicitud deben habilitarse tras liberar");
    }

    /**
     * Prueba de Robustez: Error en la llamada remota durante el trayecto
     * @throws java.rmi.RemoteException
     */
    @Test
    public void testIntegracionFalloRedAlIr() throws RemoteException {
        System.out.println("Caso Integración: Error RMI al pulsar IR");

        // GRABACIÓN: Grabamos que el cliente intentará ir a la PLANTA 5
        expect(mockServidor.solicitarAscensor(0, ID_CLIENTE)).andReturn(1);
        mockServidor.irAPlanta(1, 5, ID_CLIENTE); // El parámetro destino es 5
        expectLastCall().andThrow(new RemoteException("Conexión perdida"));

        replay(mockServidor);

        // EJECUCIÓN
        vista.solicitarAscensor();
        vista.getPlantasSpinner().setValue(5); 

        // Al fallar la red, el sistema debería manejar la excepción sin colapsar la GUI
        assertDoesNotThrow(() -> {
            vista.getIrButton().doClick();
        }, "La GUI debe capturar el error de red y no propagarlo");

        verify(mockServidor);
    }

}
