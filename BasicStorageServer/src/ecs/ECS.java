/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package ecs;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import app_kvClient.*;
import java.math.BigInteger;
import java.security.MessageDigest;
/**
 *
 * @author alexander-frey
 */
import java.security.NoSuchAlgorithmException;import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author alexander-frey
 */
public class ECS {

    /**
     * @param args the command line arguments
     */
    
    private ArrayList <String[]> servers;
     private ArrayList <String[]> active_servers;
     private ArrayList <Process> processes;
    
    public ECS () {
        this.servers = new ArrayList();
        this.active_servers = new ArrayList();
       this.processes = new ArrayList();
    }
    public static void main(String[] args) {
        ECS service = new ECS();
        service.initService(4);
       
       
        
        // TODO code application logic here
    }
    
    public void initService (int numberOfNodes) {
        this.servers = getConfig();
        this.active_servers = getActiveServers(numberOfNodes);
        
         for (int i=0; i< this.servers.size(); i++) {
            //System.out.print(Arrays.toString(servers.get(i)));
        }
        
         displayActiveServers();
         
         addNode();
        
        
    } 
    
    public void initProcess (String ip, String port) {
          ProcessBuilder pb = new ProcessBuilder ("nohup", "ssh", "-n", ip, "java -jar", "ms3-server.jar", port, "%");
         pb.redirectErrorStream();
         
         Process proc;
   
         try {
             proc = pb.start();
             processes.add(proc);
         } catch (IOException e) {
             e.printStackTrace();
         }
    }
    
    public void displayActiveServers () {
        for (int i=0; i< this.active_servers.size(); i++) {
            System.out.println(Arrays.toString(active_servers.get(i)));
             initProcess(active_servers.get(i)[1],active_servers.get(i)[2]);
        }
    }
    
    public ArrayList getActiveServers (int numberOfNodes) {
        ArrayList <String[]> active = new ArrayList();
        int[] randomNumbers = getRandomNumbers(numberOfNodes);
        
        for (int i=0; i< randomNumbers.length;i++) {
            active.add(this.servers.get(randomNumbers[i]));
        }
        
        return active;
    }
    
    public int[] getRandomNumbers (int numbers) {
        int []randomNumbers = new int[numbers];

      for (int i=0; i<randomNumbers.length;i++){
          randomNumbers[i] = -1;
          
      }
        Random random = new Random();
        for (int i=0; i<randomNumbers.length;i++){
             while (true) {
                 int randomnumber =random.nextInt(this.servers.size());
                 
                 if (!isInArray(randomnumber,randomNumbers)) {
                     randomNumbers[i] = randomnumber;
                     break;
                } 
            }
        } 

        return randomNumbers;
    }
    
    public boolean isInArray (int number, int[] array) {
        
        for (int i=0; i<array.length;i++) {
           
            if (number == array[i]) return true;
        }
        
        return false;
    }
    
    
    public ArrayList getConfig () {
       ArrayList <String[]> serverliste = new ArrayList();
      
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/ecs/ecs.config")));
            String zeile = "";
           
           
            while( (zeile = reader.readLine()) != null){
                String[]server = zeile.split(" ");
                serverliste.add(server);
                
            }
           
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return serverliste;
    }
    
    public void start() throws IOException {
        
        sendMessage("Start");
    }
    public void stop() {
        sendMessage("Stop");
    }
     public void shutDown() {
        sendMessage("ShutDown");
        
      for (int i=0; i<processes.size();i++) {
          processes.get(i).destroy();
      }
    }
    
    public void sendMessage (String message) {
        TextMessage msg = new TextMessage (message);
        for (int i=0; i<processes.size();i++) {
            try {
            OutputStream output = processes.get(i).getOutputStream();
            output.write(msg.getMsgBytes(), 0, msg.getMsgBytes().length);
            output.close();
            }
            catch (IOException ioe){
                
            }
            
        }
    }
   
    
    public void addNode () {
        while (true) {
             int[] randomNumber = getRandomNumbers(1);
             if(!isActive(servers.get(randomNumber[0]))) {
                 active_servers.add(servers.get(randomNumber[0]));
                 break;
             }
        }
        
        active_servers.
       displayActiveServers();
        
    }
    
    public String generateHash(String ip, String port) throws NoSuchAlgorithmException{
        String s = ip+":"+port;
        
     MessageDigest m=MessageDigest.getInstance("MD5");
      m.update(s.getBytes(),0,s.length());
    
  return new BigInteger(1,m.digest()).toString(16);
    }
    
    public boolean isActive (String [] server) {
        
        for (int i=0; i<active_servers.size();i++) {
            try {
                if (generateHash(server[1], server[2]).equals(generateHash(active_servers.get(i)[1],active_servers.get(i)[2]))){
                     return true;
                }
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(ECS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return false;
    }
    
    public void removeNode() {
        
    }
}
