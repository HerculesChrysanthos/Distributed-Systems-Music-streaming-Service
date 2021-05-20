package com.example.spotihy11;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class TempApplication extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String username = getIntent().getExtras().getString("package com.example.spotihy.user");
        String ArtistName =  getIntent().getExtras().getString("package com.example.spotihy.artistname");
        String newIp = getIntent().getExtras().getString("package com.example.spotihy.ip");
        int newPort = getIntent().getExtras().getInt("package com.example.spotihy.port");

        ((MyApplication) this.getApplication()).Redirect(newIp,newPort,false,username,ArtistName);
        Intent SearchShong = new Intent(getApplicationContext(),SearchSongActivity.class);
        SearchShong.putExtra("package com.example.spotihy.user",username);
        SearchShong.putExtra("package com.example.spotihy.artistname",ArtistName);
        startActivity(SearchShong);
    }
}
