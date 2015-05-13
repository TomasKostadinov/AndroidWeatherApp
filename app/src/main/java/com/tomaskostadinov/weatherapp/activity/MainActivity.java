package com.tomaskostadinov.weatherapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.helper.ServiceHandler;

import org.apache.http.Header;
import org.json.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements FragmentDrawer.FragmentDrawerListener {

    private static String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private String url1 = "http://api.openweathermap.org/data/2.5/weather?q=";
    private ServiceHandler obj;
    TextView temp, l, windspeed, press, hum, suns, sunr, desc;
    Integer stat = 0;
    String url = "Heidenheim an der Brenz";
    ImageView todayStat;
    ScrollView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(url);
        //mToolbar.setLogo(R.mipmap.ic_launcher);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);
        // display the first navigation drawer view on app launch
        displayView(0);

        temp = (TextView)findViewById(R.id.t);
        l = (TextView)findViewById(R.id.l);
        windspeed = (TextView)findViewById(R.id.windspeed);
        press = (TextView)findViewById(R.id.pressure);
        hum = (TextView)findViewById(R.id.humidity);
        sunr = (TextView)findViewById(R.id.sunrise);
        suns = (TextView)findViewById(R.id.sunset);
        desc = (TextView)findViewById(R.id.desc);
        todayStat = (ImageView) findViewById(R.id.stattoday);
        sv = (ScrollView) findViewById(R.id.sv);
        getWeatherData();
        sv.setVisibility(View.GONE);
    }
    public void getWeatherData(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://api.openweathermap.org/data/2.5/weather?q=" + url + ",DE&lang=de", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                //temp.setText("Loading");
                SnackbarManager.show(
                        Snackbar.with(MainActivity.this)
                                .text(R.string.downloading_data)
                                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT));
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                String in = new String(response);
                if(in != ""){
                    UpdateData(in);
                    sv.setVisibility(View.VISIBLE);
                } else {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                sv.setVisibility(View.GONE);
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }
    void UpdateData(String in){
        try {
            Double kelvin = 272.15;
            JSONObject reader = new JSONObject(in);
            String city = reader.getString("name");

            JSONArray weather  = reader.getJSONArray("weather");
            JSONObject JSONWeather = weather.getJSONObject(0);
            String description = JSONWeather.getString("description");
            Integer weatherid = JSONWeather.getInt("id");

            JSONObject sys  = reader.getJSONObject("sys");
            //String country = sys.getString("country");
            Integer sunrise = sys.getInt("sunrise");
            Integer sunset = sys.getInt("sunset");

            JSONObject main  = reader.getJSONObject("main");
            //Integer temperature_min = main.getInt("temp_min");
            Double temperature_max = main.getDouble("temp_max");
            Integer humidity = main.getInt("humidity");
            Integer pressure = main.getInt("pressure");

            JSONObject wind  = reader.getJSONObject("wind");
            Double speed = wind.getDouble("speed");
            //temperature_min =  Math.round(temperature_min) - kelvin;
            temperature_max =  temperature_max - kelvin;
            temp.setText(String.format("%.1f", temperature_max) + "°");
            l.setText(city);
            desc.setText(description);
            windspeed.setText(getResources().getString(R.string.windspeed) + ": " + speed.toString() + " km/h");
            hum.setText(getResources().getString(R.string.humidity) + ": " + humidity.toString() + "%");
            press.setText(getResources().getString(R.string.pressure) + ": " + pressure.toString() + " hPa");
            sunr.setText(getResources().getString(R.string.sunrise) + ": " + convertTime(sunrise) + " Uhr");
            suns.setText(getResources().getString(R.string.sunset) + ": " + convertTime(sunset) + " Uhr");
            todayStat.setImageResource(convertWeather(weatherid));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String convertTime(Integer Time){
        Date date = new Date(Time*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); // the format of your date
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT+2")); // give a timezone reference for formating (see comment at the bottom
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    Integer convertWeather(Integer ID){
        Integer istat = R.drawable.ic_sunny;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            /**
             * Taken from https://github.com/nispok/snackbar
             */
            SnackbarManager.show(
                    Snackbar.with(this)
                            .text(String.format("Noch nicht vorhanden :("))
                            .actionLabel(R.string.close)
                            .actionColor(Color.parseColor("#ff1744"))
                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG));
            //startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            return true;
        }

        if (id == R.id.action_about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            return true;
        }

        if (id == R.id.action_exit) {
            System.exit(0);
            return true;
        }

        if(id == R.id.action_refresh){
            //open();
            getWeatherData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case 1:
                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case 2:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                break;
            case 3:
                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case 4:
                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            default:
                break;
        }

    }
}