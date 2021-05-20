package com.example.spotihy11;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

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

public class OnlinePlayerActivity extends AppCompatActivity {
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
    AlertDialog.Builder searchAnotherArtist ;
    int FilesAmount;
    ArrayList<MediaPlayer> mplist;
    int playerCounter;
    boolean flag = true;
    SeekBar seekbar;
    Handler myhandler;
    private double startTime = 0;
    MediaPlayer tempMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_player);
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
        myhandler = new Handler();
        searchAnotherArtist = new AlertDialog.Builder(OnlinePlayerActivity.this);
        playerCounter = 0;
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
        mplist = new ArrayList<MediaPlayer>();
        for(int j=playerCounter;j<FilesAmount;j++){
            tempMediaPlayer = new MediaPlayer();
            mplist.add(tempMediaPlayer);
        }
        PauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mplist.get(playerCounter) != null && mplist.get(playerCounter).isPlaying()) {
                    mplist.get(playerCounter).pause();
                    PauseButton.setEnabled(false);
                    PlayButton.setEnabled(true);
                }
            }
        });
        PlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mplist.get(playerCounter) != null) {
                    mplist.get(playerCounter).start();
                    PauseButton.setEnabled(true);
                    PlayButton.setEnabled(false);
                }
            }
        });
    }

    private class mediaPlayer extends AsyncTask<String, String, String> {
        FileOutputStream fos = null;
        MusicFile mf;
        ArrayList<File> files = new ArrayList<>();
        int ttime = 0;
        @Override
        protected String doInBackground(String... strings) {
            try {
                int sizeOfFiles = (int) in.readObject(); //piecessize
                int lastpiece = (int) in.readObject(); //last piece size

                for(int i=0;i<FilesAmount;i++){
                    try{
                        mf = (MusicFile) in.readObject();
                        File tempFile = File.createTempFile(strings[1],"mp3",getCacheDir());
                        files.add(tempFile);
                        if(i==0) {
                            fos = new FileOutputStream(tempFile);
                            fos.write(mf.getMusicFileExtract());
                            fos.close();
                            Uri myUry = Uri.parse(tempFile.getPath());
                            mplist.get(0).setDataSource(OnlinePlayerActivity.this, myUry);
                            mplist.get(0).prepare();
                            mplist.get(0).start();
                        }
                        else{
                            fos = new FileOutputStream(tempFile);
                            fos.write(mf.getMusicFileExtract());
                            fos.close();
                            Uri myUry = Uri.parse(tempFile.getPath());
                            mplist.get(i).setDataSource(OnlinePlayerActivity.this, myUry);
                            mplist.get(i).prepare();
                            mplist.get(i-1).setNextMediaPlayer(mplist.get(i));
                        }
                        ttime += mplist.get(i).getDuration();
                    }catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                OnlinePlayerActivity.this.runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        seekbar.setEnabled(false);
                        int mCurrentPosition =0;
                        startTime =0;
                        for(int j=0;j<mplist.size();j++){
                            mCurrentPosition += mplist.get(j).getCurrentPosition() / 1000;
                            startTime+= mplist.get(j).getCurrentPosition();
                        }

                            seekbar.setProgress(mCurrentPosition);
                        myhandler.postDelayed(this, 1000);

                        currentTime.setText(String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes((long) startTime)))
                        );
                        totalTime.setText(String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes((long) ttime),
                                TimeUnit.MILLISECONDS.toSeconds((long) ttime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                                ttime)))
                        );
                        seekbar.setMax(ttime/1000);
                    }
                });

                while(flag) {
                    mplist.get(playerCounter).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if(playerCounter==FilesAmount-2){
                                flag = false;
                            }
                            playerCounter++;
                            Toast.makeText(getApplicationContext(), String.valueOf(playerCounter), Toast.LENGTH_SHORT).show();

                        }
                    });

                    mplist.get(FilesAmount - 1).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            for (File f : files) {
                                boolean deleted = false;
                                try {
                                    deleted = f.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (!deleted) {
                                    f.deleteOnExit();
                                }
                            }
                            searchAnotherArtist.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    Intent searchArtAgain = new Intent(getApplicationContext(), SearchArtistActivity.class);
                                    searchArtAgain.putExtra("package com.example.spotihy.user", username);
                                    searchArtAgain.putExtra("check", "ok");
                                    startActivity(searchArtAgain);
                                }
                            });
                            searchAnotherArtist.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    Intent bye = new Intent(getApplicationContext(), LastActivity.class);
                                    bye.putExtra("package com.example.spotihy.user", username);
                                    startActivity(bye);
                                }
                            });
                            searchAnotherArtist.show();
                        }
                    });
                }
            }catch(ClassNotFoundException | IOException classnot) {
                System.err.println("Data received in unknown format");
            }
            return null;
        }
    }
}