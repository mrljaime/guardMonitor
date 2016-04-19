package com.tilatina.guardmonitor.Utilities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tilatina.guardmonitor.NoveltyThumbnail;
import com.tilatina.guardmonitor.R;

import java.util.List;

/**
 * Created by jaime on 19/04/16.
 */
public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {

    private List<NoveltyThumbnail> noveltyThumbnails;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;

        public ViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }

    public ThumbnailAdapter(List<NoveltyThumbnail> noveltyThumbnails) {
        this.noveltyThumbnails = noveltyThumbnails;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.novelty_thumbnail, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NoveltyThumbnail noveltyThumbnail = noveltyThumbnails.get(position);
        holder.thumbnail.setImageBitmap(noveltyThumbnail.getThumbnail());

    }

    @Override
    public int getItemCount() {
        return noveltyThumbnails.size();
    }


}
