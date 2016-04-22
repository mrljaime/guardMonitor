package com.tilatina.guardmonitor;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.widget.TimePicker;

import com.tilatina.guardmonitor.Utilities.Preferences;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jaime on 20/04/16.
 */
public class ScheduleNotifierService extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*
        Date date = new Date();
        Log.d(Preferences.MYPREFERENCES, String.format("Minutos = %s", date.getMinutes()));

        if (date.getMinutes() > 0) {
            date.setMinutes((date.getMinutes() + 1));
            Log.d(Preferences.MYPREFERENCES, String.format("Minutos = %s", date.getMinutes()));
        }


        Log.d("BackgroundService", "onStartCommand");
        AlarmManager alarmManager = (AlarmManager)this
                .getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiver = new Intent(this, ScheduleNotifyReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmReceiver, 0);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, TIME.getTime().getTime() + 60000, sender);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000*60*2, sender);
        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), sender);
        */
        Preferences.setAlarmReceiver(this);
        return START_STICKY;
    }
}