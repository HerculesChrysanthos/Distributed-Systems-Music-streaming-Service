package com.example.spotihy11;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {
    public static final int progress_bar_type = 0;
    String username;
    TextView UserName;
    TextView SongName;
    TextView ArtistName;
    TextView Genre;
    TextView Album;
    TextView currentTime;
    TextView totalTime;
    Socket tempSocket;
    ObjectInputStream in;
    ObjectOutputStream out;
    String mode;
    Button PlayButton;
    Button PauseButton;
    MediaPlayer player;
    SeekBar seekbar;
    Handler myhandler;
    ProgressDialog progressDialog;
    AlertDialog.Builder searchAnotherArtist ;
    private double startTime = 0;
    private double finalTime = 0;
    int FilesAmount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        UserName = (TextView)findViewById(R.id.username);
        SongName = (TextView)findViewById(R.id.SongName);
        ArtistName = (TextView)findViewById(R.id.ArtistName);
        Genre = (TextView)findViewById(R.id.Genre);
        Album = (TextView)findViewById(R.id.Album);
        currentTime = (TextView)findViewById(R.id.CurrentTime);
        totalTime = (TextView)findViewById(R.id.TotalTime);
        PlayButton = (Button)findViewById(R.id.PlayButton);
        PlayButton.setEnabled(false);
        PauseButton = (Button)findViewById(R.id.PauseButton);
        seekbar =  (SeekBar) findViewById(R.id.PlayingBar);
        player = new MediaPlayer();
        myhandler = new Handler();
        searchAnotherArtist = new AlertDialog.Builder(PlayerActivity.this);

    }

    protected Dialog onCreateDialog(int id){
        switch (id) {
            case progress_bar_type:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Downloading file. Please wait...");
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(true);
                progressDialog.show();
                return progressDialog;
            default:
                return null;
        }
    }
    public void onStart() {
        super.onStart();
        username = getIntent().getExtras().getString("user");
        UserName.setText(username);
        String song = getIntent().getExtras().getString("songname");
        SongName.setText(song);
        ArtistName.setText(getIntent().getExtras().getString("artist"));
        Genre.setText(getIntent().getExtras().getString("genre"));
        Album.setText(getIntent().getExtras().getString("album"));
        FilesAmount = getIntent().getExtras().getInt("filesamount");
        if(getIntent().hasExtra("mode")){
            mode = getIntent().getExtras().getString("mode");
        }

        tempSocket = (((MyApplication) this.getApplication()).getSocket());
        in = (((MyApplication) this.getApplication()).getIn());
        out = (((MyApplication) this.getApplication()).getOut());
        searchAnotherArtist.setTitle("Warning");
        searchAnotherArtist.setMessage("Do you want to search for another artist?");
        mediaPlayer play = new mediaPlayer();
        play.execute(mode,song);
        PauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player!=null && player.isPlaying()){
                    player.pause();
                    PauseButton.setEnabled(false);
                    PlayButton.setEnabled(true);
                }
            }
        });
        PlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player!=null){
                    player.start();
                    PauseButton.setEnabled(true);
                    PlayButton.setEnabled(false);
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(player != null && fromUser){
                    player.seekTo(progress * 1000);
                }
            }
        });
    }

    private class mediaPlayer extends AsyncTask<String, String, String> {
        FileOutputStream fos = null;
        MusicFile mf;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            showDialog(progress_bar_type);
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                int sizeOfFiles = (int) in.readObject(); //piecessize
                int lastpiece = (int) in.readObject(); //last piece size

                ArrayList<MusicFile> mflist = new ArrayList<MusicFile>();
                File file = new File(getFilesDir()+"/"+strings[1]+".mp3");
                int lenghtOfFile = (FilesAmount-1)*sizeOfFiles + lastpiece;
                for (int i = 0; i < FilesAmount; i++) {
                    Thread.sleep(400);
                    if(i==FilesAmount-1){ //emfanizei to pososto tou download
                        publishProgress(""+(int)((((FilesAmount-1)*sizeOfFiles +sizeOfFiles + lastpiece )*100)/lenghtOfFile));
                    }
                    else {
                        publishProgress("" + (int) ((((i+1)*sizeOfFiles) * 100) / lenghtOfFile));
                    }
                    mf = (MusicFile) in.readObject();
                    mflist.add(mf);
                }
                try {
                    fos = new FileOutputStream(file);
                    for (int i = 0; i < mflist.size(); i++) {
                        fos.write(mflist.get(i).getMusicFileExtract(), 0, mflist.get(i).getMusicFileExtract().length);
                    }
                    fos.close();
                }catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                Uri myUry = Uri.parse(file.getPath());
                player.setDataSource(PlayerActivity.this,myUry);
                player.prepare();
                player.start();
                finalTime = player.getDuration();
                startTime = player.getCurrentPosition();


                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        if(player != null){
                            int mCurrentPosition = player.getCurrentPosition() / 1000;
                            seekbar.setProgress(mCurrentPosition);
                        }
                        myhandler.postDelayed(this, 1000);
                        startTime = player.getCurrentPosition();
                        currentTime.setText(String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes((long) startTime)))
                        );
                    }
                });

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        searchAnotherArtist.setPositiveButton( "YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                Intent searchArtAgain = new Intent(getApplicationContext(),SearchArtistActivity.class);
                                searchArtAgain.putExtra("package com.example.spotihy.user",username);
                                searchArtAgain.putExtra("check","ok");
                                startActivity(searchArtAgain);
                            }
                        });
                        searchAnotherArtist.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                Intent bye = new Intent(getApplicationContext(),LastActivity.class);
                                bye.putExtra("package com.example.spotihy.user",username);
                                startActivity(bye);
                            }
                        });
                        searchAnotherArtist.show();
                    }
                });
            }catch(ClassNotFoundException | IOException | InterruptedException classnot) {
                System.err.println("Data received in unknown format");
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(String... progress) {
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(String file_url) {
            totalTime.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    finalTime)))
            );
            currentTime.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    startTime)))
            );
            dismissDialog(progress_bar_type);
            seekbar.setMax((player.getDuration())/1000);
        }
    }
}