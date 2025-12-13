package barrel;


import java.rmi.*;
import java.util.List;

public interface Manager extends Remote {
    List<String> getActiveBarrels() throws RemoteException;

    public interface WorkAvailableListener extends Remote {
        void onNewWorkAvailable() throws RemoteException;
    }
    
    void registerWorkListener(WorkAvailableListener listener) throws RemoteException;
    
    void unregisterWorkListener(WorkAvailableListener listener) throws RemoteException;
    
    void notifyNewWorkAvailable() throws RemoteException;
}
