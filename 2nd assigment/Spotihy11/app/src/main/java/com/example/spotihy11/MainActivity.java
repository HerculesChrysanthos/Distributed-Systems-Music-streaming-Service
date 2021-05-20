package com.example.spotihy11;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    Button continue1;
    AlertDialog WrongName;
    EditText UserName ;
    Socket tempSocket;
    ObjectInputStream in;
    ObjectOutputStream out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         continue1 = (Button) findViewById(R.id.continue1);
         WrongName = new AlertDialog.Builder(MainActivity.this).create();

    }
    public void onStart(){
        super.onStart();
        tempSocket = ((MyApplication) this.getApplication()).getSocket();
        in = ((MyApplication) this.getApplication()).getIn();
        out = ((MyApplication) this.getApplication()).getOut();
        continue1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                 UserName = (EditText)findViewById(R.id.UserName);
                if(UserName.getText().toString().equals("")){
                    WrongName.setTitle("Warning");
                    WrongName.setMessage("Invalid username. Try again!");
                    WrongName.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int k) {
                            dialog.dismiss();
                        }
                    });
                    WrongName.show();
                }
                else {
                    AsyncTaskRunner runner = new AsyncTaskRunner();
                    runner.execute(UserName.getText().toString());

                }
            }
        });
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private String resp;
        ProgressDialog progressDialog;
        String sendMessage = "";
        @Override
        protected String doInBackground(String... params) {
            try {
                int time = 1000;
                Thread.sleep(time);
                try {
                    sendMessage = params[0];
                    out.writeObject(sendMessage);
                    out.flush();
                } catch (UnknownHostException unknownHost) {
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
            Intent searchArt = new Intent(getApplicationContext(),SearchArtistActivity.class);
            searchArt.putExtra("package com.example.spotihy.user",params[0]);
            startActivity(searchArt);
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
           progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Wait",
                    "Connecting to server...");
        }
    }
}