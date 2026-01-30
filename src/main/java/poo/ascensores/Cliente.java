
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
            Persona p = new Persona(stub);
            p.start();
            
            p.join();
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
    }    
}
