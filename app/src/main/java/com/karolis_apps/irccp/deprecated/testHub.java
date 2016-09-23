package com.karolis_apps.irccp.deprecated;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.karolis_apps.irccp.R;
import com.karolis_apps.irccp.deprecated.core.ClientManager;
import com.karolis_apps.irccp.deprecated.core.IRC.ManagedIRCClient;

public class testHub extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_hub);

        if (savedInstanceState == null) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String default_username = getResources().getString(R.string.default_username);
            String default_nickname = getResources().getString(R.string.default_nickname);
            String default_realname = getResources().getString(R.string.default_realname);
            String default_server = getResources().getString(R.string.default_server);
            int default_port = getResources().getInteger(R.integer.default_port);
            boolean default_ssl = getResources().getBoolean(R.bool.default_ssl);
            EditText ti = (EditText) findViewById(R.id.nickTextBox);
            ti.setText(sharedPref.getString(getResources().getString(R.string.nickname), default_nickname));
            ti = (EditText) findViewById(R.id.userTextBox);
            ti.setText(sharedPref.getString(getResources().getString(R.string.username), default_username));
            ti = (EditText) findViewById(R.id.realTextBox);
            ti.setText(sharedPref.getString(getResources().getString(R.string.realname), default_realname));
            ti = (EditText) findViewById(R.id.serverTextBox);
            ti.setText(sharedPref.getString(getResources().getString(R.string.server), default_server));
            ti = (EditText) findViewById(R.id.portTextBox);
            int port = sharedPref.getInt(getResources().getString(R.string.pport), default_port);
            ti.setText(String.valueOf(port));
            CheckBox ch = (CheckBox) findViewById(R.id.sslCheckBox);
            ch.setChecked(sharedPref.getBoolean(getResources().getString(R.string.ssl), default_ssl));
        }
    }

    public void Show(View v) {
        this.SavePrefs(v);
        Intent test = new Intent(this, channelView.class);
        startActivity(test);
    }

    public void Disconnect(View v) {
        ManagedIRCClient ircClient = ClientManager.getInstance().GetClientByName("Main");
        if(ircClient != null){
            ircClient.Disconnect(null);
        }
    }

    public void SavePrefs(View v) {
        //Data harvesting:
        String nickname;
        String username;
        String realname;
        String server;
        int port;
        boolean ssl;
        EditText ti = (EditText) findViewById(R.id.nickTextBox);
        nickname = ti.getText().toString();
        ti = (EditText) findViewById(R.id.userTextBox);
        username = ti.getText().toString();
        ti = (EditText) findViewById(R.id.realTextBox);
        realname = ti.getText().toString();
        ti = (EditText) findViewById(R.id.serverTextBox);
        server = ti.getText().toString();
        ti = (EditText) findViewById(R.id.portTextBox);
        port = Integer.parseInt(ti.getText().toString());
        CheckBox ch = (CheckBox) findViewById(R.id.sslCheckBox);
        ssl = ch.isChecked();
        //Saving in preferences
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getResources().getString(R.string.nickname), nickname);
        editor.putString(getResources().getString(R.string.username), username);
        editor.putString(getResources().getString(R.string.realname), realname);
        editor.putString(getResources().getString(R.string.server), server);
        editor.putInt(getResources().getString(R.string.pport), port);
        editor.putBoolean(getResources().getString(R.string.ssl), ssl);
        editor.apply();
    }
}
