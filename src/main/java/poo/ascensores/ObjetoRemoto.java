
package poo.ascensores;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ObjetoRemoto extends UnicastRemoteObject implements InterfazRemota {
    private int numAscensores;
    private int plantaMin, plantaMax;
    private ArrayList<Ascensor> ascensores;
    private Control vista;
    
    private String algoritmoActual;
    
    // Estadísticas
    private long tiempoTotalEspera = 0;
    private int peticionesCompletadas = 0;

    public ObjetoRemoto(int n, int min, int max, String algoritmo, Control vista) throws RemoteException {
        this.numAscensores = n;
        this.plantaMin = min;
        this.plantaMax = max;
        this.ascensores = new ArrayList<Ascensor>();
        for (int i = 0; i < n; i++) {
            Ascensor a = new Ascensor(i);
            ascensores.add(a);
        }
        System.out.println(ascensores);
        this.algoritmoActual = algoritmo;
        this.vista=vista;
        actualizarGUI();
    }
    
    private synchronized void actualizarGUI() {
        if (vista != null) {
            double media = peticionesCompletadas == 0 ? 0 : (double) (tiempoTotalEspera / peticionesCompletadas) / 1000;
            vista.actualizarDatos(ascensores, media);
        }
    }

    public synchronized int solicitarAscensor(int plantaOrigen, int idCliente) throws RemoteException {
        System.out.println(fechaHora()+"Cliente " + idCliente + " solicita ascensor en planta " + plantaOrigen);
        long inicioPeticion = System.currentTimeMillis();

        int idSeleccionado = -1;
        // Algoritmo: El ascensor libre más cercano
        while (idSeleccionado == -1) {
            idSeleccionado = ejecutarAlgoritmo(plantaOrigen);
            
            if (idSeleccionado == -1) {
                try {
                    System.out.println(fechaHora()+"Cliente " + idCliente + " esperando: No hay ascensores libres.");
                    wait(); 
                } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }

        Ascensor elegido = ascensores.get(idSeleccionado);
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
        ArrayList<Ascensor> libres = new ArrayList<>();        
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

            Ascensor seleccionado = null;

            for (Ascensor candidato : libres) {
                if(seleccionado == null)
                {
                    seleccionado = candidato;
                    continue;
                }
                
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
            result = seleccionado != null ? seleccionado.getId() : -1;
        }
        return result;
    }
    
    private int distancia(Ascensor a, int planta) {
        return Math.abs(a.getPlantaActual() - planta);
    }

    public synchronized void irAPlanta(int idAscensor, int plantaDestino, int idCliente) throws RemoteException {
        System.out.println(fechaHora()+"Cliente " + idCliente + " va a planta " + plantaDestino + " en ascensor " + idAscensor);
        Ascensor a = ascensores.get(idAscensor);
        moverAscensor(a.getId(), plantaDestino, idCliente);
        actualizarGUI();
    }
    
    public synchronized void moverAscensor(int id, int destino, int idCliente) throws RemoteException {
        int origen = ascensores.get(id).getPlantaActual();
        int direccion = (destino > origen) ? 1 : -1;
        
        while (origen != destino)
        {
            // VALIDACIÓN CRÍTICA: ¿El destino sigue estando dentro de los límites actuales?
            if (destino > plantaMax || destino < plantaMin) {
                System.out.println(fechaHora() + "[Ascensor " + id + "] ABORTANDO: Destino fuera de límites");
                break; 
            }
            
            origen += direccion;
            int aux = origen;
            try
            {
                Thread.sleep(5000); // 5 segundos por planta
                ascensores.get(id).setPlantaActual(aux);
                System.out.println(fechaHora()+"[Ascensor " + id + "] En planta: " + ascensores.get(id).getPlantaActual());
                actualizarGUI();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println(fechaHora()+"Ascensor " + id + " ha llegado a planta " + destino);
    }

    public synchronized void liberarAscensor(int idAscensor) throws RemoteException {
        ascensores.get(idAscensor).setOcupado(false);
        System.out.println(fechaHora()+"Ascensor " + idAscensor + " liberado.");
        ascensores.get(idAscensor).setIdClienteActual(-1);
        actualizarGUI();
        notifyAll(); // Despertar a clientes esperando
    }

    public String obtenerEstadisticas() throws RemoteException {
        String resultado = "";
        if (peticionesCompletadas == 0) {
            resultado = fechaHora()+"No hay datos.";
        } else
        {
            resultado = fechaHora()+"Tiempo medio de espera: " + (tiempoTotalEspera / peticionesCompletadas) / 1000 + " segundos.";
        }
        
        return resultado;
    }
    
    public synchronized String fechaHora() throws RemoteException
    {
        LocalDateTime hoy = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String fechaHora = hoy.format(formatter);
        String nuevoEvento = "["+fechaHora+"] - ";
        return nuevoEvento;
    }
    
    public String getAlgoritmo() throws RemoteException {
        return this.algoritmoActual;
    }
    
    // Permite cambiar el algoritmo en tiempo de ejecución
    public void setAlgoritmo(String nuevo) throws RemoteException {
        if (nuevo != null && !nuevo.trim().isEmpty()) {
            this.algoritmoActual = nuevo;
            System.out.println(fechaHora()+"Algoritmo cambiado a: " + nuevo);
        }
    }

    public int getNumAscensores() throws RemoteException {
        return numAscensores;
    }

    public void setNumAscensores(int nuevoNumAscensores) throws RemoteException {
        // Validación de seguridad (Clase de Equivalencia Inválida)
        if (nuevoNumAscensores < 0) {
            System.out.println(fechaHora() + "Error: Intento de establecer número de ascensores inválido.");
        }
        else
        {
            if(this.numAscensores > nuevoNumAscensores)
            {
                // Hay que quitar ascensores (filas)
                int ascensoresEliminar = numAscensores - nuevoNumAscensores;
                for (int i = 0; i < ascensoresEliminar; i++)
                {
                    ascensores.remove(numAscensores-i-1);
                }
            } else if (this.numAscensores < nuevoNumAscensores)
            {
                // Hay que añadir nuevos ascensores (filas) vacíos
                int ascensoresAñadir = nuevoNumAscensores - numAscensores;
                for (int i = 0; i<ascensoresAñadir; i++)
                {
                    Ascensor a = new Ascensor(numAscensores+i);
                    ascensores.add(a);
                }
            }

            this.numAscensores = nuevoNumAscensores;
            actualizarGUI();
        }
    }

    public int getPlantaMin() throws RemoteException {
        return plantaMin;
    }

    public void setPlantaMin(int plantaMin) throws RemoteException {
        if (plantaMin >= this.plantaMax) {
            System.out.println(fechaHora() + "Error: La planta mínima no puede superar la máxima.");
        }
        else
        {
            this.plantaMin = plantaMin;  
        }
    }

    public int getPlantaMax() throws RemoteException {
        return plantaMax;
    }

    public void setPlantaMax(int plantaMax) throws RemoteException {
        if (plantaMax <= this.plantaMin) {
            System.out.println(fechaHora() + "Error: La planta máxima no puede ser menor que la mínima.");
        } else {
            this.plantaMax = plantaMax;
        }
    }

    public long getTiempoTotalEspera() throws RemoteException {
        return tiempoTotalEspera;
    }

    public void setTiempoTotalEspera(long tiempoTotalEspera) throws RemoteException {
        this.tiempoTotalEspera = tiempoTotalEspera;
    }

    public ArrayList<Ascensor> getAscensores() throws RemoteException {
        return ascensores;
    }

    public void setAscensores(ArrayList<Ascensor> ascensores) throws RemoteException {
        this.ascensores = ascensores;
    }
    
}
