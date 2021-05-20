package com.example.spotihy11;
import java.io.*;
import java.net.*;
import java.util.*;

public class Publisher{
    private String publisherId,publisherIp;
    private int publisherPort;
    private HashMap<String,ArrayList<String>> ArtistsForProducer;

    public Publisher(String[][] brokersInfo,HashMap<String,ArrayList<String>> art,String LastLetter,String publisherId,String publisherIp,int publisherPort){
        this.publisherId = publisherId;
        this.publisherIp = publisherIp;
        this.publisherPort = publisherPort;
        CreatePubHashMap(art,LastLetter,publisherId);
        connectToServer(brokersInfo);
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisherIp() {
        return publisherIp;
    }

    public void setPublisherIp(String publisherIp) {
        this.publisherIp = publisherIp;
    }

    public int getPublisherPort() {
        return publisherPort;
    }

    public void setPublisherPort(int publisherPort) {
        this.publisherPort = publisherPort;
    }

    public void CreatePubHashMap(HashMap<String,ArrayList<String>> art, String HalfLetter, String publisherId){
        ArtistsForProducer = new HashMap<String,ArrayList<String>>();
        for(Map.Entry<String,ArrayList<String>> entry : art.entrySet()){
            char tempChar = entry.getKey().toUpperCase().charAt(0);
            char CharLastLetter = HalfLetter.charAt(0);
            if(tempChar <= CharLastLetter && publisherId.equals("1")){ // o publisher 1 pairnei artists sto euros A-L
                ArrayList<String> al = new ArrayList<String>();
                for(int i=0;i<entry.getValue().size();i++) {
                    al.add(entry.getValue().get(i));
                }
                ArtistsForProducer.put(entry.getKey(),al);
            }
            else if(tempChar > CharLastLetter && publisherId.equals("2")){  // o publisher 2 pairnei artists sto euros M-Z
                ArrayList<String> al = new ArrayList<String>();
                for(int i=0;i<entry.getValue().size();i++) {
                    al.add(entry.getValue().get(i));
                }
                ArtistsForProducer.put(entry.getKey(),al);
            }
        }
    }

    public HashMap<String,ArrayList<String>> getArtistsForProducer(){
        return ArtistsForProducer;
    }

    public void connectToServer(String[][] brokersInfo){//sends the artists that are contained in it
        for(int i=0;i<brokersInfo.length;i++) {
            Socket requestSocket = null;
            ObjectOutputStream out = null;
            ObjectInputStream input = null;
            String sendMessage = "";
            String ip = brokersInfo[i][0];
            int port = Integer.parseInt(brokersInfo[i][1]);

            try {
                requestSocket = new Socket(ip, port);
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                input = new ObjectInputStream(requestSocket.getInputStream());
                sendMessage =  "Publisher";
                out.writeObject(sendMessage);
                sendMessage = getPublisherId();
                out.writeObject(sendMessage);
                sendMessage = getPublisherIp();
                out.writeObject(sendMessage);
                sendMessage = Integer.toString(getPublisherPort());
                out.writeObject(sendMessage);
                out.writeObject(getArtistsForProducer().keySet().size());  //stelnei to mege8os tou hashmap twn artist tou
                for(String name : getArtistsForProducer().keySet()){
                    sendMessage =name;
                    out.writeObject(sendMessage);
                    System.out.println(sendMessage);
                }
                sendMessage = "\nPublisher who connected to broker with ip: "+ip+" and port: "+port+" finished.\n";
                System.out.println(sendMessage);
            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    input.close();
                    out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        openPublisherServer();
    }

    public void openPublisherServer(){
        ServerSocket providerSocket = null;
        Socket connection = null;
        try {
            providerSocket = new ServerSocket(publisherPort,10);
            while(true) {
                connection = providerSocket.accept();
                System.out.println("Accepted connection : " + connection);
                Thread afp = new ActionForPublishers(connection,this);
                afp.start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                connection.close();
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    //args[0]: publisher code , 1:A-L , 2: M-Z
    //args[1]: publisher ip
    //args[2]: publisher port
    public static void main(String[] args){
        String HalfLetter="L";
        StoreBrokerInfo sbi = new StoreBrokerInfo();
        sbi.LoadBrokersInfo("BrokersIp.txt");
        StoreFiles sf = new StoreFiles();
        sf.LoadMusicFiles();

        new Publisher(sbi.getBrokersInfo(),sf.getHM(),HalfLetter,args[0],args[1],Integer.parseInt(args[2]));
    }
}