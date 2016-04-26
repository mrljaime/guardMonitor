package com.tilatina.guardmonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.tilatina.guardmonitor.Utilities.Preferences;
import com.tilatina.guardmonitor.Utilities.ThumbnailAdapter;
import com.tilatina.guardmonitor.Utilities.WebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class NoveltyActivity extends AppCompatActivity {

    Uri mCurrentPhotoPath;
    ArrayList<NoveltyThumbnail> noveltyThumbnails = new ArrayList<>();
    ThumbnailAdapter thumbnailAdapter;
    FloatingActionButton takePicture;
    String dateString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novelty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /**
         * General context of activity
         */
        final Context me = NoveltyActivity.this;

        /**
         * Resources for actions
         */
        Button send = (Button) findViewById(R.id.sendNovelty);
        final EditText description = (EditText) findViewById(R.id.noveltyText);
        takePicture = (FloatingActionButton) findViewById(R.id.takePicture);
        ListView listView = (ListView) findViewById(R.id.listViewThumbnail);
        final Spinner stateColor = (Spinner) findViewById(R.id.state_color);

        /**
         * Adapter for state color spinner
         */
        String [] stateColors = {"Verde", "Amarillo", "Rojo"};
        ArrayAdapter<String> stateColorAdapter = new ArrayAdapter<String>(me,
                R.layout.support_simple_spinner_dropdown_item, stateColors);
        stateColor.setAdapter(stateColorAdapter);


        /**
         * Initialize adapter for thumbnails and set to the listView
         */
        thumbnailAdapter = new ThumbnailAdapter(me, noveltyThumbnails);
        listView.setAdapter(thumbnailAdapter);


        /**
         * For handle scroll inside ScrollView
         */
        assert listView != null;
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


        /**
         * ClickListener of takePicture button. Make a intent to camera and wait for result
         */
        assert takePicture != null;
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                dateString = getUTCDateTime();

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File picture = null;
                    picture = new File(Environment.
                            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dateString + ".jpg");
                    mCurrentPhotoPath = Uri.fromFile(picture);
                    Log.d(Preferences.MYPREFERENCES, "Get camera intent");
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture));
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });

        /**
         * ClickListener for send button. Make a request to server for send information and
         * picture of a novelty
         */
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkifTextIsNull(description.getText().toString())) {

                    dateString = getUTCDateTime();
                    final ProgressDialog progressDialog = new ProgressDialog(NoveltyActivity.this);
                    progressDialog.setMessage("Enviando");

                    String stateColorValue = stateColor.getSelectedItem().toString();
                    Log.d(Preferences.MYPREFERENCES, String.format("COLOR = %s", stateColorValue));
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("description", URLEncoder.encode(description.getText().toString()));
                    params.put("captureDate", URLEncoder.encode(dateString));
                    params.put("colorStatus", transfornStateColorValue(stateColorValue));

                    progressDialog.show();
                    sendNoveltyToServer(me, params, noveltyThumbnails, new onSuccessResultSend() {
                        @Override
                        public void returnValue(int value) {

                            /**
                             * Success
                             */
                            if (200 == value) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.hide();
                                        finish();
                                        Toast.makeText(me, "Novedad enviada", Toast.LENGTH_SHORT).show();

                                        return;
                                    }
                                });
                            }

                            /**
                             * Service not found
                             */
                            if (320 == value) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.hide();
                                        Intent intent = new Intent();
                                        intent.setClass(me, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                        Toast.makeText(me, "El servicio no fue encontrado, contacta a tu supervisor",
                                                Toast.LENGTH_SHORT).show();

                                        return;
                                    }
                                });

                            }

                            /**
                             * Error in the request
                             */
                            if (500 == value) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.hide();
                                        Toast.makeText(me, "Error de comunicaciones",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });


                } else {
                    description.setError("El campo no puede estar vacío");
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = BitmapFactory.decodeFile(mCurrentPhotoPath.getPath());
            NoveltyThumbnail noveltyThumbnail = new NoveltyThumbnail(image, mCurrentPhotoPath.getPath());
            noveltyThumbnails.add(noveltyThumbnail);
            thumbnailAdapter.notifyDataSetChanged();
            try {
                Preferences.downSize(this, mCurrentPhotoPath, 250);
            }catch(IOException e) {
                e.printStackTrace();
            }

            takePicture.setEnabled(false);

        }
    }

    private boolean checkifTextIsNull(String text) {
        if (text.trim().length() == 0) {
            return false;
        }
        return true;
    }

    private String getUTCDateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(new Date());
    }

    private int sendNoveltyToServer(final Context context, Map<String, String> params,
                                        ArrayList<NoveltyThumbnail> noveltyThumbnails,
                                    final NoveltyActivity.onSuccessResultSend listener) {
        /**
         * Check if count of noveltyThumbnails is larger than 1, if is true, make a request with picture,
         * otherwise, make a request only with novelty text and color state of the event.
         * We consider than noveltyThumbnails is limit to 1 item
         */
        if (0 != noveltyThumbnails.size()) {
            Bitmap image = noveltyThumbnails.get(0).getThumbnail();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 70, bytes);

            /**
             * Make a call to webService class for make the request.
             */
            WebService.uploadFile("uploadFile", noveltyThumbnails.get(0).getPath(), null, params,
                    new WebService.onFileUploadListener() {
                        @Override
                        public int fileUploaded(String response) {
                            Log.d(Preferences.MYPREFERENCES, String.format("RESPONSE = %s", response));
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                if (200 == jsonResponse.getInt("code")) {
                                    /**
                                     * Return 200 for success
                                     */
                                    listener.returnValue(200);
                                    return 200;
                                } else {
                                    /**
                                     * Return 302 because the service was not found in server
                                     */
                                    listener.returnValue(302);
                                    return 302;
                                }
                            }catch (JSONException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        }
                    }, new WebService.onErrorListener() {
                        @Override
                        public int onError() {
                            /**
                             * Return 500 for error
                             */
                            listener.returnValue(500);
                            return 500;
                        }
                    }, context);
        } else {
            WebService.sendNoveltyWithOutPicture(context, params, new WebService.sendNoveltyListener() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (200 == jsonResponse.getInt("code")) {
                            listener.returnValue(200);
                        } else {
                            listener.returnValue(302);
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String error) {
                    listener.returnValue(500);
                }
            });
        }

        return 0;
    }

    private interface onSuccessResultSend{
        void returnValue(int value);
    }

    private String transfornStateColorValue(String color) {
        if ("Amarillo".equals(color)) {
            return "Y";
        }

        if ("Rojo".equals(color)) {
            return "R";
        }

        if ("Verde".equals(color)) {
            return "G";
        }

        return "";
    }
}
