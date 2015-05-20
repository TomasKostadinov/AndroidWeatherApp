package com.tomaskostadinov.weatherapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tomaskostadinov.weatherapp.R;

/**
 * Created by Tomas on 13.05.2015.
 */
public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    EditText et;

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
        et   = (EditText)findViewById(R.id.editText);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("Location", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                //editor.putBoolean("key_name1", true);           // Saving boolean - true/false
                //editor.putInt("key_name2", "int value");        // Saving integer
                //editor.putFloat("key_name3", "float value");    // Saving float
                //editor.putLong("key_name4", "long value");      // Saving long
                editor.putString("location",  et.getText().toString());  // Saving string
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