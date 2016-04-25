package com.tilatina.guardmonitor.Utilities;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.tilatina.guardmonitor.NoveltyThumbnail;
import com.tilatina.guardmonitor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaime on 19/04/16.
 */
public class ThumbnailAdapter extends BaseAdapter implements ListAdapter{
    private ArrayList<NoveltyThumbnail> noveltyThumbnails;
    private Context context;

    public ThumbnailAdapter(){
    }

    public ThumbnailAdapter(Context context, ArrayList<NoveltyThumbnail> noveltyThumbnails) {
        this.context = context;
        this.noveltyThumbnails = noveltyThumbnails;
    }



    @Override
    public int getCount() {
        return noveltyThumbnails.size();
    }

    @Override
    public Object getItem(int position) {
        return noveltyThumbnails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (null == row) {
            LayoutInflater layoutInflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.novelty_thumbnail, null);
            Button deleteThumbnail = (Button) row.findViewById(R.id.deleteThumbnail);
            ImageView imageView = (ImageView) row.findViewById(R.id.thumbnail);

            final FloatingActionButton takePicture = (FloatingActionButton)
                    ((Activity)context).findViewById(R.id.takePicture);
            imageView.setImageBitmap(noveltyThumbnails.get(position).getThumbnail());

            deleteThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noveltyThumbnails.remove(position);
                    notifyDataSetChanged();
                    takePicture.setEnabled(true);
                }
            });
        }
        return row;
    }
}
