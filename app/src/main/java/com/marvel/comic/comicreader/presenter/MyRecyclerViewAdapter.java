package com.marvel.comic.comicreader.presenter;

/**
 * Created by shashank on 8/6/2016.
 */
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
import com.marvel.comic.comicreader.model.SuperHero;

import java.io.FileInputStream;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;


public class MyRecyclerViewAdapter extends
        RealmRecyclerViewAdapter<SuperHero, MyRecyclerViewAdapter.MyViewHolder>
{


    private Context context;
    private OnItemClickListener onItemClickHandler;

    public MyRecyclerViewAdapter(@Nullable Context context, OrderedRealmCollection<SuperHero> data,
                                 OnItemClickListener onItemClickHandler) {
        super(context, data, true);
        this.context = context;
        this.onItemClickHandler = onItemClickHandler;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_main_recycler,
                parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String name = getItem(position).getName();
        holder.title.setText(name);
        holder.image.setImageBitmap(getImageBitmap(context, name, "jpg" ));
    }

    @Nullable
    @Override
    public SuperHero getItem(int index) {
        return super.getItem(index);
    }

    public Bitmap getImageBitmap(Context context, String name, String extension){
        name=name+"."+extension;
        try(FileInputStream fis = context.openFileInput(name)){
            Bitmap b = BitmapFactory.decodeStream(fis);
            return b;
        }
        catch(Exception e){
        }
        return null;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private ImageView image;
        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.country_name);
            image = (ImageView) view.findViewById(R.id.country_photo);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            SuperHero data = getItem(adapterPosition);
            onItemClickHandler.onItemClick(adapterPosition, data);
        }
    }

    public static interface OnItemClickListener {
        public void onItemClick(int position, SuperHero data);
    }

}