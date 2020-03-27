package com.alinabaranova.youtubedemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Bitmap> images;
    private ArrayList<String> artistNames;
    private ArrayList<String> songNames;

    public ListAdapter(Context context, ArrayList<Bitmap> images, ArrayList<String> artistNames, ArrayList<String> songNames) {
        this.context = context;
        this.images = images;
        this.artistNames = artistNames;
        this.songNames = songNames;
    }

    @Override
    public int getCount() {
        return songNames.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.single_list_item, parent, false);
            viewHolder.image = convertView.findViewById(R.id.albumCover);
            viewHolder.artistName = convertView.findViewById(R.id.artistName);
            viewHolder.songName = convertView.findViewById(R.id.songName);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        viewHolder.image.setImageBitmap(images.get(position));
        viewHolder.artistName.setText(artistNames.get(position));
        viewHolder.songName.setText(songNames.get(position));

        return convertView;

    }

    private static class ViewHolder {

        ImageView image;
        TextView artistName;
        TextView songName;

    }
}
