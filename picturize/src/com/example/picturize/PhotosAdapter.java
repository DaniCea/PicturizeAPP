package com.example.picturize;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class PhotosAdapter extends BaseAdapter {

    private Activity activity;
    private Context mContext;
    
    private static LayoutInflater inflater = null;
    ArrayList<getPhotos> arrayPhotos;

    ImageLoader imageLoader; 

    public PhotosAdapter(Activity a, ArrayList<getPhotos> arrPhotos, Context c) {

        activity = a;

        mContext = c;
        
        arrayPhotos = arrPhotos;
                 
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
    }

    public int getCount() {
        return arrayPhotos.size();
    }

    public Object getItem(int position) {
        return arrayPhotos.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
        
        if (arrayPhotos.get(position).getPhotoPicture() != null){
            imageLoader.displayImage(arrayPhotos.get(position).getPhotoPicture(), imageView);
        }
        
        return imageView;
    }

    static class ViewHolder {
        ImageView imgPhoto;

    }
}
