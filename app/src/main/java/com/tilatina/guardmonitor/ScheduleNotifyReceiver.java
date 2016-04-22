package com.tilatina.guardmonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.tilatina.guardmonitor.Utilities.Preferences;

import java.util.Date;

/**
 * Created by jaime on 20/04/16.
 */
public class ScheduleNotifyReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Preferences.MYPREFERENCES, "Antes de llamar a notificationManager");
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(Preferences.MYPREFERENCES, "Despues de llamar a notificationManager");

        Intent notifyAction = new Intent(context, RollCallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyAction, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Notificacion pendiente")
                .setContentText("Tu proxima notificacion es " + new Date().toString())
                .setContentIntent(pendingIntent)
                ;
        notification.setVibrate(new long[] { 700, 1000, 1200});
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification.setSound(alarmSound);
        notification.setAutoCancel(true);

        notificationManager.notify(0, notification.getNotification());

        Log.d(Preferences.MYPREFERENCES, "Inside the receiver");
        context.startService(new Intent(context, ScheduleNotifierService.class));
    }
}
