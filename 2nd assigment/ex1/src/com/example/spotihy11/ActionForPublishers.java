package com.example.spotihy11;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ActionForPublishers extends Thread{
    public ObjectInputStream input;
    public ObjectOutputStream out;
    Publisher pub;
    Socket s;

    public ActionForPublishers(Socket connection, Publisher pub){
        this.s = connection;
        this.pub = pub;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        String sendMessage = "";
        String message = "";
        String pathSong ="";
        String tempSong ="";
        String tempGenre ="";
        int startCategoryIndex = 0;
        int startTitleIndex = 0;
        String tempAlbum = "";
        boolean songFound = false;
        try{
            System.out.println("eimai o publisher "+pub.getPublisherId());
            try{
                sendMessage ="publisher server "+pub.getPublisherId()+ " >> kalws hr8es";
                out.writeObject(sendMessage);
                ArtistName art = (ArtistName) input.readObject();  //lambanw artist
                System.out.println("zhtas :"+art.getArtistName());
                ArrayList<String> songsList = new ArrayList<>();
                for (int i = 0; i < pub.getArtistsForProducer().get(art.getArtistName()).size(); i++) {
                    pathSong = pub.getArtistsForProducer().get(art.getArtistName()).get(i);
                    int count = 0;
                    for (int j = 0; j < pathSong.length(); j++) {
                        String temp = Character.toString(pathSong.charAt(j));
                        if (temp.equals("/")) {
                            count++;
                            if (count == 1) {
                                startCategoryIndex = j + 1;
                            } else if (count == 2) {
                                startTitleIndex = j + 1;
                            } else if (count == 3) {
                                tempSong = pathSong.substring(startTitleIndex, j - 4);
                            }
                        }
                    }
                    songsList.add(tempSong);
                }
                out.writeObject(songsList.size());
                for(int i=0;i<songsList.size();i++){
                    out.writeObject(songsList.get(i)); //stelnei ti lista me ta tragoudia tou artist pou exei epilex8ei

                }
                while(!songFound) {
                    message = (String) input.readObject();  //lamvanw song
                    for (int i = 0; i < pub.getArtistsForProducer().get(art.getArtistName()).size(); i++) {
                        pathSong = pub.getArtistsForProducer().get(art.getArtistName()).get(i);
                        int count = 0;
                        for (int j = 0; j < pathSong.length(); j++) {
                            String temp = Character.toString(pathSong.charAt(j));
                            if (temp.equals("/")) {
                                count++;
                                if (count == 1) {
                                    startCategoryIndex = j + 1;
                                } else if (count == 2) {
                                    startTitleIndex = j + 1;
                                } else if (count == 3) {
                                    tempSong = pathSong.substring(startTitleIndex, j - 4);
                                    tempGenre = pathSong.substring(startCategoryIndex, startTitleIndex - 1);
                                    tempAlbum = pathSong.substring(j + 1, pathSong.length());
                                    pathSong = pathSong.substring(0, j);
                                    break;
                                }
                            }
                        }
                        if (tempSong.equalsIgnoreCase(message)) {
                            songFound = true;
                            break;
                        }
                    }
                    if (songFound) {
                        out.writeObject(songFound);
                        out.writeObject(tempGenre);
                        out.writeObject(tempAlbum);
                        push(pathSong, tempSong, art, tempAlbum, tempGenre);
                    } else {
                        System.out.println("can't find this song");
                    }
               }

            }catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }catch (IOException e) {
            e.printStackTrace();

        }finally{
            try {
                input.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void push(String pathSong,String title,ArtistName art,String Album,String Genre){
        FileInputStream fis = null;
        int partCounter = 1;
        int sizeOfFiles = 1024 * 512;// 512KB
        byte[] buffer = new byte[sizeOfFiles];
        File myFile = null;
        try {
            myFile = new File(pathSong);
            fis = new FileInputStream(myFile);
            int chunksAmount = (int)myFile.length()/sizeOfFiles;
            int lastpiece = (int)myFile.length()%sizeOfFiles;
            String fileName = myFile.getName();
            int totatlChunks = chunksAmount + 1 ;
            out.writeObject(totatlChunks);
            out.writeObject(sizeOfFiles);
            out.writeObject(lastpiece);
            MusicFile mf = null;
            byte[] temp = null;
            for(int i=0;i<totatlChunks;i++){
                String filePartName = String.format("%03d_%s", partCounter++, fileName);
                if(i==totatlChunks-1){
                    fis.read(buffer,0,lastpiece);
                    temp = Arrays.copyOf(buffer , lastpiece);
                }
                else{
                    fis.read(buffer,0,sizeOfFiles);
                     temp = Arrays.copyOf(buffer , sizeOfFiles);
                }
                mf = new MusicFile(filePartName,art.getArtistName(),Album,Genre,temp);
                out.writeObject(mf);
            }
        }catch(IOException e){
            System.out.println("Could not open file.");
        }
    }
}