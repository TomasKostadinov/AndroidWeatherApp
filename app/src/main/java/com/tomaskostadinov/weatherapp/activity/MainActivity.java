package com.tomaskostadinov.weatherapp.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.helper.NotificationHelper;
import com.tomaskostadinov.weatherapp.helper.WeatherHelper;

import org.apache.http.Header;

import android.os.Handler;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity{


    private Toolbar mToolbar;
    TextView temp, loc, windspeed, press, hum, suns, sunr, desc;
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

    public Drawer result;
    public Bundle extras;

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
        mToolbar.setSubtitle(city);
        //mToolbar.setLogo(R.mipmap.ic_launcher);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);

        startActivity(new Intent(getApplicationContext(), ForecastActivity.class));
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

        getWeatherData();

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

        /**
         * Setting up navigation drawer header
         * TODO FIRST, SECOND & THIRD CITY SUPPORT
         */

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withProfileImagesVisible(false)
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withEmail(prefs.getString("location", "Berlin")).withIcon(getResources().getDrawable(R.drawable.ic_cloud))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        //Now create your drawer and pass the AccountHeader.Result
        result = new DrawerBuilder()
                .withTranslucentStatusBar(true)

                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Wettervorhersage").withIcon(getResources().getDrawable(R.drawable.ic_sunny)),
                        new PrimaryDrawerItem().withName(R.string.places).withIcon(getResources().getDrawable(R.drawable.ic_place)),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.nav_item_help_and_support).withIcon(getResources().getDrawable(R.drawable.ic_help)),
                        new SecondaryDrawerItem().withName(R.string.about).withIcon(getResources().getDrawable(R.mipmap.ic_launcher)),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.title_settings).withIcon(getResources().getDrawable(R.drawable.ic_settings))
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        switch (position) {
                            case 0:
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                return true;
                            case 1:
                                startActivity(new Intent(getApplicationContext(), PlaceActivity.class));
                                result.setSelection(0);
                                return true;
                            case 3:
                                result.setSelection(0);
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                Uri data = Uri.parse("mailto:tomas.kostadinov@gmx.de?subject=Android Weather App");
                                i.setData(data);
                                startActivity(i);
                                return true;
                            case 4:
                                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                                result.setSelection(0);
                                return true;
                            case 5:
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                                result.setSelection(0);
                                return true;
                            case 6:
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                                result.setSelection(0);
                                return true;
                            default:
                                result.closeDrawer();
                                return true;
                        }
                    }
                }).build();
    }

    @Override
    public void onResume() {
        super.onResume();
        getWeatherData();
    }

    public void getWeatherData(){
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
        mToolbar.setSubtitle(city);
        /**
         * Start JSON data download
         */
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://api.openweathermap.org/data/2.5/weather?q=" + city + "," + CountryCode + "&units=" + unit +"&lang=" + language, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                /**
                 * Only for testing
                 */
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                String in = new String(response);
                if(in != ""){
                    WeatherHelper.ParseData(in);
                    UpdateData();
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

    public void UpdateData(){
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
        sendNotification();
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
                UpdateData();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_refresh){
            getWeatherData();
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
        if(result.isDrawerOpen()){
            result.closeDrawer();
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