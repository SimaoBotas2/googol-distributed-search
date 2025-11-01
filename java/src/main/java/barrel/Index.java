package barrel;

import java.rmi.*;
import java.util.*;

public interface Index extends Remote {

    //Handling da queue
    public String takeNext() throws RemoteException;
    public void putNew(String url) throws java.rmi.RemoteException;

    //Handling de varios barrels
    public void addToIndex(String word, String url) throws java.rmi.RemoteException;

    public List<String> searchWord(String word) throws java.rmi.RemoteException;

    public boolean ping() throws RemoteException;

    public List<String> searchAll(List<String> terms, int page, int pageSize) throws RemoteException;

    //para debugging, imprime as estatisticas do barrel
    public Map<String, List<String>> getIndexSnapshot() throws RemoteException;
    
    //Synchronization methods
    public void synchronizeFrom(Map<String, List<String>> data, Set<String> visitedUrls, Queue<String> pendingUrls) throws RemoteException;
    public Map<String, Object> getSynchronizationData() throws RemoteException;

}
