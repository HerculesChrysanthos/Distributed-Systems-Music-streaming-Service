package com.example.spotihy11;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SearchArtistActivity extends AppCompatActivity {
    String username;
    TextView tv;
    Socket tempSocket;
    ObjectInputStream in;
    ObjectOutputStream out;
    ArrayList<String> artists;
    ListView ArtistsList;
    ArrayAdapter adapter;
    Intent searchArt;
    Intent change;
    ArrayList<String> songs;
    Boolean searchAgainArtist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_artist);
        tv = (TextView)findViewById(R.id.username);
        artists = (((MyApplication) this.getApplication()).getTotalArtistList());
        ArtistsList = (ListView)findViewById(R.id.ArtistsList);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,artists);
        ArtistsList.setAdapter(adapter);
        songs = new ArrayList<>();
        searchAgainArtist = false;
    }

    public void onStart(){
        super.onStart();
        if(getIntent().hasExtra("package com.example.spotihy.user")){
            username = getIntent().getExtras().getString("package com.example.spotihy.user");
            tv.setText(username);
        }
        if(getIntent().hasExtra("check")){
            searchAgainArtist = true;
        }
        tempSocket = (((MyApplication) this.getApplication()).getSocket());
        in = (((MyApplication) this.getApplication()).getIn());
        out = (((MyApplication) this.getApplication()).getOut());
        ArtistsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = ArtistsList.getItemAtPosition(position).toString();
                SearchArtist runner = new SearchArtist();
                runner.execute(selected);
            }
        });
    }

    private class SearchArtist extends AsyncTask<String, String, String> {
        private String resp;
        ProgressDialog progressDialog;
        String sendMessage = "";
        boolean changeServer = false;
        boolean findCorrectBroker = false;

        @Override
        protected String doInBackground(String... strings) {
            int newPort = 0;
            String newIp = "";
            try{
                int time = 1000;
                Thread.sleep(time);
                sendMessage = strings[0];
                try{
                    if(searchAgainArtist){
                        out.writeObject(true);
                        out.flush();
                    }
                    out.writeObject(sendMessage); //stelnei artist
                    out.flush();
                    findCorrectBroker = (boolean)in.readObject();//sima gia to an o trexwn broker exei ton artist
                    if(!findCorrectBroker){
                        newIp = (String)in.readObject();
                        newPort = (int)in.readObject();
                        changeServer = true;
                    }
                    else{
                        int songsAmount = (int)in.readObject();
                        for(int i=0;i<songsAmount;i++){
                             String tempString = (String)in.readObject();
                              songs.add(tempString);
                        }
                    }
                }catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                resp = e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            searchArt = new Intent(getApplicationContext(),SearchSongActivity.class);
            change = new Intent(getApplicationContext(),TempApplication.class);
            searchArt.putExtra("package com.example.spotihy.user",username);
            change.putExtra("package com.example.spotihy.user",username);
            searchArt.putExtra("package com.example.spotihy.artistname",strings[0]);
            change.putExtra("package com.example.spotihy.artistname",strings[0]);
             if(changeServer){
                 change.putExtra("package com.example.spotihy.ip",newIp);
                 change.putExtra("package com.example.spotihy.port",newPort);
               //  change.putExtra("changeServer","yes");
                 startActivity(change);
             }
             else{
                 searchArt.putExtra("songs",songs);
                 startActivity(searchArt);
             }
            return resp;
        }
        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(SearchArtistActivity.this,
                    "Wait",
                    "Searching artist...");
        }
    }
}