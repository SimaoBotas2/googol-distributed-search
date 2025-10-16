package search;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.*;

public class IndexServer extends UnicastRemoteObject implements Index {
    private ArrayList<String> urlsToIndex;
    private HashMap<String,List<String>> indexedItems;

    public IndexServer() throws RemoteException {
        super();
        //This structure has a number of problems. The first is that it is fixed size. Can you enumerate the others?
        urlsToIndex = new ArrayList<String>();       
    }

    public static void main(String args[]) {
        try {
            IndexServer server = new IndexServer();
            Registry registry = LocateRegistry.createRegistry(8183);
            registry.rebind("index", server);
            System.out.println("Server ready. Waiting for input...");
            //TODO: This approach needs to become interactive. Use a Scanner(System.in) to create a rudimentary user interface to:
            //1. Add urls for indexing
            //2. search indexed urls
            server.putNew("https://pt.wikipedia.org/wiki/Wikip%C3%A9dia:P%C3%A1gina_principal");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private long counter = 0, timestamp = System.currentTimeMillis();;

    public String takeNext() throws RemoteException {
        // prefer queue-like behaviour: remove and return head
        synchronized (urlsToIndex) {
            if (urlsToIndex.isEmpty()) {
                return null;
            }
            return urlsToIndex.remove(0);
        }
    }

    public void putNew(String url) throws java.rmi.RemoteException {
        synchronized (urlsToIndex) {
            urlsToIndex.add(url);
        }
    }

    public void addToIndex(String word, String url) throws java.rmi.RemoteException {
        //TODO: not implemented
    }

    
    public List<String> searchWord(String word) throws java.rmi.RemoteException {
        //TODO: not implemented
        return new ArrayList<String>();
    }
}
