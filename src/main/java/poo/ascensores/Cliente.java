
package poo.ascensores;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Cliente {

    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        try {
            InterfazRemota stub = (InterfazRemota) Naming.lookup("//localhost/Ascensor");
            VistaCliente vista = new VistaCliente();
            
            int idCliente = (int)(Math.random() * 1000);
            PintorCliente pc = new PintorCliente(vista, stub, idCliente, 0);
            pc.start();
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
    }    
}
