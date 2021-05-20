package com.example.spotihy11;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SearchSongActivity extends AppCompatActivity {
    String username, ArtistName, AlbumName, Genre;
    TextView tv;
    Socket tempSocket;
    ObjectInputStream in;
    ObjectOutputStream out;
    ListView SongsList;
    ArrayAdapter adapter;
    ArrayList<String> songs;
    AlertDialog.Builder mode;
    Intent onlinePlayer;
    Intent offlinePlayer;
    AlertDialog popup;
    String selected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_song);
        tv = (TextView)findViewById(R.id.username);
        mode = new AlertDialog.Builder(SearchSongActivity.this);
        popup = new AlertDialog.Builder(SearchSongActivity.this).create();
        if(getIntent().hasExtra("package com.example.spotihy.user")){
            username = getIntent().getExtras().getString("package com.example.spotihy.user");
            tv.setText(username);
        }
        ArtistName = getIntent().getExtras().getString("package com.example.spotihy.artistname");
        if(getIntent().hasExtra("songs")){
            songs = (ArrayList<String>) getIntent().getSerializableExtra("songs");
        }
        else{
            songs =((MyApplication) this.getApplication()).getSongs();
        }
        tempSocket = (((MyApplication) this.getApplication()).getSocket());
        in = (((MyApplication) this.getApplication()).getIn());
        out = (((MyApplication) this.getApplication()).getOut());

        SongsList = (ListView)findViewById(R.id.SongsList);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,songs);
        SongsList.setAdapter(adapter);
    }
    public void onStart(){
        super.onStart();
        SongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected= SongsList.getItemAtPosition(position).toString();
                SearchSong runner = new SearchSong();
                runner.execute(selected);

            }
        });
    }
    private class SearchSong extends AsyncTask<String, String, String> {
        private String resp;
        ProgressDialog progressDialog;
        String sendMessage = "";
        boolean songFound;
        @Override
        protected String doInBackground(String... strings) {
            try{
                sendMessage = strings[0];
                try{
                    out.writeObject(sendMessage); //stelnei song
                    out.flush();
                    publishProgress();
                    Thread.sleep(2500);
                    songFound = (boolean)in.readObject();
                    if(songFound) {
                        Genre = (String) in.readObject();
                        AlbumName = (String) in.readObject();
                        int FilesAmount = (int) in.readObject();
                        onlinePlayer = new Intent(getApplicationContext(), OnlinePlayerActivity.class);
                        offlinePlayer = new Intent(getApplicationContext(), PlayerActivity.class);
                        onlinePlayer.putExtra("filesamount", FilesAmount);
                        offlinePlayer.putExtra("filesamount", FilesAmount);
                        onlinePlayer.putExtra("genre", Genre);
                        offlinePlayer.putExtra("genre", Genre);
                        onlinePlayer.putExtra("album", AlbumName);
                        offlinePlayer.putExtra("album", AlbumName);
                    }
                }catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }
        @Override
        protected void onPostExecute(String result) {
            if(songFound) {
                progressDialog.dismiss();
                onlinePlayer.putExtra("user", username);
                offlinePlayer.putExtra("user", username);
                onlinePlayer.putExtra("songname", selected);
                offlinePlayer.putExtra("songname", selected);
                onlinePlayer.putExtra("artist", ArtistName);
                offlinePlayer.putExtra("artist", ArtistName);

                mode.setTitle("Warning");
                mode.setMessage("Select mode");
                mode.setPositiveButton("OFFLINE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        offlinePlayer.putExtra("mode", "OFFLINE");
                        startActivity(offlinePlayer);
                    }
                });
                mode.setNegativeButton("ONLINE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        onlinePlayer.putExtra("mode", "ONLINE");
                        startActivity(onlinePlayer);
                    }
                });
                mode.show();
            }
            else{
                popup.setTitle("Warning");
                popup.setMessage("Invalid song name");
                popup.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int k) {
                        dialog.dismiss();
                        Intent last = new Intent(getApplicationContext(),LastActivity.class);
                        startActivity(last);
                    }
                });
                popup.show();
            }
        }

        protected void onProgressUpdate(String... progress) {
            progressDialog = ProgressDialog.show(SearchSongActivity.this,
                    "Wait",
                    "Searching your song...");
        }
    }
}