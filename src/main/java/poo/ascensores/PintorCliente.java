
package poo.ascensores;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PintorCliente extends Thread {
    private VistaCliente vista;
    private InterfazRemota ir;
    private Persona cliente;
    private Ascensor[] ascensores;

    public PintorCliente(VistaCliente vista, InterfazRemota ir, Persona cl) {
        this.vista = vista;
        this.ir = ir;
        this.cliente = cl;
    }    
    
    public void run()
    {
        try {
            vista.setInterfazRemota(ir);
            vista.setVisible(true);
        } catch (RemoteException ex) {
            Logger.getLogger(PintorCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while(true)
        {
            try {
                ascensores = ir.getAscensores();
                vista.actualizarDatos(ascensores);
            } catch (RemoteException ex) {
                Logger.getLogger(PintorCliente.class.getName()).log(Level.SEVERE, null, ex);
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

    public Persona getCliente() {
        return cliente;
    }

    public void setCliente(Persona cliente) {
        this.cliente = cliente;
    }
    
    
}
