package com.tomaskostadinov.weatherapp.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.helper.NotificationHelper;
import com.tomaskostadinov.weatherapp.helper.WeatherHelper;

import org.apache.http.Header;

import android.os.Handler;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity{

    private Toolbar mToolbar;
    TextView temp, loc, windspeed, press, hum, suns, sunr, desc, currloc;
    ImageView todayStat;
    ScrollView sv;
    ImageButton fab; //The floating action button
    LinearLayout ErrorLayout;
    RelativeLayout LoadingLayout;
    String city, CountryCode, language, unit;

    private Handler mHandler = new Handler();
    public  SharedPreferences prefs;

    public Integer b = 0;
    NotificationHelper NotificationHelper;
    WeatherHelper WeatherHelper;

    public Bundle extras;
    public DrawerLayout drawerLayout;
    public NavigationView navview;
    public SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        extras = getIntent().getExtras();

        if(extras != null){
            city = extras.getString("place");
        } else {
            city = prefs.getString("location", "Berlin");
        }
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //mToolbar.setLogo(R.mipmap.ic_launcher);
        if (mToolbar != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolbar.setNavigationIcon(R.drawable.ic_menu);
        }
        /**
         * Helper classes
         */

        NotificationHelper = new NotificationHelper();
        WeatherHelper = new WeatherHelper();

        /**
         * Layouts
         */

        ErrorLayout = (LinearLayout) findViewById(R.id.error);
        LoadingLayout = (RelativeLayout) findViewById(R.id.loading);
        sv = (ScrollView) findViewById(R.id.scroll_view);

        /**
         * Views
         */

        temp = (TextView)findViewById(R.id.t);
        currloc = (TextView)findViewById(R.id.current_location);
        loc = (TextView)findViewById(R.id.l);
        windspeed = (TextView)findViewById(R.id.windspeed);
        press = (TextView)findViewById(R.id.pressure);
        hum = (TextView)findViewById(R.id.humidity);
        sunr = (TextView)findViewById(R.id.sunrise);
        suns = (TextView)findViewById(R.id.sunset);
        desc = (TextView)findViewById(R.id.desc);
        todayStat = (ImageView) findViewById(R.id.stattoday);
        fab = (ImageButton) findViewById(R.id.imageButton);

        /**
         * Removing that ugly overscroll effect
         * Setting ScrollView's & ErrorLayout's visibility to gone -> displaying the LoadingLayout
         */

        sv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        sv.setVisibility(View.GONE);
        ErrorLayout.setVisibility(View.GONE);

        /**
         * Starting
         */

        getWeatherData(true);

        /**
         * [BETA] Starting, if activated, WeatherService to sync the weather data in Background
         * Current Status: Download fails
         * TODO fix problems
         */

        if(prefs.getBoolean("bgprocess", false)) {
            Intent intent = new Intent(this, WeatherService.class);
            PendingIntent PI = PendingIntent.getService(this, 0, intent, 0);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            // schedule for every 30 seconds
            Calendar cal = Calendar.getInstance();
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30 * 60 * 1000, PI);
        }
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navview = (NavigationView) findViewById(R.id.navigation_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setProgressViewOffset(true, 100, 150);
        mSwipeRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = sv.getScrollY();
                if (scrollY == 0) mSwipeRefreshLayout.setEnabled(true);
                else mSwipeRefreshLayout.setEnabled(false);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getWeatherData(false);
            }});

        navview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.home:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.place:
                        startActivity(new Intent(getApplicationContext(), ForecastActivity.class));
                        return true;
                    case R.id.about:
                        startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));;
                        return true;
                    case R.id.beta:
                        startActivity(new Intent(getApplicationContext(), BetaSettingsActivity.class));;
                        return true;
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getWeatherData(false);
    }

    public void getWeatherData(Boolean notification){
        final Boolean not = notification;
        /**
         * Get settings
         */
        if(extras != null){
            city = extras.getString("place");
            CountryCode = extras.getString("country");
        } else {
            city = prefs.getString("location", "Berlin");
            CountryCode = prefs.getString("countrykey", "DE");
        }
        unit = prefs.getString("unitcode", "metric");
        language = prefs.getString("lang", "de");
        /**
         * Start JSON data download
         */
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://api.openweathermap.org/data/2.5/weather?q=" + city + "," + CountryCode + "&units=" + unit + "&lang=" + language, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                if(mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                // called when response HTTP status is "200 OK"
                String in = new String(response);
                if (in != "") {
                    WeatherHelper.ParseData(in);
                    UpdateData(not);
                    sv.setVisibility(View.VISIBLE);
                    LoadingLayout.setVisibility(View.VISIBLE);
                } else {
                    ErrorLayout.setVisibility(View.VISIBLE);
                    sv.setVisibility(View.GONE);
                    LoadingLayout.setVisibility(View.GONE);
                    getCachedData();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                /**
                 * Called when response HTTP status is "4XX" (eg. 401, 403, 404)
                 * Setting ScrollView's & LoadingLayout's visibility to gone -> displaying the ErrorLayout
                 * TODO Find a better solution for this
                 */
                sv.setVisibility(View.GONE);
                LoadingLayout.setVisibility(View.GONE);
                ErrorLayout.setVisibility(View.VISIBLE);
                getCachedData();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    public void UpdateData(Boolean notification){
        fab.bringToFront();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DesignTest.class));
            }
        });
        /**
         * Writing data to TextView
         */
        loc.setText(WeatherHelper.getCity());
        currloc.setText(WeatherHelper.getCity());
        temp.setText(String.format("%.1f", WeatherHelper.getTemperature_max()) + "°");
        desc.setText(WeatherHelper.getDescription());
        windspeed.setText(WeatherHelper.getSpeed().toString() + " km/h");
        hum.setText(WeatherHelper.getHumidity().toString() + "%");
        press.setText(WeatherHelper.getPressure().toString() + " hPa");
        sunr.setText(WeatherHelper.convertTime(WeatherHelper.getSunrise()) + " Uhr");
        suns.setText(WeatherHelper.convertTime(WeatherHelper.getSunset()) + " Uhr");
        /**
         * Setting Sun/Cloud/... Image from converted weather id
         */
        todayStat.setImageResource(WeatherHelper.convertWeather(WeatherHelper.getWeatherId()));
        if(notification){
            sendNotification();
        }
    }

    public void sendNotification(){
        if(prefs.getBoolean("notifications", true)){
            /**
             * Setting up Notification
             */
            NotificationHelper.setCtxt(this);
            NotificationHelper.setTitl(String.format("%.1f", WeatherHelper.getTemperature_max()) + "° in " + city + ", " + CountryCode);
            NotificationHelper.setDesc(WeatherHelper.getDescription());
            NotificationHelper.setTicker("Wettervorhersage");
            NotificationHelper.setLaIc(WeatherHelper.convertWeather(WeatherHelper.getWeatherId()));
            NotificationHelper.setSmIc(R.mipmap.ic_launcher);
            NotificationHelper.fire();
        }
    }

    public void getCachedData(){
        if (prefs.getBoolean("cache", false)){
            /**
             * Getting cached Data
             */
            if (prefs.getString("cached", "") != ""){
                WeatherHelper.ParseData(prefs.getString("cached", ""));
                UpdateData(true);
            }
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
        // Handle tool bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        if(id == R.id.action_share){
            String sharebody = "Wetterbericht für " + city + ", " + CountryCode + ": " + WeatherHelper.getDescription() + " bei " + String.format("%.1f", WeatherHelper.getTemperature_max()) + "°";
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, sharebody);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(getBaseContext(), "lol", Toast.LENGTH_LONG).show();
            b = 0;
        } else {
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

}