package barrel;


import java.rmi.*;
import java.util.List;

public interface Manager extends Remote {
    List <String> getActiveBarrels() throws RemoteException;
}
