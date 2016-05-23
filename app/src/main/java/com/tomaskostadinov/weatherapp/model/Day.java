package com.tomaskostadinov.weatherapp.model;

/**
 * Created by Tomas on 03.08.2015
 */
public class Day {
    public Integer time;
    public Double max_temp;
    public Double min_temp;
    public Double pressoure;
    public Integer humidity;
    public Integer weatherid;
    public String description;
    public Double windspeed;
    public Day(
            Integer time,
            Double max_temp,
            Double min_temp,
            Double pressoure,
            Integer humidity,
            Integer weatherid,
            String description,
            Double windspeed) {
        this.time = time;
        this.max_temp = max_temp;
        this.min_temp = min_temp;
        this.pressoure = pressoure;
        this.humidity = humidity;
        this.weatherid = weatherid;
        this.description = description;
        this.windspeed = windspeed;
    }
}


