package com.tomaskostadinov.weatherapp.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
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
import com.tomaskostadinov.weatherapp.search.SearchSugestionProvider;

import org.apache.http.Header;
import org.w3c.dom.Text;

import android.os.Handler;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity{

    private Toolbar mToolbar;
    TextView temp, loc, windspeed, press, hum, suns, sunr, desc, currloc, errorcode;
    ImageView todayStat;
    ScrollView sv;
    LinearLayout ErrorLayout;
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
    public CheckBox dontShowAgain;
    public static final String PREFS_NAME = "updates";
    public  boolean retried = false, downloadSucessfull = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Intent i = getIntent();

        if(Intent.ACTION_SEARCH.equals(i.getAction())){
            String query = i.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSugestionProvider.AUTHORITY, SearchSugestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("location", query);
            editor.commit();
            Toast.makeText(this, city, Toast.LENGTH_SHORT).show();
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
        errorcode = (TextView)findViewById(R.id.errorcode);
        todayStat = (ImageView) findViewById(R.id.stattoday);

        /**
         * Removing that ugly overscroll effect
         * Setting ScrollView's & ErrorLayout's visibility to gone
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
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryLight);
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
                retried = false;
            }
        });

        navview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.home:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.place:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), PlaceActivity.class));
                            }
                        }, 250);
                        return true;
                    case R.id.help:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                Uri data = Uri.parse("mailto:tomas.kostadinov@gmx.de?subject=I need help with your weather app!");
                                i.setData(data);
                                startActivity(i);
                            }
                        }, 250);
                        return true;
                    case R.id.about:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                            }
                        }, 250);
                        return true;
                    case R.id.settings:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                            }
                        }, 250);
                        return true;
                    case R.id.beta:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), BetaSettingsActivity.class));
                            }
                        }, 250);
                        return true;
                    case R.id.tomorrow:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), ForecastActivity.class));
                            }
                        }, 250);
                        return true;
                }
                return true;
            }
        });

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if(!settings.getBoolean("updatenews2", false)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Neu in " + getResources().getString(R.string.versionDesc));
            builder.setMessage(Html.fromHtml(
            "- Suchen Sie direkt eine Stadt aus dem Startbildschirm heraus<br/>- Sprachsuche<br/>- Vorbereitung auf Wettervorhersage-Funktion <br/>- Design Updates (Vorbereitung auf wählbare Farben)<br/>- Verbesserungen, Bugfixes<br/><br/><b>BITTE ALLE BUGS + FEHLER MELDEN!<br/> FEATURE REQUESTS ERWÜNSCHT!</b>"));
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("updatenews2", true);
                    // Commit the edits!
                    editor.apply();
                }
            });
            builder.show();
        }
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
        city = prefs.getString("location", "Berlin");
        CountryCode = prefs.getString("countrykey", "DE");
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
                // called when response HTTP status is "200 OK"
                String in = new String(response);
                if (in != "") {
                    WeatherHelper.ParseData(in);
                    UpdateData(not);
                    sv.setVisibility(View.VISIBLE);
                    downloadSucessfull = true;
                } else {
                    ErrorLayout.setVisibility(View.VISIBLE);
                    sv.setVisibility(View.GONE);
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
                ErrorLayout.setVisibility(View.VISIBLE);
                currloc.setText("No Internet Connection");
                mSwipeRefreshLayout.setRefreshing(false);
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
                // called when request is retried
                Toast.makeText(MainActivity.this, "Neuversuch Nr. " + retryNo, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void UpdateData(Boolean notification){
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
        Integer t = WeatherHelper.getSunrise();
        sunr.setText(WeatherHelper.convertTime(t) + " Uhr");
        //suns.setText(WeatherHelper.convertTime(WeatherHelper.getSunset().toString()) + " Uhr");
        /**
         * Setting Sun/Cloud/... Image from converted weather id
         */
        todayStat.setImageResource(WeatherHelper.convertWeather(WeatherHelper.getWeatherId()));
        if(notification){
            sendNotification();
        }
        if(mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.setRefreshing(false);
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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);

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
            if (downloadSucessfull){
                String sharebody = "Wetterbericht für " + city + ", " + CountryCode + ": " + WeatherHelper.getDescription() + " bei " + String.format("%.1f", WeatherHelper.getTemperature_max()) + "°";
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, sharebody);
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
                return true;
            } else{
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
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