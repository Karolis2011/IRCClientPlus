package com.karolis_apps.irccp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

public class testHub extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_hub);
    }

    public void InitiateTesting2(View v) {
        Intent test = new Intent(this, channelView.class);
        startActivity(test);
    }
}
