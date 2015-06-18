package com.tomaskostadinov.weatherapp.helper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Tomas on 03.06.2015.
 */
public class WeatherForecastHelper {

    JSONObject reader, main;
    JSONArray weather;
    private JSONArray list;

    public Integer date, temp, weather_id;
    public String description;

    public void ParseData(String in){
        try {
            reader = new JSONObject(in);
            this.list  = reader.getJSONArray("list");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getWeatherForecastForId(Integer i){
        try {
            JSONObject jDayForecast = list.getJSONObject(i);
            date = jDayForecast.getInt("dt");
            main = jDayForecast.getJSONObject("main");
            temp = main.getInt("temp");
            weather = jDayForecast.getJSONArray("weather");
            JSONObject JSONWeather = weather.getJSONObject(0);
            description = JSONWeather.getString("description");
            weather_id = JSONWeather.getInt("id");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
