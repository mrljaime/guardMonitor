package com.tilatina.guardmonitor.Utilities;

import android.content.Context;
import android.media.audiofx.PresetReverb;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by jaime on 19/04/16.
 */
public class WebService {
    private static String DEV_URL = "http://192.168.3.46:8090/ws/guard";

    public interface LoginSuccessListener {
        void onSuccess(String response);
    }

    public interface LoginErroListener {
        void onError(String error);
    }

    public interface RollCallListener {
        void onSuccess(String response);
    }

    public interface RollCallErrorListener {
        void onError(String error);
    }

    public static void loginAction(Context context, final String phone, final LoginSuccessListener loginSuccessListener,
                                   final LoginErroListener loginErroListener) {

        String url = String.format("%s/loginMonitor", DEV_URL);

        StringRequest loginAction = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loginSuccessListener.onSuccess(response);
                Log.d(Preferences.MYPREFERENCES, String.format("Responde = %s", response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d(Preferences.MYPREFERENCES, String.format("Error = %s", error));
                loginErroListener.onError("Falló algo");
            }
        }){
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("phoneToken", phone);

                return params;
            }
        };


        loginAction.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(context).add(loginAction);
    }


    public static void rollCallAction(Context context, String service, final JSONArray rollCall,
                                      final RollCallListener rollCallListener,
                                      final RollCallErrorListener rollCallErrorListener) {

        String url = String.format("%s/%s/rollCallMonitor", DEV_URL, service);

        StringRequest rollCallRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                rollCallListener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                rollCallErrorListener.onError("Fallo");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("rollCall", rollCall.toString());

                return params;
            }
        };

        rollCallRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(context).add(rollCallRequest);
    }



}
