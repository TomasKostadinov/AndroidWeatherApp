package com.tomaskostadinov.weatherapp.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.helper.ServiceHandler;
import com.tomaskostadinov.weatherapp.helper.WeatherHelper;
import com.tomaskostadinov.weatherapp.helper.NotificationHelper;

import org.apache.http.Header;

import android.os.Handler;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    private static String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private ServiceHandler obj;
    TextView temp, loca, windspeed, press, hum, suns, sunr, desc;
    Integer stat = 0;
    ImageView todayStat;
    ScrollView sv;
    RelativeLayout ErrorLayout, LoadingLayout;
    String city = "Berlin";
    String countrycode = "DE";
    String units = "metric";
    String unit;
    private Handler mHandler = new Handler();

    public Integer b = 0;
    NotificationHelper nh;
    WeatherHelper wh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //String city = settings.getString("city", null);
        //SharedPreferences prefs = this.getSharedPreferences("Location", Context.MODE_PRIVATE);
        city = prefs.getString("location", "Berlin");
        countrycode = prefs.getString("countrykey", "DE");
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(city);
        //mToolbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.header_bg));
        //mToolbar.setLogo(R.mipmap.ic_launcher);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);
        // display the first navigation drawer view on app launch
        displayView(0);

        /**
         * Helper Classes
         */

        nh = new NotificationHelper();
        wh = new WeatherHelper();

        /**
         * Views
         */

        temp = (TextView)findViewById(R.id.t);
        loca = (TextView)findViewById(R.id.l);
        windspeed = (TextView)findViewById(R.id.windspeed);
        press = (TextView)findViewById(R.id.pressure);
        hum = (TextView)findViewById(R.id.humidity);
        sunr = (TextView)findViewById(R.id.sunrise);
        suns = (TextView)findViewById(R.id.sunset);
        desc = (TextView)findViewById(R.id.desc);
        todayStat = (ImageView) findViewById(R.id.stattoday);

        /**
         * Layouts
         */

        ErrorLayout = (RelativeLayout) findViewById(R.id.error);
        LoadingLayout = (RelativeLayout) findViewById(R.id.loading);
        sv = (ScrollView) findViewById(R.id.scroll_view);

        /**
         * Setting ScrollView's & ErrorLayout's visibility to gone -> displaying the LoadingLayout
         */

        sv.setVisibility(View.GONE);
        ErrorLayout.setVisibility(View.GONE);

        /**
         * Starting the Download
         */

        getWeatherData();
        Calendar cal = Calendar.getInstance();

        if(prefs.getBoolean("bgprocess", false)) {
            Intent intent = new Intent(this, WeatherService.class);
            PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            // schedule for every 30 seconds
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30 * 1000, pintent);
        }
    }

    public void getWeatherData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //String city = settings.getString("city", null);
        //SharedPreferences prefs = this.getSharedPreferences("Location", Context.MODE_PRIVATE);
        city = prefs.getString("location", "Berlin");
        countrycode = prefs.getString("countrykey", "DE");
        unit = prefs.getString("unitcode", "metric");
        String langu = prefs.getString("lang", "de");
        mToolbar.setSubtitle(city);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://api.openweathermap.org/data/2.5/weather?q=" + city + "," + countrycode + "&units=" + unit +"&lang=" + langu, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
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
                    wh.ParseData(in);
                    UpdateData();
                    sv.setVisibility(View.VISIBLE);
                    LoadingLayout.setVisibility(View.VISIBLE);
                } else {
                    ErrorLayout.setVisibility(View.VISIBLE);
                    sv.setVisibility(View.GONE);
                    LoadingLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                sv.setVisibility(View.GONE);
                LoadingLayout.setVisibility(View.GONE);
                ErrorLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    void UpdateData(){
        loca.setText(wh.getCity());
        temp.setText(String.format("%.1f", wh.getTemperature_max()) + "°");
        desc.setText(wh.getDescription());
        windspeed.setText(getResources().getString(R.string.windspeed) + ": " + wh.getSpeed().toString() + " km/h");
        hum.setText(getResources().getString(R.string.humidity) + ": " + wh.getHumidity().toString() + "%");
        press.setText(getResources().getString(R.string.pressure) + ": " + wh.getPressure().toString() + " hPa");
        sunr.setText(getResources().getString(R.string.sunrise) + ": " + wh.convertTime(wh.getSunrise()) + " Uhr");
        suns.setText(getResources().getString(R.string.sunset) + ": " + wh.convertTime(wh.getSunset()) + " Uhr");
        todayStat.setImageResource(wh.convertWeather(wh.getWeatherId()));
        sendNotification();
    }

    void sendNotification(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("notifications", true)){
        nh.Notificate(
                this,
                String.format("%.1f", wh.getTemperature_max()) + "° in " + city + ", " + countrycode,
                wh.getDescription(),
                "Wettervorhersage",
                wh.convertWeather(wh.getWeatherId()),
                R.mipmap.ic_launcher);
        } else {
            //notin!
        }
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
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
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
        switch (position) {
            case 0:
                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case 1:
                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:tomas.kostadinov@gmx.de?subject=Android Weather App");
                i.setData(data);
                startActivity(i);
                break;
            case 2:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                break;
            case 3:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("doubleback", true)){
            b = b + 1;
            if(b == 2){
                finish();
            }   else if(b == 1){
                Toast.makeText(getBaseContext(), getResources().getString(R.string.back), Toast.LENGTH_LONG).show();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    b = 0;
                }
            }, 2000);
        } else {
            finish();
        }
    }

}