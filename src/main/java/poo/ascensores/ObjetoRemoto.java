
package poo.ascensores;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

public class ObjetoRemoto extends UnicastRemoteObject implements InterfazRemota {
    private int numAscensores;
    private int plantaMin, plantaMax;
    private Ascensor[] ascensores;
    private Control vista;
    ArrayList<Ascensor> libres = new ArrayList<>();
    
    private String algoritmoActual;
    
    // Estadísticas
    private long tiempoTotalEspera = 0;
    private int peticionesCompletadas = 0;

    public ObjetoRemoto(int n, int min, int max, String algoritmo, Control vista) throws RemoteException {
        this.numAscensores = n;
        this.plantaMin = min;
        this.plantaMax = max;
        this.ascensores = new Ascensor[n];
        for (int i = 0; i < n; i++) {
            ascensores[i] = new Ascensor(i);
        }
        System.out.println(Arrays.toString(ascensores));
        this.algoritmoActual = algoritmo;
        this.vista=vista;
        actualizarGUI();
    }
    
    private void actualizarGUI() {
        if (vista != null) {
            double media = peticionesCompletadas == 0 ? 0 : (double) (tiempoTotalEspera / peticionesCompletadas) / 1000;
            vista.actualizarDatos(ascensores, media);
        }
    }

    public synchronized int solicitarAscensor(int plantaOrigen, int idCliente) throws RemoteException {
        System.out.println("Cliente " + idCliente + " solicita ascensor en planta " + plantaOrigen);
        long inicioPeticion = System.currentTimeMillis();

        int idSeleccionado = -1;
        // Algoritmo: El ascensor libre más cercano
        while (idSeleccionado == -1) {
            idSeleccionado = ejecutarAlgoritmo(plantaOrigen);
            
            if (idSeleccionado == -1) {
                try {
                    System.out.println("Cliente " + idCliente + " esperando: No hay ascensores libres.");
                    wait(); 
                } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }

        Ascensor elegido = ascensores[idSeleccionado];
        elegido.setOcupado(true);
        elegido.setIdClienteActual(idCliente);
        moverAscensor(idSeleccionado, plantaOrigen, idCliente);
        actualizarGUI();
        
        tiempoTotalEspera += (System.currentTimeMillis() - inicioPeticion);
        peticionesCompletadas++;
        actualizarGUI();
        
        return idSeleccionado;
    }
    
    private int ejecutarAlgoritmo(int plantaOrigen) {        
        int result = 0;
        
        for (Ascensor a : ascensores) {
            if (!a.isOcupado()) {
                libres.add(a);
            }
        }

        if (libres.isEmpty()) {
            result = -1;
        }
        else {

            Ascensor seleccionado = libres.get(0);

            for (Ascensor candidato : libres) {
                switch (algoritmoActual) {
                    case "MAS_CERCANO" -> {
                        if (distancia(candidato, plantaOrigen) < distancia(seleccionado, plantaOrigen)) {
                            seleccionado = candidato;
                        }
                    }

                    case "MAS_LEJANO" -> {
                        if (distancia(candidato, plantaOrigen) > distancia(seleccionado, plantaOrigen)) {
                            seleccionado = candidato;
                        }
                    }

                    case "MAS_INACTIVO" -> {
                        // Comparamos el timestamp (el menor valor es el que más tiempo lleva parado)
                        if (candidato.getUltimaVezMovido() < seleccionado.getUltimaVezMovido()) {
                            seleccionado = candidato;
                        }
                    }

                    case "MENOS_INACTIVO" -> {
                        // El mayor valor es el que se movió más recientemente
                        if (candidato.getUltimaVezMovido() > seleccionado.getUltimaVezMovido()) {
                            seleccionado = candidato;
                        }
                    }
                }
            }
            result = seleccionado.getId();
        }
        return result;
    }
    
    private int distancia(Ascensor a, int planta) {
        return Math.abs(a.getPlantaActual() - planta);
    }

    public void irAPlanta(int idAscensor, int plantaDestino, int idCliente) throws RemoteException {
        System.out.println("Cliente " + idCliente + " va a planta " + plantaDestino + " en ascensor " + idAscensor);
        Ascensor a = ascensores[idAscensor];
        moverAscensor(a.getId(), plantaDestino, idCliente);
        actualizarGUI();
    }
    
    public synchronized void moverAscensor(int id, int destino, int idCliente) throws RemoteException {
        int origen = ascensores[id].getPlantaActual();
        int direccion = (destino > origen) ? 1 : -1;
        
        while (origen != destino)
        {
            origen += direccion;
            int aux = origen;
            try
            {
                Thread.sleep(1000);
                ascensores[id].setPlantaActual(aux);
                System.out.println("[Ascensor " + id + "] En planta: " + ascensores[id].getPlantaActual());
                actualizarGUI();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Ascensor " + id + " ha llegado a planta " + destino);
    }

    public synchronized void liberarAscensor(int idAscensor) throws RemoteException {
        ascensores[idAscensor].setOcupado(false);
        System.out.println("Ascensor " + idAscensor + " liberado.");
        ascensores[idAscensor].setIdClienteActual(-1);
        actualizarGUI();
        notifyAll(); // Despertar a clientes esperando
    }

    public String obtenerEstadisticas() throws RemoteException {
        String resultado = "";
        if (peticionesCompletadas == 0) {
            resultado = "No hay datos.";
        } else
        {
            resultado = "Tiempo medio de espera: " + (tiempoTotalEspera / peticionesCompletadas) / 1000 + " segundos.";
        }
        
        return resultado;
    }
    
    // Permite cambiar el algoritmo en tiempo de ejecución
    public void setAlgoritmo(String nuevo) throws RemoteException {
        this.algoritmoActual = nuevo;
        System.out.println("Algoritmo cambiado a: " + nuevo);
    }

    public int getNumAscensores() throws RemoteException {
        return numAscensores;
    }

    public void setNumAscensores(int numAscensores) throws RemoteException {
        this.numAscensores = numAscensores;
        this.ascensores = new Ascensor[numAscensores];
        for (int i = 0; i < numAscensores; i++) {
            ascensores[i] = new Ascensor(i);
        }
        actualizarGUI();
    }

    public int getPlantaMin() throws RemoteException {
        return plantaMin;
    }

    public void setPlantaMin(int plantaMin) throws RemoteException {
        this.plantaMin = plantaMin;
    }

    public int getPlantaMax() throws RemoteException {
        return plantaMax;
    }

    public void setPlantaMax(int plantaMax) throws RemoteException {
        this.plantaMax = plantaMax;
    }

    public long getTiempoTotalEspera() throws RemoteException {
        return tiempoTotalEspera;
    }

    public void setTiempoTotalEspera(long tiempoTotalEspera) throws RemoteException {
        this.tiempoTotalEspera = tiempoTotalEspera;
    }
    
    public Ascensor[] getAscensores() throws RemoteException {
        return ascensores;
    }

    public void setAscensores(Ascensor[] ascensores) throws RemoteException {
        this.ascensores = ascensores;
    }
    
}
