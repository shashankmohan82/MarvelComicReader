package com.marvel.comic.comicreader.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marvel.comic.comicreader.R;
import com.marvel.comic.comicreader.model.Comic;

import java.io.FileInputStream;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by shashank on 8/4/2016.
 */
public class ComicListAdapter extends
        RealmRecyclerViewAdapter<Comic, ComicListAdapter.MyViewHolder> {


    private Context context;
    private OnItemClickListener onItemClickHandler;

    public ComicListAdapter(@Nullable Context context, OrderedRealmCollection<Comic> data,
                                 OnItemClickListener onItemClickHandler) {
        super(context, data, true);
        this.context = context;
        this.onItemClickHandler = onItemClickHandler;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comiclist_recycler,
                parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.title.setText(getItem(position).getName());
        holder.image.setImageBitmap(getImageBitmap(context, getItem(position).getId(), "jpg"));
    }

    @Nullable
    @Override
    public Comic getItem(int index) {
        return super.getItem(index);
    }

    public Bitmap getImageBitmap(Context context, String name, String extension) {
        name = name + "." + extension;
        try (FileInputStream fis = context.openFileInput(name)) {
            Bitmap b = BitmapFactory.decodeStream(fis);
            return b;
        } catch (Exception e) {
        }
        return null;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private ImageView image;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.comic_name);
            image = (ImageView) view.findViewById(R.id.comic_image);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Comic data = getItem(adapterPosition);
            onItemClickHandler.onItemClick(adapterPosition, data);
        }
    }

    public static interface OnItemClickListener {
        public void onItemClick(int position, Comic data);
    }

}