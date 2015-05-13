package com.tomaskostadinov.weatherapp.helper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.annotation.SuppressLint;

public class ServiceHandler {
    private String country = "county";
    private static Integer temperature = 0;
    private String humidity = "humidity";
    private String pressure = "pressure";
    private String urlString = null;
    private Integer kelvin = 272;

    public volatile boolean parsingComplete = true;
    public ServiceHandler(String url){
        this.urlString = url;
    }
    public String getCountry(){
        return country;
    }
    public Integer getTemperature(){
        temperature =  Math.round(temperature) - kelvin;
        return temperature;
    }
    public String getHumidity(){
        return humidity;
    }
    public String getPressure(){
        return pressure;
    }

    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            JSONObject reader = new JSONObject(in);

            JSONObject sys  = reader.getJSONObject("sys");
            country = sys.getString("country");

            JSONObject main  = reader.getJSONObject("main");
            temperature = main.getInt("temp");
            pressure = main.getString("pressure");
            //humidity = main.getString("humidity");

            parsingComplete = false;



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void fetchJSON(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    String data = convertStreamToString(stream);

                    readAndParseJSON(data);
                    stream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
