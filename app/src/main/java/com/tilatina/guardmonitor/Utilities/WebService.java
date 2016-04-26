package com.tilatina.guardmonitor.Utilities;

import android.content.Context;
import android.graphics.LinearGradient;
import android.media.audiofx.PresetReverb;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by jaime on 19/04/16.
 */
public class WebService {
    private static String DEV_URL = "http://ws.tilatina.com/ws/guard";
    //private static String DEV_URL = "http://192.168.3.46:8090/ws/guard";

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

    public interface onFileUploadListener {
        int fileUploaded(String response);
    }

    public interface onErrorListener {
        int onError();
    }

    public interface sendNoveltyListener {
        void onSuccess(String response);
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


    /**
     * Uploads file to server in a separated thread. If file can not be uploaded signals a
     * communications error through the onErrorListener. NOTE: As this method runs under an
     * AsynTask, listeners are required to issue a new Runnable task in order to show UI
     * messages.
     * @param fileTitle File name that will be reported to the server.
     * @param path Whole file path within the mobile equipment.
     * @param headers Headers to be sent along the request.
     * @param requestParams Request params.
     * @param onFileUploadedListener Listener to be called upon upload success.
     * @param onErrorListener Listener that signals upload failure.
     */
    public static void uploadFile(final String fileTitle, final String path,
                           final Map<String, String> headers,
                           final Map<String, String> requestParams,
                           final onFileUploadListener onFileUploadedListener,
                           final onErrorListener onErrorListener, final Context context) {

        Log.d("FileUploader", String.format("path='%s'", path));

        final File sourceFile = new File(path);
        List<String> paramList = new ArrayList<String>();
        for (Map.Entry<String, String> param : requestParams.entrySet()) {
            paramList.add(param.getKey() + "=" + param.getValue());
        }
        final String queryString = TextUtils.join("&", paramList);

        if (!sourceFile.isFile()) {
            Log.e("FileUploader", String.format("Source File '%s' does not exist", path));
            onErrorListener.onError();
        } else {
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    try {
                        Log.d(Preferences.MYPREFERENCES, "Intentando mandar.... antes de conexión");
                        HttpURLConnection conn = null;
                        DataOutputStream dos = null;
                        String lineEnd = "\r\n";
                        String twoHyphens = "--";
                        String boundary = "*****";
                        int bytesRead, bytesAvailable, bufferSize;
                        byte[] buffer;
                        int maxBufferSize = 1 * 1024 * 1024;


                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
                        URL url = new URL(String.format("%s/%s/noveltyMonitor?%s",
                                DEV_URL, Preferences.getPreference(
                                        context
                                            .getSharedPreferences(Preferences.MYPREFERENCES, Context.MODE_PRIVATE),
                                        Preferences.TOKEN, null), queryString));
                        Log.d("FileUploader", String.format("Uploading to %s", url.toString()));

                        // Open a HTTP  connection to  the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("file", fileTitle);
                        if (headers != null) {
                            for (Map.Entry<String, String> entry : headers.entrySet()) {
                                conn.setRequestProperty(entry.getKey(), entry.getValue());
                            }
                        }

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                                + fileTitle + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of  maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        }

                        // send multipart form data necesssary after file data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();


                        Log.d("FileUploader", "HTTP Response is : "
                                + serverResponseMessage + ": " + serverResponseCode);


                        //close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();
                        if (serverResponseCode == 200) {

                            //Para ver la respuesta
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String output;
                            while ((output = br.readLine()) != null) {
                                sb.append(output);
                            }
                            Log.d("SERVER RESPONSE ----- ", sb.toString());


                            onFileUploadedListener.fileUploaded(sb.toString());
                        } else {
                            Log.e("FileUploader", String.format("Response code %s (%s) from '%s'",
                                    serverResponseCode, serverResponseMessage, url.toString()));
                            onErrorListener.onError();
                        }

                    } catch (MalformedURLException ex) {
                        Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                        onErrorListener.onError();
                    } catch (Exception e) {
                        Log.e("Upload file to server", "error: " + e.getMessage(), e);
                        onErrorListener.onError();
                    }
                    return null;
                }

            }.execute();

        } // End else block
    }

    public static void sendNoveltyWithOutPicture(Context context, final Map<String, String> params,
                                                 final sendNoveltyListener sendNoveltyListener) {

        String url = String.format("%s/%s/noveltyWithOutMonitor", DEV_URL,
                Preferences.getPreference(context.getSharedPreferences(Preferences.MYPREFERENCES,
                        Context.MODE_PRIVATE), Preferences.TOKEN, null));
        Log.d(Preferences.MYPREFERENCES, String.format("URL = %s", url));

        StringRequest sendNovelty = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(Preferences.MYPREFERENCES, response);
                sendNoveltyListener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendNoveltyListener.onError("Error");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };


        sendNovelty.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(context).add(sendNovelty);

    }
}
