
package poo.ascensores;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Servidor {

    public static void main(String[] args) {
        // TODO code application logic here
        
        try {
            int n = 3; // 3 ascensores
            int min = -2, max = 10; // plantas
            
            LocateRegistry.createRegistry(1099);
            
            Control v = new Control();
            v.setVisible(true);
            ObjetoRemoto objetoRemoto = new ObjetoRemoto(n, min, max, "MAS_CERCANO", v);
            System.out.println("Iniciado el Objeto Remoto");
            v.setObjeto(objetoRemoto);
            Naming.rebind("//localhost/Ascensor", objetoRemoto);
            
            System.out.println("Servidor de ascensores listo con " + n + " unidades.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
