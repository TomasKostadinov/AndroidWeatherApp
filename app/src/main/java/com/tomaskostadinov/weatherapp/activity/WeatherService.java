package com.tomaskostadinov.weatherapp.activity;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.helper.NotificationHelper;
import com.tomaskostadinov.weatherapp.helper.WeatherHelper;

import org.apache.http.Header;

/**
 * Created by Tomas on 27.05.2015.
 */

public class WeatherService extends Service {

    public WeatherHelper wh;
    public NotificationHelper nh;

    public String city, countrycode, unit, langu;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        Toast.makeText(getBaseContext(), "Loading Callback", Toast.LENGTH_LONG).show();
        getWeatherData();
        nh.Notificate(
                this,
               "lol in " + city + ", " + countrycode,
                "Xd",
                "Wettervorhersage",
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    public void getWeatherData(){
        wh = new WeatherHelper();
        nh = new NotificationHelper();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //String city = settings.getString("city", null);
        //SharedPreferences prefs = this.getSharedPreferences("Location", Context.MODE_PRIVATE);
        city = prefs.getString("location", "Berlin");
        countrycode = prefs.getString("countrykey", "DE");
        unit = prefs.getString("unitcode", "metric");
        langu = prefs.getString("lang", "de");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://api.openweathermap.org/data/2.5/weather?q=" + city + "," + countrycode + "&units=" + unit +"&lang=" + langu, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                String in = new String(response);
                if(in != ""){
                    wh.ParseData(in);
                    noti();
                } else {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

    }

    void noti(){

        nh.Notificate(
                this,
                String.format("%.1f", wh.getTemperature_max()) + " in " + city + ", " + countrycode,
                wh.getDescription(),
                "Wettervorhersage",
                wh.convertWeather(wh.getWeatherId()),
                R.mipmap.ic_launcher);
    }


}

