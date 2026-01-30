
package poo.ascensores;

import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Persona extends Thread{
    
    private int plantaActual = 0;
    private int idCliente = (int)(Math.random() * 1000);
    private boolean dentro = true;
    private InterfazRemota stub;
    Scanner sc = new Scanner(System.in);

    public Persona(InterfazRemota ir) {
        this.stub = ir;
    }
    
    public void run()
    {
        VistaCliente vista = new VistaCliente();
        PintorCliente pc = new PintorCliente(vista, stub, this);
        pc.start();
        
        while (dentro) {
            System.out.println("\nCliente " + idCliente + " en planta " + plantaActual);
            System.out.print("¿Desea solicitar un ascensor? (s/n): ");
            if (sc.next().equalsIgnoreCase("n")) {
                dentro=false;
            }

            // Llamar ascensor
            System.out.println("Esperando ascensor...");
            int idAscensor=0;
            try {
                idAscensor = stub.solicitarAscensor(plantaActual, idCliente);
            } catch (RemoteException ex) {
                Logger.getLogger(Persona.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("¡Ascensor " + idAscensor + " ha llegado!");

            // Elegir destino
            int destino = 0;
            int plantaMin=0, plantaMax=0;
            try {
                plantaMin = stub.getPlantaMin();
                plantaMax = stub.getPlantaMax();
            } catch (RemoteException ex) {
                Logger.getLogger(Persona.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            do{
                System.out.print("Plantas: ");
                for(int i = plantaMin; i <= plantaMax; i++)
                {
                    System.out.print(i+" ");
                }
                System.out.println("\n¿A qué planta desea ir?: ");
                destino = sc.nextInt();
            } while(destino < plantaMin || destino > plantaMax);

            try {
                stub.irAPlanta(idAscensor, destino, idCliente);
            } catch (RemoteException ex) {
                Logger.getLogger(Persona.class.getName()).log(Level.SEVERE, null, ex);
            }
            plantaActual = destino;
            System.out.println("Ha llegado a la planta " + plantaActual);
            vista.actualizarPlanta(plantaActual, plantaMin, plantaMax);

            try {
                // Liberar
                stub.liberarAscensor(idAscensor);
            } catch (RemoteException ex) {
                Logger.getLogger(Persona.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (plantaActual == 0) {
                System.out.println("Ha salido del edificio.");
                break;
            }
        }
    }

    public int getPlantaActual() {
        return plantaActual;
    }

    public void setPlantaActual(int plantaActual) {
        this.plantaActual = plantaActual;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public boolean isDentro() {
        return dentro;
    }

    public void setDentro(boolean dentro) {
        this.dentro = dentro;
    }

    public InterfazRemota getStub() {
        return stub;
    }

    public void setStub(InterfazRemota stub) {
        this.stub = stub;
    }
    
}
