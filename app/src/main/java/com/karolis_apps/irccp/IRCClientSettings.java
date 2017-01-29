package com.karolis_apps.irccp;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class IRCClientSettings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IRCClientSettingsFragment ircClientSettingsFragment = new IRCClientSettingsFragment();
        //ircClientSettingsFragment.setArguments(b);
        getFragmentManager().beginTransaction().replace(android.R.id.content, ircClientSettingsFragment).commit();
    }

    public static class IRCClientSettingsFragment extends PreferenceFragment {

        private String config_id;
        private int preference_id;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle d = getArguments();
            config_id = d.getString("config_id");
            preference_id = d.getInt("pref_id");
            PreferenceManager.setDefaultValues(getApplicationContext(), preference_id, false);
            addPreferencesFromResource(preference_id);
        }
    }
}
