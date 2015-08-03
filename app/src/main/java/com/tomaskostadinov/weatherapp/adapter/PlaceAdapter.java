package com.tomaskostadinov.weatherapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.model.Place;

import java.util.ArrayList;

/**
 * Created by Tomas on 02.08.2015.
 */
// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvName;
        public TextView tvHometown;
        public TextView tvTime;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            super(itemView);
            this.tvName = (TextView) itemView.findViewById(R.id.tvName);
            this.tvHometown = (TextView) itemView.findViewById(R.id.tvHome);
            this.tvTime = (TextView) itemView.findViewById(R.id.genre);
        }
    }

    private ArrayList<Place> users;
    // Store the context for later use
    private Context context;

    // Pass in the context and users array into the constructor
    public PlaceAdapter(Context context, ArrayList<Place> users) {
        this.users = users;
        this.context = context;
    }

    @Override
    public PlaceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the custom layout
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.place, parent, false);
        // Return a new holder instance
        return new PlaceAdapter.ViewHolder(itemView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(PlaceAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Place songs = users.get(position);
        // Set item views based on the data model
        holder.tvName.setText(songs.name);
        holder.tvHometown.setText(songs.author);
        String result = DateUtils.getRelativeTimeSpanString(songs.timerequest, System.currentTimeMillis(), 0).toString();
        holder.tvTime.setText(result);
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return users.size();
    }
}

