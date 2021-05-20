package com.example.spotihy11;

import android.app.Application;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public final class MyApplication extends Application {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ArrayList<String> totalArtistList;
    public ArrayList<String> songs;

    public Socket getSocket(){
        return socket;
    }

    public void setSocket(Socket socket){
        this.socket = socket;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }

    public ArrayList<String> getTotalArtistList() {
        return totalArtistList;
    }

    public void addTotalArtistList(String artist) {
        totalArtistList.add(artist);
    }

    public void setTotalArtistList(ArrayList<String> totalArtistList) {
        this.totalArtistList = totalArtistList;
    }

    public ArrayList<String> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<String> songs) {
        this.songs = songs;
    }

    @Override
    public void onCreate() {
         super.onCreate();
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute("192.168.1.68", "4321","true","false");

    }

    public void Redirect(String ip,int port,boolean check,String username,String artistName){
        songs = new ArrayList<>();
        AsyncTaskRunner redirect = new AsyncTaskRunner();
        redirect.execute(ip,String.valueOf(port),String.valueOf(check),"false",username,artistName);

    }

    public void CloseSocket(){
        AsyncTaskRunner bye = new AsyncTaskRunner();
        bye.execute("","","","true","","");
    }
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            if( Boolean.parseBoolean(strings[3])){
                try{
                    out.writeObject(false);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        in.close();
                        out.close();
                        socket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
            else {

                boolean check = Boolean.parseBoolean(strings[2]);
                if (!check) {
                    try {
                        socket.close(); //an allksei server kleinei to socket kai ta streams tou prohgoumenou
                        in.close();
                        out.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                try {
                    socket = new Socket(strings[0], Integer.parseInt(strings[1]));
                    in = new ObjectInputStream(socket.getInputStream());
                    out = new ObjectOutputStream(socket.getOutputStream());
                    getOut().writeObject("Consumer");
                    getOut().writeObject(check);
                    if (check) {
                        totalArtistList = new ArrayList<String>();
                        int artistsSize = (int) in.readObject();
                        for (int i = 0; i < artistsSize; i++) {
                            String message = (String) in.readObject();
                            totalArtistList.add(message);
                        }
                    } else {
                        getOut().writeObject(strings[4]); //stelnei username an kanei redirect
                        getOut().flush();
                        getOut().writeObject(strings[5]);//stelnei artistname an kanei redirect
                        getOut().flush();
                        boolean findCorrectBroker = (boolean) in.readObject();
                        if (findCorrectBroker) {
                            int songsAmount = (int) in.readObject();
                            for (int i = 0; i < songsAmount; i++) {
                                String tempString = (String) in.readObject();
                                songs.add(tempString);
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                }
            }
            return null;
        }
    }

}
