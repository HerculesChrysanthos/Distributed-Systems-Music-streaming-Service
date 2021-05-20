import java.io.*;
import java.math.BigInteger;
import java.net.*;

public class ActionForBrokers extends Thread{
    public ObjectInputStream in;
    public ObjectOutputStream out;
    Broker br;

    public ActionForBrokers(Socket connection,Broker br) {
        this.br = br;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        String sendMessage = "";
        String message = "";
        try{
            System.out.println("eimai o broker "+br.getBrokerId());
            try {
                sendMessage ="broker "+br.getBrokerId()+ " >> kalws hr8es";
                out.writeObject(sendMessage);
                message = (String) in.readObject();
                if(message.equals("Publisher")){
                    System.out.println("sunde8hke publisher ");
                    String[] tempPublisher = new String[3];
                    message = (String)in.readObject();
                    tempPublisher[0] = message; //publisher id
                    message = (String)in.readObject();
                    tempPublisher[1] = message; //publisher ip
                    message = (String)in.readObject();
                    tempPublisher[2] = message; //publisher port
                    br.addRegistedPublishers(tempPublisher);

                    int messageCounter =(int) in.readObject();
                    for(int i=0;i < messageCounter;i++){
                        message = (String) in.readObject();
                        String artistHash = br.getMD5(message);
                        sortArtistsToBrokers(message,artistHash);
                    }
                    System.out.println("\nPublisher with ip: "+tempPublisher[1]+" and port: "+tempPublisher[2]+" shared his artists and finished.\n");
                }
                else if(message.equals("Consumer")){
                    String searchingArtist = "";
                    String searchingSong = "";
                    boolean flag = true;
                    boolean searchForOtherArt = false;
                    message = (String) in.readObject();
                    if(!br.getRegistedUsers().contains(message)) {
                        br.addRegistedUsers(message);
                    }
                    System.out.println("consumer with username: "+message+" connected.");
                    while(flag || searchForOtherArt) {
                        flag = false;
                        searchingArtist = (String)in.readObject();
                        if(searchingArtist.equals("") || searchForOtherArt) {
                            sendMessage = "Give artist: ";
                            out.writeObject(sendMessage);
                            searchingArtist = (String) in.readObject();
                        }else{
                            sendMessage = "The artist that you are searching for is: "+searchingArtist;
                            out.writeObject(sendMessage);
                        }
                        searchForOtherArt = false;
                        boolean CorrectBroker = false;
                        for (int i = 0; i < br.getArtistArrayList().size(); i++) {
                            if (br.getArtistArrayList().get(i).getArtistName().equalsIgnoreCase(searchingArtist.toLowerCase())) {
                                CorrectBroker = true;
                                out.writeObject(CorrectBroker); //stelnei sima oti tou anhkei o artist
                                sendMessage = searchingArtist + " found, give song title: ";
                                out.writeObject(sendMessage);
                                searchingSong = (String) in.readObject();
                                sendMessage = "Please wait...";
                                out.writeObject(sendMessage);
                                char tempArt = searchingArtist.toUpperCase().charAt(0);
                                if (tempArt <= 'L') {
                                    for (int PublisherIndex = 0; PublisherIndex < br.getRegistedPublishers().size(); PublisherIndex++) {
                                        if (br.getRegistedPublishers().get(PublisherIndex)[0].equals("1")) {
                                            br.pull(PublisherIndex, br.getArtistArrayList().get(i), searchingSong,out,in);
                                        }
                                    }
                                } else {
                                    for (int PublisherIndex = 0; PublisherIndex < br.getRegistedPublishers().size(); PublisherIndex++) {
                                        if (br.getRegistedPublishers().get(PublisherIndex)[0].equals("2")) {
                                            br.pull(PublisherIndex, br.getArtistArrayList().get(i), searchingSong,out,in);
                                        }
                                    }
                                }
                            }
                        }
                        if (!CorrectBroker) {
                            System.out.println("la8os broker");
                            out.writeObject(CorrectBroker); //stelnei sima oti den tou anhkei o artist
                            if(((int)in.readObject()) == 2){
                                out.writeObject(true);
                                out.writeObject("The artist can't be found in our dataset.\nDo you want to search for another?\nType 1 for yes and 0 for no: ");
                                message = (String)in.readObject();
                                if(message.equals("1")){
                                    searchForOtherArt = true;
                                    out.writeObject(searchForOtherArt);
                                }
                                else{
                                    sendMessage = "goodbye";
                                    out.writeObject(sendMessage);
                                }
                            }
                            else {
                                out.writeObject(false);
                                sendMessage = searchingArtist + " can't be found at this broker, wait to connect you with the appropriate broker...";
                                out.writeObject(sendMessage);
                                findBroker(searchingArtist);
                            }
                        }
                        else{
                            sendMessage = "do you want to search for another artist and song?\nType 1 for yes and 0 for no: ";
                            out.writeObject(sendMessage);
                            message = (String)in.readObject();
                            if(message.equals("1")){
                                searchForOtherArt = true;
                            }
                            else{
                                sendMessage = "okay goodbye.";
                                out.writeObject(sendMessage);
                            }
                        }
                    }

                    System.out.println("The connected consumers at broker with id "+br.getBrokerId()+ " are: ");
                    for (int ConsCount = 0; ConsCount < br.getRegistedUsers().size(); ConsCount++) {
                        System.out.println(br.getRegistedUsers().get(ConsCount));
                    }
                }
             } catch (ClassNotFoundException e) {
                e.printStackTrace();
             }
        }catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void sortArtistsToBrokers(String artistName,String artistHash){
        BigInteger artistHashBi = new BigInteger(artistHash);
        int BrokerIndex;
        for(BrokerIndex=0;BrokerIndex<br.getBrokers().length;BrokerIndex++){  //vriskw poia thesi ston pinaka twn broker exei o trexwn broker
            if(br.getBrokers()[BrokerIndex][3].equals(br.getBrokerId()))
                break;
        }
        if((new BigInteger(br.getBrokers()[BrokerIndex][2]).compareTo(artistHashBi)) >= 0){ //artistHash <hash trexwntos broker
            if(BrokerIndex == 0){
                ArtistName art = new ArtistName(artistName,artistHash);
                br.addArtistsForBroker(art);
            }
            else if((new BigInteger(br.getBrokers()[BrokerIndex - 1][2]).compareTo(artistHashBi)) <= 0){ //hash broker[i-1] < artist hash
                ArtistName art = new ArtistName(artistName,artistHash);
                br.addArtistsForBroker(art);
            }
        }
        else {  //artistHash > has trexwntos broker
            if(BrokerIndex == 0 &&  (new BigInteger(br.getBrokers()[br.getBrokers().length - 1][2]).compareTo(artistHashBi)) < 0){ //ama einai ston teleutaio broker
                ArtistName art = new ArtistName(artistName,artistHash);
                br.addArtistsForBroker(art);
            }
        }
    }

    public void findBroker(String artistName){//search of a song that was not found in first broker
        String artistHash = br.getMD5(artistName);
        String ip = "";
        int port = 0 ;
        BigInteger artistHashBi = new BigInteger(artistHash);
        try {
            for(int i = 0; i < br.getBrokers().length; i++) {
                if ((new BigInteger(br.getBrokers()[i][2]).compareTo(artistHashBi)) > 0) {
                    ip = br.getBrokers()[i][0];
                    out.writeObject(ip);
                    port = Integer.parseInt(br.getBrokers()[i][1]);
                    out.writeObject(port);
                    break;
                }
                else if(((new BigInteger(br.getBrokers()[i][2]).compareTo(artistHashBi)) <= 0) && (i == (br.getBrokers().length - 1))){
                    ip = br.getBrokers()[0][0];
                    out.writeObject(ip);
                    port = Integer.parseInt(br.getBrokers()[0][1]);
                    out.writeObject(port);
                    break;
                }
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}