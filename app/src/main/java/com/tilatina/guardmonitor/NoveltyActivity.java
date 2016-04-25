package com.tilatina.guardmonitor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.tilatina.guardmonitor.Utilities.Preferences;
import com.tilatina.guardmonitor.Utilities.ThumbnailAdapter;
import com.tilatina.guardmonitor.Utilities.WebService;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
        thumbnailAdapter = new ThumbnailAdapter(this, noveltyThumbnails);


        Button send = (Button) findViewById(R.id.sendNovelty);
        final EditText description = (EditText) findViewById(R.id.noveltyText);
        takePicture = (FloatingActionButton) findViewById(R.id.takePicture);
        ListView listView = (ListView) findViewById(R.id.listViewThumbnail);

        listView.setAdapter(thumbnailAdapter);

        assert listView != null;
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


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

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkifTextIsNull(description.getText().toString())) {
                    dateString = getUTCDateTime();
                    final ProgressDialog progressDialog = new ProgressDialog(NoveltyActivity.this);
                    progressDialog.setMessage("Enviando");
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("description", URLEncoder.encode(description.getText().toString()));
                    params.put("captureDate", URLEncoder.encode(dateString));
                    params.put("colorStatus", URLEncoder.encode("G"));
                    if (noveltyThumbnails.size() != 0) {
                        progressDialog.show();
                        for (int i = 0; i < noveltyThumbnails.size(); i++) {

                            Bitmap image = noveltyThumbnails.get(i).getThumbnail();
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            WebService.uploadFile("uploadFile", noveltyThumbnails.get(i).getPath(),
                                    null, params, new WebService.onFileUploadListener() {
                                @Override
                                public void fileUploaded(String response) {
                                    Log.d(Preferences.MYPREFERENCES, "Responde :" + response);
                                    try {
                                        JSONObject respondeObj = new JSONObject(response);
                                        if (200 == respondeObj.getInt("code")) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    finish();
                                                    Toast.makeText(NoveltyActivity.this, "Enviado con éxito", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new WebService.onErrorListener() {
                                @Override
                                public void onError() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.hide();
                                            Toast.makeText(NoveltyActivity.this, "Error de comunicaciones", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }, getApplicationContext());
                        }
                    } else {
                        Toast.makeText(NoveltyActivity.this, "No puedes enviar novedad sin tomar fotografa",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
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
}
