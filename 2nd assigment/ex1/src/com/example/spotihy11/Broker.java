package com.example.spotihy11;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.*;
import java.net.*;
public class Broker{
	private ArrayList<String> registedUsers;
	private ArrayList<String[]> registedPublishers;
	private ArrayList<ArtistName> artistArrayList;
	private ArrayList<String> totalArtistArrayList;
	private int port;
	private String ip,brokerId,brokerHash;
	private String[][] brokers;
	boolean songFound;

	public Broker(String[][] brokersInfo,String ip,int port){
		this.ip = ip;
		this.port = port;
		artistArrayList = new ArrayList<ArtistName>();
		registedUsers = new ArrayList<String>();
		registedPublishers = new ArrayList<String[]>();
		totalArtistArrayList = new ArrayList<String>();
		CreateBrokersArr(brokersInfo,ip,port);
		System.out.println("Broker ip: "+ip+" Broker port: "+port);
		openServer(this.ip,this.port);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getBrokerId() {
		return brokerId;
	}

	public String getBrokerHash() {
		return brokerHash;
	}

	public void setBrokerHash(String brokerHash) {
		this.brokerHash = brokerHash;
	}

	public void setBrokerId(String brokerId) {
		this.brokerId = brokerId;
	}

	public void setBrokers(String[][] brokers) {
		this.brokers = brokers;
	}

	public String[][] getBrokers(){
		return brokers;
	}

	public ArrayList<ArtistName> getArtistArrayList() {
		return artistArrayList;
	}

	public void addArtistsForBroker(ArtistName art) {
		artistArrayList.add(art);
	}

	public ArrayList<String> getRegistedUsers() {
		return registedUsers;
	}

	public void addRegistedUsers(String User) {
		registedUsers.add(User);
	}

	public ArrayList<String[]> getRegistedPublishers() {
		return registedPublishers;
	}

	public void addRegistedPublishers(String[] Pub) {
		registedPublishers.add(Pub);
	}

	public ArrayList<String> getTotalArtistArrayList() {
		return totalArtistArrayList;
	}

	public void addTotalArtistArrayList(String artist) {
		totalArtistArrayList.add(artist);
	}

	public String getMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger num = new BigInteger(1, messageDigest);//to kanei thetiko to 1
			return num.toString();
		} catch (NoSuchAlgorithmException e) {
			return "I'm sorry, but MD5 is not a valid message digest algorithm";
		}
	}

	public void CreateBrokersArr(String[][] brokersInfo, String ip, int port){
		brokers = new String[brokersInfo.length][4];
		for(int i=0;i<brokersInfo.length;i++){
			brokers[i][0] = brokersInfo[i][0];  //ip
			brokers[i][1] = brokersInfo[i][1];  //port
			brokers[i][2] = getMD5((brokersInfo[i][0]+brokersInfo[i][1]));  //hash
			brokers[i][3] = Integer.toString(i + 1); //broker id
			if(brokers[i][0].equals(ip)  && Integer.parseInt(brokers[i][1]) == port) {
				brokerHash = brokers[i][2];
				brokerId = brokers[i][3];
			}
		}
		sortBrokers(brokers);
	}

	public void sortBrokers(String[][] brokers){ //sort brokers according their hash
		boolean flag = true;
		int j = 0;
		BigInteger b1;
		BigInteger b2;
		BigInteger temp;
		String tempIp = "",tempPort = "",tempBrokerId = "" ;
		while(flag){
			flag = false;
			j++;
			for(int i = 0;i < brokers.length - j;i++){
				b1 = new BigInteger(brokers[i][2]);
				b2 = new BigInteger(brokers[i + 1][2]);
				if(b1.compareTo(b2) > 0){  // b1>b2
					tempIp = brokers[i][0];
					tempPort = brokers[i][1];
					temp = b1;
					tempBrokerId = brokers[i][3];

					brokers[i][0] = brokers[i + 1][0];
					brokers[i][1] = brokers[i + 1][1];
					brokers[i][2] = brokers[i + 1][2];
					brokers[i][3] = brokers[i + 1][3];

					brokers[i + 1][0] = tempIp;
					brokers[i + 1][1] = tempPort;
					brokers[i + 1][2] = temp.toString();
					brokers[i + 1][3] = tempBrokerId;
					flag = true;
				}
			}
		}
	}

	public void openServer(String ip,int port){
		ServerSocket providerSocket = null;
		Socket connection = null;
		try {
			providerSocket = new ServerSocket(port,10);
			while(true) {
				connection = providerSocket.accept();
				System.out.println("Accepted connection : " + connection);
				Thread afb = new ActionForBrokers(connection,this);
				afb.start();
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

	public void  pull(int PublisherIndex,ArtistName art,ObjectOutputStream serverOut,ObjectInputStream serverIn){
		Socket requestSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream input = null;
		String message = "";
		songFound = false;
		String pubId = getRegistedPublishers().get(PublisherIndex)[0];
		String pubIp = getRegistedPublishers().get(PublisherIndex)[1];
		int pubPort = Integer.parseInt(getRegistedPublishers().get(PublisherIndex)[2]);
		try {
			requestSocket = new Socket(pubIp, pubPort);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			input = new ObjectInputStream(requestSocket.getInputStream());
			System.out.println("connected to publisher with Id: "+pubId);
			try {
				message = (String) input.readObject();  //kalws hr8es
				System.out.println(message);
				out.writeObject(art); //stelnei ton kallitexni pou thelei na tou vrei o upeuthinos publisher

				int songsAmount = (int)input.readObject(); //#apo tragoudia tou artist pou zhththike
				serverOut.writeObject(songsAmount);
				for(int i=0;i<songsAmount;i++){
					String tempSong = (String)input.readObject();
					serverOut.writeObject(tempSong); //stelnei ti lista me ta tragoudia tou artist pou epelekse o user

				}

				message = (String)serverIn.readObject(); //dexetai song name
				out.writeObject(message); //stelnei ston publisher song name

				songFound = (boolean)input.readObject();
				serverOut.writeObject(songFound);
				if(songFound) {
					String genre = (String) input.readObject();
					serverOut.writeObject(genre);
					String album = (String) input.readObject();
					serverOut.writeObject(album);

					int filesAmount = (int) input.readObject(); //# of files
					serverOut.writeObject(filesAmount);
					int sizeOfFiles = (int) input.readObject(); //pieces size
					serverOut.writeObject(sizeOfFiles);
					int lastpiece = (int) input.readObject(); //last piece size
					serverOut.writeObject(lastpiece);
					MusicFile mf = null;
					for (int i = 0; i < filesAmount; i++) {
						mf = (MusicFile) input.readObject();
						serverOut.writeObject(mf);
						System.out.println(mf.getTrackName());
					}

				}
			}catch (ClassNotFoundException classnot) {
				System.err.println("Data received in unknown format");
			}

		}catch(UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		}catch (IOException ioException) {
			ioException.printStackTrace();
		}finally {
			try {
				input.close();
				out.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

	}

	//args[0]: ip
	// args[1] : port
	public static void main(String[] args){
		StoreBrokerInfo sbi = new StoreBrokerInfo();
		sbi.LoadBrokersInfo("BrokersIp.txt");
		new Broker(sbi.getBrokersInfo(),args[0],Integer.parseInt(args[1])) ;
	}
}