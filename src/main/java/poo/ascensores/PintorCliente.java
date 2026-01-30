
package poo.ascensores;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PintorCliente extends Thread {
    private VistaCliente vista;
    private InterfazRemota ir;
    private int idCliente = 0;
    private int plantaActual = 0;
    private ArrayList<Ascensor> ascensores;

    public PintorCliente(VistaCliente vista, InterfazRemota ir, int id, int pl) {
        this.vista = vista;
        this.ir = ir;
        this.idCliente = id;
        this.plantaActual = pl;
    }    
    
    public void run()
    {
        vista.setInterfazRemota(ir);
        vista.setIdCliente(idCliente);
        vista.setVisible(true);
        
        while(true)
        {
            try {
                ascensores = ir.getAscensores();
                vista.actualizarDatos(ascensores);
                vista.setPlantaMax(ir.getPlantaMax());
                vista.setPlantaMin(ir.getPlantaMin());
            } catch (RemoteException ex) {
                System.out.println("Problema de conexión con el Servidor. Parece que se ha finalizado la ejecución del servidor. Se procederá a cerrar el sistema");
                vista.dispose();
                break;
            }
            try {
                sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(PintorCliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public VistaCliente getVista() {
        return vista;
    }

    public void setVista(VistaCliente vista) {
        this.vista = vista;
    }

    public InterfazRemota getIr() {
        return ir;
    }

    public void setIr(InterfazRemota ir) {
        this.ir = ir;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getPlantaActual() {
        return plantaActual;
    }

    public void setPlantaActual(int plantaActual) {
        this.plantaActual = plantaActual;
    }
    
}
