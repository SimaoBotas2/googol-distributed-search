package barrel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class IndexManager{

    public static void main(String[] args) {
        String configFile = "config.txt";
        List<Integer> ports = new ArrayList<>();

        //Ler as ports do config file
        try(BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
             String line;
             while((line = reader.readLine()) != null){
                line = line.trim(); // trim tira os espaços em branco no fim e no início
                if(line.isEmpty() || line.startsWith("#")) continue;
                ports.add(Integer.parseInt(line.split("\\s+")[0])); //pega no primeiro elemento do split, o \\s+ é os espaços<
             }
        
        }
        catch (IOException e){
            System.err.println("[IndexManager] Erro ao ler o ficheiro de config "+ configFile + " " + e.getMessage());
            return;
        }

        int numberPorts = ports.size();

        for(int p : ports){
            try{
                IndexBarrel barrel = new IndexBarrel();
                Registry registry = LocateRegistry.createRegistry(p);
                registry.rebind("index", barrel);
                System.out.println("[IndexManager] Barrel criado na porta " + p); 
            }
            catch(ExportException ex){
                System.err.println("[IndexManager] Já existe um barrel na porta" + p);
            }

            catch(RemoteException ex){
                System.err.println("[IndexManager] Erro ao criar barrel na porta" + p);
            }
        }
        System.out.println("[IndexManager] Todos os barrels foram lançados! ");
        


    }

}


