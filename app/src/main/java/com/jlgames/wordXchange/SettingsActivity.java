package com.jlgames.wordXchange;

/**
 * Created by jesse on 11/8/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.jlgames.wordXchange.R;


public class SettingsActivity extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference);
        context = getApplicationContext(); // or activity.getApplicationContext()
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(Color.WHITE);
        myToolbar.setLogo(R.mipmap.ic_launcher);
        myToolbar.setTitle("Main Settings");
        myToolbar.showOverflowMenu();
        setSupportActionBar(myToolbar);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

            //Log.e("prefs","key: " + key);
            Preference p = findPreference(key);
            if(p instanceof ListPreference)
            {
                if(key.equals("timerPref"))
                {
                    prefs.edit().putString("timerSelected",prefs.getString("timerPref","30")).apply();
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}

