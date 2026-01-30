
package poo.ascensores;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfazRemota extends Remote{
    public int solicitarAscensor(int plantaOrigen, int idCliente) throws RemoteException;
    public void moverAscensor(int id, int destino, int idCliente) throws RemoteException;
    public void irAPlanta(int idAscensor, int plantaDestino, int idCliente) throws RemoteException;
    public void liberarAscensor(int idAscensor) throws RemoteException;
    public String obtenerEstadisticas() throws RemoteException;
    public void setAlgoritmo(String nuevo) throws RemoteException ;
    public int getNumAscensores() throws RemoteException;
    public void setNumAscensores(int numAscensores) throws RemoteException;
    public int getPlantaMin() throws RemoteException;
    public void setPlantaMin(int plantaMin) throws RemoteException;
    public int getPlantaMax() throws RemoteException;
    public void setPlantaMax(int plantaMax) throws RemoteException;
    public long getTiempoTotalEspera() throws RemoteException;
    public void setTiempoTotalEspera(long tiempoTotalEspera) throws RemoteException;
    public Ascensor[] getAscensores() throws RemoteException;
    public void setAscensores(Ascensor[] ascensores) throws RemoteException;
}
