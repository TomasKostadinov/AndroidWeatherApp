package com.tomaskostadinov.weatherapp.helper;


import com.tomaskostadinov.weatherapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tomas on 17.05.2015.
 */
public class WeatherHelper {

    private NotificationHelper nh;

    private Double kelvin = 272.15;
    private String city = "lol";
    private String description, formattedDater;
    private Integer weatherid,sunrise, sunset, humidity, pressure;
    private Double temperature_max, speed;
    private Integer istat = R.drawable.ic_sunny;

    public void setCity(String in){
        city = in;
    }

    public void setDescription(String in){
        description = in;
    }

    public void setWeatherId(Integer in){
        weatherid = in;
    }

    public void setSunrise(Integer in){
        sunrise = in;
    }

    public void setSunset(Integer in){
        sunset = in;
    }

    public void setTemperature_max(Double in){
        temperature_max = in;
    }

    public void setHumidity(Integer in){
        humidity = in;
    }

    public void setPressure(Integer in){
        pressure = in;
    }

    public void setSpeed(Double in){
        speed = in;
    }

    public String getCity(){
        return city;
    }
    public String getDescription(){
        return description;
    }

    public Integer getWeatherId(){
        return weatherid;
    }

    public Integer getSunrise(){
        return sunrise;
    }

    public Integer getSunset(){
        return sunset;
    }

    public Double getTemperature_max(){
        return temperature_max;
    }

    public Integer getHumidity(){
        return humidity;
    }

    public Integer getPressure(){
        return pressure;
    }

    public Double getSpeed(){
        return speed;
    }

    public void ParseData(String in){
        try {
            JSONObject reader = new JSONObject(in);
            setCity(reader.getString("name"));

            JSONArray weather  = reader.getJSONArray("weather");
            JSONObject JSONWeather = weather.getJSONObject(0);
            setDescription(JSONWeather.getString("description"));
            setWeatherId(JSONWeather.getInt("id"));

            JSONObject sys  = reader.getJSONObject("sys");
            //String country = sys.getString("country");
            setSunrise(sys.getInt("sunrise"));
            setSunset(sys.getInt("sunset"));

            JSONObject main  = reader.getJSONObject("main");
            //Integer temperature_min = main.getInt("temp_min");
            setTemperature_max(main.getDouble("temp_max"));
            setHumidity(main.getInt("humidity"));
            setPressure(main.getInt("pressure"));

            JSONObject wind  = reader.getJSONObject("wind");
            setSpeed(wind.getDouble("speed"));
            //temperature_min =  Math.round(temperature_min) - kelvin;
            setTemperature_max(temperature_max);// - kelvin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer convertWeather(Integer ID){
        switch (ID){
            case 200:
            case 201:
            case 202:
            case 210:
            case 211:
            case 212:
            case 221:
            case 230:
            case 231:
            case 232:
                istat = R.drawable.ic_rain;
                break;
            case 300:
            case 301:
            case 302:
            case 310:
            case 311:
            case 312:
            case 313:
            case 314:
            case 321:
                istat = R.drawable.ic_rain;
                break;
            case 500:
            case 501:
            case 502:
            case 503:
            case 504:
            case 511:
            case 520:
            case 521:
            case 522:
            case 531:
                istat = R.drawable.ic_rain;
                break;
            case 600:
            case 601:
            case 602:
            case 611:
            case 612:
            case 615:
            case 616:
            case 620:
            case 621:
            case 622:
                istat = R.drawable.ic_rain;
                //stat = R.drawable.ic_snow;
                break;
            case 700:
            case 711:
            case 721:
            case 731:
            case 741:
            case 751:
            case 761:
            case 762:
            case 771:
            case 781:
                istat = R.drawable.ic_cloud;
                break;
            case 800:
                istat = R.drawable.ic_sunny;
                break;
            case 801:
            case 802:
                istat = R.drawable.ic_cloudy;
                break;
            case 803:
            case 804:
                istat = R.drawable.ic_cloud;
                break;
            case 900:
            case 901:
            case 902:
            case 903:
            case 904:
            case 905:
            case 906:
                istat = R.drawable.ic_cloud;
                break;
            default:
                istat = R.drawable.ic_sunny;
                break;
        }
        return istat;
    }

    public String convertTime(Integer Time){
        Date date = new Date(Time*1000L); // *1000 Convertiert Sekunden in Millisekunden
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM, HH:mm:ss"); // Datumsformat
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT+2")); // Zeitzone
        formattedDater = sdf.format(date);
        return formattedDater;
    }

}