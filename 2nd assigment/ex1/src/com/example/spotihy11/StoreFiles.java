package com.example.spotihy11;
import java.io.*;
import java.util.*;
import com.mpatric.mp3agic.*;
public class StoreFiles {
    private HashMap<String,ArrayList<String>> art;

    public void LoadMusicFiles(){
        try {
            art = new HashMap<String,ArrayList<String>>(); //key: artistname value: list with songs
            File f = new File("dataset1");
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File f, String name) {
                    return !name.endsWith("pdf") && !name.startsWith(".");
                }
            };
            File[] files = f.listFiles(filter);
            for (int i = 0; i <files.length; i++) {
                File f_in = new File("dataset1"+"/"+files[i].getName());
                File[] files_new = f_in.listFiles();
                for(int j=0;j<files_new.length;j++){
                    try{
                        if(!(files_new[j].getName().startsWith("._")) ){
                            Mp3File mp3file = new Mp3File(files_new[j]);
                            if(files_new[j].getName().endsWith("mp3")){
                                if(mp3file.hasId3v2Tag() ){
                                    ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                                    if(id3v2Tag.getArtist()!=null && !(id3v2Tag.getArtist().equals(""))) {
                                        if (art.containsKey(id3v2Tag.getArtist().toLowerCase())) {
                                            art.get(id3v2Tag.getArtist().toLowerCase()).add("dataset1" + "/" + files[i].getName() + "/" + files_new[j].getName() + "/" + id3v2Tag.getAlbum());
                                        } else if (!art.containsKey(id3v2Tag.getArtist().toLowerCase())) {
                                            ArrayList<String> al = new ArrayList<String>();
                                            art.put(id3v2Tag.getArtist().toLowerCase(), al);
                                            art.get(id3v2Tag.getArtist().toLowerCase()).add("dataset1" + "/" + files[i].getName() + "/" + files_new[j].getName() + "/" + id3v2Tag.getAlbum());
                                        }
                                    }
                                    else{
                                        if(art.containsKey("Unknown")){
                                            art.get("Unknown").add("dataset1"+"/"+files[i].getName()+"/"+files_new[j].getName()+"/"+id3v2Tag.getAlbum());
                                        }
                                        else{
                                            ArrayList<String> al=new ArrayList<String>();
                                            art.put("Unknown",al);
                                            art.get("Unknown").add("dataset1"+"/"+files[i].getName()+"/"+files_new[j].getName()+"/"+id3v2Tag.getAlbum());
                                        }
                                    }
                                }
                            }
                        }
                    }catch (UnsupportedTagException | InvalidDataException | IOException e) {
                        System.err.println(files_new[j].getName()+"is not an mp3 with id3v2 coding");
                    }
                }
            }
        }catch(NullPointerException e){
            System.err.println("The file was not found.");
        }
    }

    public HashMap<String,ArrayList<String>> getHM(){
        return art;
    }
}