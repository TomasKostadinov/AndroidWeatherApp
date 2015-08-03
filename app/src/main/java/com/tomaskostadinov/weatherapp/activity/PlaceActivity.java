package com.tomaskostadinov.weatherapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.adapter.PlaceAdapter;
import com.tomaskostadinov.weatherapp.model.Place;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Tomas on 31.05.2015.
 * Class for testing new desings, debug only
 */
public class PlaceActivity extends AppCompatActivity {

    Toolbar toolbar;
    ScrollView ScrollView;

    public ArrayList<Place> items;
    PlaceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testlayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        items = new ArrayList<>();
        downloadJSON();

        RecyclerView rvUsers = (RecyclerView) findViewById(R.id.rvUsers);
        // Create adapter passing in the sample user data
        adapter = new PlaceAdapter(this, getThronesCharacters());
        // Attach the adapter to the recyclerview to populate items
        rvUsers.setAdapter(adapter);
        // Set layout manager to position the items
        rvUsers.setLayoutManager(new LinearLayoutManager(this));


    }

    private ArrayList<Place> getThronesCharacters() {
        return items;
    }

    public void downloadJSON(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://192.168.43.2/api/dj/data.json", new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                String in = new String(response);
                parse(in);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)

                Toast.makeText(PlaceActivity.this, "Fail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    public void parse(String in){
        try {
            JSONObject reader = new JSONObject(in);

            JSONArray weather  = reader.getJSONArray("voting");
            for (int i = 0; i < weather.length(); i++) {
                Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show();
                JSONObject JSONWeather = weather.getJSONObject(i);
                String song = JSONWeather.getString("song");
                String interpret = JSONWeather.getString("interpret");
                String logo = JSONWeather.getString("logo");
                Integer points = JSONWeather.getInt("points");
                Long timereq = JSONWeather.getLong("timerequested");
                items.add(new Place(song, interpret, points, 2, timereq));
                adapter.notifyItemInserted(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
