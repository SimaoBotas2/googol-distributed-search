package gateway;

import java.rmi.*;
import java.util.*;

public interface GatewayInterface extends Remote{
    void addUrl(String url) throws RemoteException;
    List<String> search(String word) throws RemoteException;
    List<String> getPagesOrderedByInLinks(int limit, int offset) throws RemoteException;
    Set<String> getPagesLinkingTo(String url) throws RemoteException;
}
