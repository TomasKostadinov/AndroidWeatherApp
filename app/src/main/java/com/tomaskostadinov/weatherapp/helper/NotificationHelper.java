package com.tomaskostadinov.weatherapp.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;

import com.tomaskostadinov.weatherapp.R;
import com.tomaskostadinov.weatherapp.activity.MainActivity;

/**
 * Created by Tomas on 26.05.2015.
 */
public class NotificationHelper {

    private String title, descrion, ticker;
    private Integer largeIcon, smallIcon;
    private Context context;

    public void setCtxt(Context inContext){
        this.context = inContext;
    }

    public void setTitl(String inTitle){
        this.title = inTitle;
    }

    public void setDesc(String inDesc){
        this.descrion = inDesc;
    }

    public void setTicker(String inTicker){
        this.ticker = inTicker;
    }

    public void setLaIc(Integer inLargeIcon){
        this.largeIcon = inLargeIcon;
    }

    public void setSmIc(Integer inSmallIcon){
        this.smallIcon = inSmallIcon;
    }

    public void fire(){
        Intent notificationIntent = new Intent(this.context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this.context,
                99, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(this.context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, this.largeIcon))
                .setTicker(this.ticker)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(this.title)
                .setContentText(this.descrion)
                // ???
                .setStyle(new Notification.BigTextStyle().bigText(this.descrion))
                .setVibrate(new long[]{100, 100, 100, 100})
                .setLights(Color.YELLOW, 1000, 1000);
        Notification n = builder.build();
        n.defaults = 0;
        n.ledARGB = Color.WHITE;
        nm.notify(99, n);
    }
/*
    public void Notificate(Context context, String Title, String Subtitle, String Ticker, Integer LargeIcon, Integer SmallIcon){
    Intent notificationIntent = new Intent(context, MainActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(context,
            99, notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);

    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    Resources res = context.getResources();
    Notification.Builder builder = new Notification.Builder(context);

    builder.setContentIntent(contentIntent)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setLargeIcon(BitmapFactory.decodeResource(res, LargeIcon))
        .setTicker(Ticker)
        .setWhen(System.currentTimeMillis())
        .setAutoCancel(true)
        .setContentTitle(Title)
        .setContentText(Subtitle)
        .setVibrate(new long[]{100, 100, 100, 100})
        .setLights(Color.YELLOW, 1000, 1000);
    Notification n = builder.build();
    n.defaults = 0;
    n.ledARGB = Color.WHITE;
    nm.notify(99, n);
    }*/
}
