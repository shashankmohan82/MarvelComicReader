package com.marvel.comic.comicreader.presenter;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.karumi.marvelapiclient.CharacterApiClient;
import com.karumi.marvelapiclient.MarvelApiConfig;
import com.karumi.marvelapiclient.model.CharacterDto;
import com.karumi.marvelapiclient.model.CharactersDto;
import com.karumi.marvelapiclient.model.MarvelImage;
import com.karumi.marvelapiclient.model.MarvelResponse;
import com.marvel.comic.comicreader.R;
import com.marvel.comic.comicreader.model.SuperHero;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Realm realm ;
    private RecyclerView rView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        setContentView(R.layout.activity_main);
        GridLayoutManager lLayout = new GridLayoutManager(getApplicationContext(), 2);
        rView = (RecyclerView)findViewById(R.id.my_recycler_view);
        rView.setLayoutManager(lLayout);


        try {

            OrderedRealmCollection<SuperHero> data = realm.where(SuperHero.class).findAll();
            if(data.size() > 0) {

                MyRecyclerViewAdapter rcAdapter = new MyRecyclerViewAdapter(MainActivity.this, data,
                        new MyRecyclerViewAdapter.OnItemClickListener() {

                            /*Activity Transition to ComicList Activity*/
                            @Override
                            public void onItemClick(int position, final SuperHero data) {

                                String passName = data.getName();
                                String passValue = data.getId();
                                Intent intent = new Intent(MainActivity.this, ComicListActivity.class);
                                intent.putExtra("passValue", passValue);
                                intent.putExtra("passName", passName);
                                startActivity(intent);


                            }
                        });
                rView.setAdapter(rcAdapter);
            }
            else if(isNetworkConnected()){

                new DownloadFilesTask().execute();
            }
            else {
                createAlertForNoInternet();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }



    }

    private void createAlertForNoInternet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("NO INTERNET CONNECTION")
                .setMessage("Please turn on data and reopen.")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                // Sent user to GPS settings screen

                                    finish();

                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }


    /*fetching content from Marvel Api for Superhero list*/

    private class DownloadFilesTask extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(MainActivity.this, "Initialising, please wait for a moment.","",false, false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIcon(R.mipmap.ic_launcher);

        }


        protected String doInBackground(Void... voids) {

            try(Realm realm = Realm.getDefaultInstance()) {
                MarvelApiConfig marvelApiConfig = new MarvelApiConfig
                        .Builder("e292dee736108a2289dd259a751922cb",
                        "6f9c2292cb81e8b946e50d2b738557d85d8f7886")
                        .debug()
                        .build();
                CharacterApiClient characterApiClient = new CharacterApiClient(marvelApiConfig);
                MarvelResponse<CharactersDto> air = characterApiClient.getAll(0,24);
                List<CharacterDto> characters = air.getResponse().getCharacters();

                final List<SuperHero> helperList = new ArrayList<>();
                for(CharacterDto character : characters) {
                    if(!character.getThumbnail().getPath().contains("image_not_available")) {


                        saveImage(getApplicationContext(),
                                new DownloadImageTask().doInBackground(character.getThumbnail().
                                        getImageUrl(MarvelImage.Size.DETAIL)), character.getName(), "jpg");


                        SuperHero superHero = new SuperHero(character.getId(), character.getName());
                        helperList.add(superHero);
                    }
                }

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(helperList);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        /*setting up screen after fetching data from Marvel Api*/

         protected void onPostExecute(String result) {
            // Put these items in the Adapter

             progressDialog.dismiss();
            try {
                OrderedRealmCollection<SuperHero> data = realm.where(SuperHero.class).findAll();
                MyRecyclerViewAdapter rcAdapter = new MyRecyclerViewAdapter(MainActivity.this, data,
                        new MyRecyclerViewAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position, final SuperHero data) {

                                String passName = data.getName();
                                String passValue = data.getId();
                                Intent intent = new Intent(MainActivity.this, ComicListActivity.class);
                                intent.putExtra("passValue", passValue);
                                intent.putExtra("passName",passName);
                                startActivity(intent);
                            }
                        });
                rView.setAdapter(rcAdapter);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public Bitmap getImageBitmap(Context context, String name, String extension){
        name=name+"."+extension;
        try{
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }

    public void saveImage(Context context, Bitmap b,String name,String extension){
        name=name+"."+extension;
        FileOutputStream out;
        try {
            out = context.openFileOutput(name, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
