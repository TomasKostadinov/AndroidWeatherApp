package com.tomaskostadinov.weatherapp.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.helper.WeatherHelper;

import org.apache.http.Header;

public class ScrollingActivity extends BaseActivity {

    TextView temp, loc, windspeed, press, hum, suns, sunr, desc, errorcode, tomorrow_desc, tomorrow_temp;
    ImageView todayStat, tomorrowStat;
    String city, CountryCode, language, unit;
    CardView card, card_tomorrow;

    public android.content.SharedPreferences prefs;

    public boolean retried = false, downloadSucessfull = false;
    Integer id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        setupToolbar();
        setToolbarBackIcon();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getInt("id");
            Toast.makeText(this, id.toString(), Toast.LENGTH_SHORT).show();
        }

        /**
         * Views
         */

        temp = (TextView)findViewById(R.id.t);
        loc = (TextView)findViewById(R.id.l);
        windspeed = (TextView)findViewById(R.id.windspeed);
        press = (TextView)findViewById(R.id.pressure);
        hum = (TextView)findViewById(R.id.humidity);
        desc = (TextView)findViewById(R.id.desc);
        todayStat = (ImageView) findViewById(R.id.stattoday);
        card = (CardView) findViewById(R.id.card_view);
        /**
         * Removing ugly overscroll effect
         * Setting ScrollView's & ErrorLayout's visibility to gone
         */

        /**
         * Starting
         */

        getWeatherData(true);
    }

    public void getWeatherData(Boolean notification){
        final Boolean not = notification;
        /**
         * Get settings
         */
        city = prefs.getString("location", "Berlin");
        CountryCode = prefs.getString("countrykey", "DE");
        unit = prefs.getString("unitcode", "metric");
        language = prefs.getString("lang", "de");

        /**
         * Start JSON data download
         */
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://api.openweathermap.org/data/2.5/forecast/daily?q=" + city + "," + CountryCode + "&units=" + unit + "&lang=" + language + "&cnt=3&APPID=" + ApiKey, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                /**
                 *  called when response HTTP status is "200 OK"
                 */
                String in = new String(response);
                if (in != "") {
                    WeatherHelper = new WeatherHelper();
                    WeatherHelper.ParseDataSingle(in);
                    Log.i("WeatherData", "WeatherData Parsed");
                    UpdateData(not);
                    Log.i("WeatherData", "WeatherData Updated");
                    downloadSucessfull = true;
                } else {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                /**
                 * Called when response HTTP status is "4XX" (eg. 401, 403, 404)
                 * Setting ScrollView's & LoadingLayout's visibility to gone -> displaying the ErrorLayout
                 */
                currloc.setText("No Internet Connection");
                Log.e("WeatherData", "Download FAILED");
                if (!retried) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getWeatherData(false);
                        }
                    }, 5000);

                    final View coordinatorLayoutView = findViewById(R.id.cl);
                    Snackbar
                            .make(coordinatorLayoutView, "Erneuter Versuch in 5s", Snackbar.LENGTH_LONG)
                            .setAction(R.string.app, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getWeatherData(false);
                                }
                            })
                            .show();
                    retried = true;
                }
            }

            @Override
            public void onRetry(int retryNo) {
                Toast.makeText(ScrollingActivity.this, "Neuversuch Nr. " + retryNo, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void UpdateData(Boolean notification){
        /**
         * Writing data to TextView
         */
        loc.setText(WeatherHelper.getCity());
        temp.setText(String.format("%.1f", WeatherHelper.getTemperature_max()) + "°");
        desc.setText(WeatherHelper.getDescription());
        windspeed.setText(WeatherHelper.getSpeed().toString() + " km/h");
        hum.setText(WeatherHelper.getHumidity().toString() + "%");
        press.setText(WeatherHelper.getPressure().toString() + " mBar");
        /**
         * Setting Sun/Cloud/... Image from converted weather id
         */
        card.setCardBackgroundColor(getResources().getColor(WeatherHelper.convertWeatherToColor(WeatherHelper.getWeatherId())));
        //mToolbar.setBackgroundColor(getResources().getColor(WeatherHelper.convertWeatherToColor(WeatherHelper.getWeatherId())));
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
}
