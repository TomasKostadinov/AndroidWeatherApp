package com.tomaskostadinov.weatherapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.tomaskostadinov.weatherapp.R;

/**
 * Created by Tomas on 13.05.2015.
 */
public class SettingsActivity extends ActionBarActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Button clickButton = (Button) findViewById(R.id.button);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                //startActivity(browserIntent);
                SharedPreferences pref = getApplicationContext().getSharedPreferences("Location", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                //editor.putBoolean("key_name1", true);           // Saving boolean - true/false
                //editor.putInt("key_name2", "int value");        // Saving integer
                //editor.putFloat("key_name3", "float value");    // Saving float
                //editor.putLong("key_name4", "long value");      // Saving long
                editor.putString("location", "Heidenheim an der Brenz");  // Saving string
                editor.apply(); // commit changes
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cancel, menu);
        return true;
    }

}