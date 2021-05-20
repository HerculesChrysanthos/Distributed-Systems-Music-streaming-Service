import java.io.*;
import java.net.*;
import java.util.*;

public class Consumer{
	int brokerCounter;

	public Consumer(String ip,int port,String username){
		brokerCounter = 0;
		connectToServer(ip,port,username,"");
	}

	public void connectToServer(String ip,int port,String username,String searchingArtist){
		Scanner scanner = new Scanner(System.in);
		Socket requestSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream input = null;

		String sendMessage = "";
		String message = "";
		boolean findCorrectBroker = true;
		boolean keepSearching = true;
		boolean songFound = false;
		String newIp = "";
		int newPort = 0;
		try {
			requestSocket = new Socket(ip, port);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			input = new ObjectInputStream(requestSocket.getInputStream());
			sendMessage = "Consumer";
			String answer = "";
			boolean flag = true;
			boolean searchForOtherArt = false;
			try {
				System.out.println((String) input.readObject());
				out.writeObject(sendMessage);
				out.writeObject(username);
				while(flag || searchForOtherArt) {
					flag = false;
					out.writeObject(searchingArtist);
					message = (String) input.readObject(); //Give artist: // The artist that you are searching for is: "+searchingArtist
					System.out.println(message);
					if(searchingArtist.equals("") || searchForOtherArt) {
						searchingArtist = scanner.nextLine();
						out.writeObject(searchingArtist);
					}
					searchForOtherArt = false;
					findCorrectBroker = (boolean) input.readObject();
					if (findCorrectBroker) {
						message = (String) input.readObject(); // give song title
						System.out.print(message);
						answer = scanner.nextLine();
						out.writeObject(answer);

						message = (String) input.readObject(); // please wait
						System.out.println(message);
						songFound = (boolean)input.readObject();
						while(!songFound){
							message = (String)input.readObject(); //"The name of song that you entered doesn't exist, please give the correct title: "
							System.out.println(message);
							answer = scanner.nextLine();
							out.writeObject(answer);
							songFound = (boolean)input.readObject();
						}
						download(input);
						message = (String)input.readObject(); // do you want to search for another artist and song?\nType 1 for yes and 0 for no:
						System.out.println(message);
						answer = scanner.nextLine();
						out.writeObject(answer);
						if(answer.equals("1")){
							flag = true;
							brokerCounter = 1;
						}
						else{
							message = (String)input.readObject();
							System.out.println(message);//okay goodbye.
						}
					}
					else {
						brokerCounter++;
						out.writeObject(brokerCounter);
						boolean test = (boolean)input.readObject();
						if(test){
							message = (String)input.readObject(); //"The artist can't be found in our dataset.\nDo you want to search for another?\nType 1 for yes and 0 for no: "
							System.out.println(message);
							answer = scanner.nextLine();
							out.writeObject(answer);
							if(answer.equals("1")){
								searchForOtherArt = (boolean)input.readObject();
								searchingArtist = "";
								brokerCounter = 1;
							}
							else{
								message = (String)input.readObject();
								System.out.println(message);
								keepSearching = false;
							}
						}
						else {
							message = (String) input.readObject();  //can't be found at this broker, wait to connect you with the appropriate broker...
							System.out.println(message);
							newIp = (String) input.readObject();
							newPort = (int) input.readObject();
						}
					}
				}
			} catch (ClassNotFoundException classnot) {
				System.err.println("Data received in unknown format");
			}
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				input.close();
				out.close();
				requestSocket.close();
				if(!findCorrectBroker && keepSearching){
					connectToServer(newIp, newPort,username,searchingArtist);
				}
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	public void download(ObjectInputStream input){
		FileOutputStream fos = null;
		try {
			int filesAmount = (int) input.readObject(); // #
			int sizeOfFiles = (int) input.readObject(); //piecessize
			int lastpiece = (int) input.readObject(); //last piece size
			MusicFile mf = null;
			ArrayList<MusicFile> mflist = new ArrayList<MusicFile>();
			for (int i = 0; i < filesAmount; i++) {
				mf = (MusicFile) input.readObject();
				mflist.add(mf);  //apothikeush twn chunks
				System.out.println("downloading  " + mf.getTrackName());
				if (i == filesAmount - 1) {
					File newFile = new File(mf.getTrackName());
					fos = new FileOutputStream(newFile);
					fos.write(mf.getMusicFileExtract(), 0, lastpiece);
				} else {
					File newFile = new File(mf.getTrackName());
					fos = new FileOutputStream(newFile);
					fos.write(mf.getMusicFileExtract(), 0, sizeOfFiles);
				}
			}
		String name = mf.getTrackName().substring(4,mf.getTrackName().length());
		File newFile = new File(name);
		System.out.println("album: "+mf.getAlbumInfo()+ " genre: "+mf.getGenre());
		try { //dhmiourgia enos .mp3 arxeiou apo eiserxomena chunks
			fos = new FileOutputStream(newFile);
			for (int i = 0; i < mflist.size(); i++) {
				fos.write(mflist.get(i).getMusicFileExtract(), 0, mflist.get(i).getMusicFileExtract().length);
			}
			fos.close();
		}catch (IOException ioException) {
			ioException.printStackTrace();
		}
		}catch(ClassNotFoundException | IOException classnot) {
			System.err.println("Data received in unknown format");
		}
	}

//args[0]: ip ,
//args[1]: port,
//args[2]: username
	public static void main(String[] args){
		new Consumer(args[0],Integer.parseInt(args[1]),args[2]);
	}
}