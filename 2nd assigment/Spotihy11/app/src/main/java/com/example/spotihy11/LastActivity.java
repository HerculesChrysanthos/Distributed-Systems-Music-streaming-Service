package com.example.spotihy11;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last);
        ((MyApplication)this.getApplication()).CloseSocket();
    }
}
