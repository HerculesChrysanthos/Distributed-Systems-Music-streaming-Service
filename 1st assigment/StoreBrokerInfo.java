import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StoreBrokerInfo {
    private  String[][] brokersInfo;

    public  void LoadBrokersInfo(String a){
        String line;
        File f= null;
        try{
            f = new File(a);
        }
        catch(NullPointerException e){
            System.err.println("The file was not found.");
        }
        try {

            BufferedReader reader = new BufferedReader(new FileReader(f));
            Path path = Paths.get("./BrokersIp.txt");
            long lineCount = Files.lines(path).count();

            brokersInfo  = new String[(int)lineCount][2];
            line = reader.readLine();
            int count =0;
            while(line!=null){
                String[] temp =  line.split(" ");
                brokersInfo[count][0] = temp[0]; //ip
                brokersInfo[count][1] = temp[1]; //port
                count++;
                line = reader.readLine();
            }
        }catch(IOException e){
            System.out.println("Could not open file.");
        }
    }

    public String[][] getBrokersInfo(){
        return this.brokersInfo;
    }
}
