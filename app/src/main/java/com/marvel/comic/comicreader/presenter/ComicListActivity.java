package com.marvel.comic.comicreader.presenter;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.karumi.marvelapiclient.ComicApiClient;
import com.karumi.marvelapiclient.MarvelApiConfig;
import com.karumi.marvelapiclient.model.ComicDto;
import com.karumi.marvelapiclient.model.ComicsDto;
import com.karumi.marvelapiclient.model.ComicsQuery;
import com.karumi.marvelapiclient.model.MarvelImage;
import com.karumi.marvelapiclient.model.MarvelResponse;
import com.marvel.comic.comicreader.R;
import com.marvel.comic.comicreader.model.Comic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;

public class ComicListActivity extends AppCompatActivity {



    private RecyclerView rListView;
    private String id;
    private Realm realm;
    private String superHeroName;
    private LinearLayoutManager lLayout;
    private ProgressDialog progressDialog;
    private int pageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_list);
        lLayout = new LinearLayoutManager(this);
        rListView = (RecyclerView)findViewById(R.id.comiclist_recycler_view);
        rListView.setLayoutManager(lLayout);
        realm = Realm.getDefaultInstance();
        Intent intent = getIntent();
        id = intent.getStringExtra("passValue");
        superHeroName = intent.getStringExtra("passName");

        try {

            OrderedRealmCollection<Comic> data = realm.where(Comic.class).equalTo("heroId",id).findAll();
            if(data.size()> 0) {

                ComicListAdapter rcAdapter = new ComicListAdapter(ComicListActivity.this, data,
                        new ComicListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position, Comic data) {


                                Intent intent = new Intent(getApplicationContext(), SwipeActivity.class);
                                intent.putExtra("imageId", data.getId());
                                intent.putExtra("comicTitle", data.getName());
                                intent.putExtra("pageCount",data.getPageCount());
                                startActivity(intent);

                                Toast.makeText(getApplicationContext(),
                                            "Tap or pinch to zoom in or zoom out",Toast.LENGTH_SHORT)
                                            .show();


                            }
                        });
                rListView.setAdapter(rcAdapter);
            }
            else{
                getComics();

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }



        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.search);
        fab.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.cardview_light_background)));
        fab.setRippleColor(getResources().getColor(R.color.colorPrimary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("query", superHeroName);
                startActivity(intent);


            }
        });
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(superHeroName);


    }

    private void getComics() {
        CharSequence text = "Fetching Data for "+superHeroName.toUpperCase()+"";
        progressDialog = ProgressDialog.show(ComicListActivity.this, text,"",false, false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    finish();
                }
                return false;
            }
        });
        new DownloadFilesTask().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class DownloadFilesTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {


        }


        protected String doInBackground(Void... voids) {




            try {
                Realm realm = Realm.getDefaultInstance();

                MarvelApiConfig marvelApiConfig = new MarvelApiConfig
                        .Builder("e292dee736108a2289dd259a751922cb",
                        "6f9c2292cb81e8b946e50d2b738557d85d8f7886")
                        .debug()
                        .build();
                ComicApiClient comicApiClient = new ComicApiClient(marvelApiConfig);

                ComicsQuery query = ComicsQuery
                        .Builder
                        .create()
                        .addCharacter(Integer.parseInt(id))
                        .build();
                MarvelResponse<ComicsDto> all = comicApiClient
                        .getAll(query);
                List<ComicDto> comicsDtoList;
                if(all.getResponse().getComics().size()<8) {
                     comicsDtoList = all
                            .getResponse()
                            .getComics();
                }
                else{
                     comicsDtoList = all.getResponse().getComics().subList(0,7);
                }


                final List<Comic> list = new ArrayList<>();

                    for (ComicDto comicsDto : comicsDtoList) {


                        saveImage(getApplicationContext(),
                                new DownloadImageTask().doInBackground(comicsDto.getThumbnail().
                                        getImageUrl(MarvelImage.Size.DETAIL)), comicsDto.getId(), "jpg");



                        pageCount = comicsDto.getPageCount();
                        Comic comic = new Comic(comicsDto.getId(), comicsDto.getTitle(), id,pageCount);
                        list.add(comic);
                    }
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            realm.copyToRealmOrUpdate(list);
                        }
                    });


            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";


        }

        protected void onPostExecute(String result) {

            try {

                OrderedRealmCollection<Comic> data = realm.where(Comic.class).equalTo("heroId",id).findAll();
                if(data.size()==0){
                    Toast.makeText(getApplicationContext(),
                            "No Comics returned for"+superHeroName+"",Toast.LENGTH_SHORT)
                            .show();
                }

                ComicListAdapter rcAdapter = new ComicListAdapter(ComicListActivity.this, data,
                        new ComicListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position, Comic data) {


                                Intent intent = new Intent(getApplicationContext(), SwipeActivity.class);
                                intent.putExtra("imageId",data.getId());
                                intent.putExtra("comicTitle",data.getName());
                                intent.putExtra("pageCount",data.getPageCount());
                                startActivity(intent);

                            }
                        });
                rListView.setAdapter(rcAdapter);
                progressDialog.dismiss();

            }
            catch (Exception e) {
                e.printStackTrace();
            }


        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comics_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                getComics();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
