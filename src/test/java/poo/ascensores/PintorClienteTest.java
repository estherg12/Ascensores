
package poo.ascensores;

import java.rmi.RemoteException;
import java.util.ArrayList;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PintorClienteTest {
    private PintorCliente pintorBajoPrueba;
    private InterfazRemota mockServidor; // Objeto simulado
    private VistaCliente vista;
    
    public PintorClienteTest() {
    }
    
    @BeforeEach
    public void setUp() throws RemoteException {
        vista = new VistaCliente();
        mockServidor = createMock(InterfazRemota.class); // Crear mock
        pintorBajoPrueba = new PintorCliente(vista, mockServidor, 894, 0);
    }

    @Test
    public void testIntegracion_CicloActualizacionExitoso() throws RemoteException {
        System.out.println("Caso 1: Integración - Flujo de datos normal");

        // Grabar expectativas del servidor
        ArrayList<Ascensor> listaSimulada = new ArrayList<>();
        listaSimulada.add(new Ascensor(0));
        
        expect(mockServidor.getAscensores()).andReturn(listaSimulada); // Esperamos petición de lista 
        expect(mockServidor.getPlantaMax()).andReturn(10);           // Esperamos petición de límites
        expect(mockServidor.getPlantaMin()).andReturn(-2);
        // Pasar el mock a estado de escucha
        replay(mockServidor);

        // EJECUCIÓN: Forzamos a la vista a procesar los datos del mock
        vista.setPlantaMax(mockServidor.getPlantaMax());
        vista.setPlantaMin(mockServidor.getPlantaMin());
        vista.actualizarDatos(mockServidor.getAscensores());
        // Verificar aserciones
        assertEquals(10, vista.getPlantaMax(), "La vista debería haber integrado el valor 10 del servidor mock");
        assertEquals(-2, vista.getPlantaMin());
        
        // Verificar que se llamaron a todos los métodos del servidor
        verify(mockServidor);
    }

    @Test
    public void testIntegracion_FalloServidor() throws RemoteException {
        System.out.println("Caso 3: Integración - Caída del servidor RMI");

        // Grabamos la expectativa de que el servidor lance una excepción de red
        expect(mockServidor.getAscensores()).andThrow(new RemoteException("Conexión perdida"));

        replay(mockServidor);
        try {
        pintorBajoPrueba.getIr().getAscensores();
    } catch (RemoteException e) {
        System.out.println("Excepción capturada correctamente en el test");
    }

    verify(mockServidor);
    }
}
