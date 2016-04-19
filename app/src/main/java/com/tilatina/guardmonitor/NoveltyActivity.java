package com.tilatina.guardmonitor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.tilatina.guardmonitor.Utilities.Preferences;
import com.tilatina.guardmonitor.Utilities.ThumbnailAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoveltyActivity extends AppCompatActivity {

    String mCurrentPhotoPath;
    List<NoveltyThumbnail> noveltyThumbnails = new ArrayList<>();
    ThumbnailAdapter thumbnailAdapter = new ThumbnailAdapter(noveltyThumbnails);
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novelty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button takePicture = (Button) findViewById(R.id.takePicture);
        recyclerView = (RecyclerView) findViewById(R.id.thumbnailRecycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(thumbnailAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new ItemListener() {
            @Override
            public void onClick(View view, final int position) {
                Button deleteItem = (Button) view.findViewById(R.id.deleteThumbnail);

                deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noveltyThumbnails.remove(position);
                        thumbnailAdapter.notifyDataSetChanged();
                    }
                });

            }
        }));

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        File picture = createImageFile();
                        Uri image = Uri.fromFile(picture);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, image);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        NoveltyActivity.ItemListener itemListener;
        public RecyclerTouchListener(Context context, final RecyclerView recyclerView,
                                     final NoveltyActivity.ItemListener itemListener) {
            this.itemListener = itemListener;
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (null != child && null != itemListener) {
                itemListener.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }
        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            Bitmap image = (Bitmap) extra.get("data");
            NoveltyThumbnail thumbnail = new NoveltyThumbnail(image);
            noveltyThumbnails.add(thumbnail);

            thumbnailAdapter.notifyDataSetChanged();
        }
    }

    private File createImageFile() throws IOException{

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private interface ItemListener {
        void onClick(View view, int position);
    }


}
