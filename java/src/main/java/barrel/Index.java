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

    //para debugging
    public Map<String, List<String>> getIndexSnapshot() throws RemoteException;

}
