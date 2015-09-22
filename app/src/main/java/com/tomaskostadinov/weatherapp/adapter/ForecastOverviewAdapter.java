package com.tomaskostadinov.weatherapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.helper.WeatherHelper;
import com.tomaskostadinov.weatherapp.model.Day;
import com.tomaskostadinov.weatherapp.model.Place;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Tomas on 02.08.2015.
 */
// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ForecastOverviewAdapter extends RecyclerView.Adapter<ForecastOverviewAdapter.ViewHolder> implements View.OnClickListener {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvDayName;
        public TextView tvDescription;
        public TextView tvTemperature;
        public TextView tvMaxTemp, tvMinTemp;
        public ImageView ivWeatheric;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            super(itemView);
            this.tvDayName = (TextView) itemView.findViewById(R.id.tvName);
            this.tvDescription = (TextView) itemView.findViewById(R.id.tvHome);
            this.tvTemperature = (TextView) itemView.findViewById(R.id.genre);
            this.ivWeatheric = (ImageView) itemView.findViewById(R.id.Stat);
        }
    }

    private ArrayList<Day> users;
    // Store the context for later use
    private Context context;

    // Pass in the context and users array into the constructor
    public ForecastOverviewAdapter(Context context, ArrayList<Day> users) {
        this.users = users;
        this.context = context;
    }

    @Override
    public ForecastOverviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the custom layout
        final View itemView = LayoutInflater.from(context).inflate(R.layout.daycard, parent, false);
        //itemView.setOnClickListener(mCLick());
        // Return a new holder instance
        return new ForecastOverviewAdapter.ViewHolder(itemView);
    }

    // Involves populating data into the item through holder

    public WeatherHelper weatherHelper;
    @Override
    public void onBindViewHolder(ForecastOverviewAdapter.ViewHolder holder, int position) {
        weatherHelper = new WeatherHelper();
        // Get the data model based on position
        Day songs = users.get(position);
        // Set item views based on the data model
        Integer timestamp = songs.time;
        Date time = new Date((long)timestamp*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd.");
        holder.tvDayName.setText(sdf.format(time));
        holder.tvDescription.setText(songs.description);
        String temp =  String.format("%.1f", songs.max_temp);
        holder.tvTemperature.setText( temp + "°");
        holder.ivWeatheric.setImageResource(weatherHelper.convertWeather(songs.weatherid));
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public void onClick(View v){
        Log.i("onClick", "on View: " + v.toString());
    }
}

