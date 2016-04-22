package com.tilatina.guardmonitor.Utilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.tilatina.guardmonitor.ScheduleNotifyReceiver;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jaime on 19/04/16.
 */
public class Preferences {
    public static String MYPREFERENCES = "GuardCheck";
    public static String TOKEN = "token";

    public static void putPreference(SharedPreferences sharedPreferences, String key, String value) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        edit.commit();
        Log.d("JAIME...", String.format("Se ha agregado una preferencia a %s", MYPREFERENCES));
    }

    public static String getPreference(SharedPreferences sharedPreferences, String key, String defaultPrefer) {
        return sharedPreferences.getString(key, defaultPrefer);
    }

    public static void deletePreference(SharedPreferences sharedPreferences, String key) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove(key);
        edit.commit();
        Log.d("JAIME...", String.format("Se ha eliminado una preferencia a %s", MYPREFERENCES));
    }

    public static void downSize(Context context, Uri uri, int maxWidth) throws IOException{

        InputStream is;
        /** Primero obtenemos datos de la imagen sin cargar en memoria **/
        try {
            is = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException fnfe) {
            throw new IOException(String.format("File %s not found", uri.toString()));
        }
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        Log.d(MYPREFERENCES, String.format("witdh = %s, height = %s, mime='%s'", dbo.outWidth,
                dbo.outHeight, dbo.outMimeType));
        is.close();


        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(uri);
        /**
         * Comparamos contra el ancho máximo permitido
         */
        if (dbo.outWidth > maxWidth) {
            float ratio = ((float) dbo.outWidth) / ((float) maxWidth);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            Log.d("ImageUtil", String.format("comprimientoImagen ratio=%s",
                    ratio));

            /**
             * Obtener muestreo combinando cada n bits, sin perder dimensiones:
             */
            options.inSampleSize = (int) ratio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            Log.d("ImageUtil", "Imagen sin comprimir");
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();


        /**
         * Preparar el archivo para sobre-escribirlo
         */
        OutputStream stream = new FileOutputStream(uri.getPath());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bsResized = byteArrayOutputStream.toByteArray();

        Log.d("ImageUtil", "Sobre escribiendo archivo...");
        stream.write(bsResized);
        stream.close();
        Log.d("ImageUtil", "Archivo sobreescrito");
    }

    public static void setAlarmReceiver(Context context) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date();
        try {
            String dateString = Preferences.getPreference(context.getSharedPreferences(Preferences.MYPREFERENCES, Context.MODE_PRIVATE), "date", null);
            if (null != dateString) {
                date = simpleDateFormat.parse(dateString);
                date.setHours(19);
                date.setMinutes(27);
            } else {
                if (date.getMinutes() < 30) {
                    date.setMinutes(30);
                } else {
                    date.setHours(date.getHours()+1);
                    date.setMinutes(0);
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        Log.d(Preferences.MYPREFERENCES, String.format("FECHA = %s", date.toString()));

        Log.d("BackgroundService",  "onStartCommand");
        AlarmManager alarmManager = (AlarmManager)context
                .getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiver = new Intent(context, ScheduleNotifyReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, alarmReceiver, 0);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, TIME.getTime().getTime() + 60000, sender);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000*60*2, sender);
        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), sender);
        Preferences.deletePreference(context.getSharedPreferences(Preferences.MYPREFERENCES, Context.MODE_PRIVATE), "date");
    }

}
