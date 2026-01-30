
package poo.ascensores;
import java.io.Serializable;

public class Ascensor implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int plantaActual;
    private boolean ocupado;
    private long ultimaVezMovido; // Para algoritmos de selección por tiempo
    private int idClienteActual = -1;
    
    public Ascensor(int id) {
        this.id = id;
        this.plantaActual = 0; // Todos inician en la planta 0
        this.ocupado = false;
        this.ultimaVezMovido = System.currentTimeMillis();
    }

    // Getters y Setters
    public int getId() { return id; }
    
    public synchronized int getPlantaActual() { return plantaActual; }
    
    public synchronized void setPlantaActual(int plantaActual) { 
        this.plantaActual = plantaActual; 
        this.ultimaVezMovido = System.currentTimeMillis();
    }

    public synchronized boolean isOcupado() { return ocupado; }
    
    public synchronized void setOcupado(boolean ocupado) { this.ocupado = ocupado; }

    public long getUltimaVezMovido() { return ultimaVezMovido; }

    public int getIdClienteActual() {
        return idClienteActual;
    }

    public void setIdClienteActual(int idClienteActual) {
        this.idClienteActual = idClienteActual;
    }
}
