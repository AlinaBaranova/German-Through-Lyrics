package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Objects;


public class StartActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {

    ArrayList<Bitmap> imagesShown = new ArrayList<>();          // images of songs in filter
    ArrayList<String> artistNamesShown = new ArrayList<>();     // artist name for songs in filter
    ArrayList<String> songNamesShown = new ArrayList<>();       // song name of songs in filter

    ListAdapter adapter;    // contains songs, their artists and their album covers
    ListView listView;      // shows songs, their artists and their album covers with adapter

    SQLiteDatabase database;

    HorizontalScrollView topicScrollView;       // scrollview for grammatical topics
    HorizontalScrollView genreScrollView;       // scrollview for genres

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // hide bar with app name
        Objects.requireNonNull(getSupportActionBar()).hide();

        // load database
//        getApplicationContext().deleteDatabase("app.db");
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        database = dbHelper.getReadableDatabase();

        // find listview and set clicklistener to it
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        // find search view and set textlistener to it
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

        // find scrollview with grammatical topics and genres
        topicScrollView = findViewById(R.id.topicScrollView);
        genreScrollView = findViewById(R.id.genreScrollView);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText.toLowerCase();

        // empty lists that contain information about songs shown in the filter
        imagesShown = new ArrayList<>();
        artistNamesShown = new ArrayList<>();
        songNamesShown = new ArrayList<>();

        // add songs that contain query
        if (text.length() > 0) {
            String[] textParts = text.split(" ");

            Cursor c = database.rawQuery("SELECT song_name, artist, album_cover FROM songs ORDER BY artist", null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                while(c.moveToNext()) {

                    String artistName = c.getString(1);
                    String songName = c.getString(0);

                    boolean ifFound = true;
                    for (String textPart : textParts) {
                        if (! artistName.toLowerCase().contains(textPart) && ! songName.toLowerCase().contains(textPart)) {
                            ifFound = false;
                        }
                    }

                    if (ifFound) {

                        byte[] imgByte = c.getBlob(2);
                        imagesShown.add(BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length));
                        artistNamesShown.add(artistName);
                        songNamesShown.add(songName);

                    }
                }
            }
            c.close();

            // hide scrollviews with grammatical topics and genres
            topicScrollView.setVisibility(View.GONE);
            genreScrollView.setVisibility(View.GONE);

        } else {

            // if query is empty, show scrollviews with grammatical topics and genres
            topicScrollView.setVisibility(View.VISIBLE);
            genreScrollView.setVisibility(View.VISIBLE);

        }

        // create adapter with information of songs in the filter and assign it to listview
        adapter = new ListAdapter(getApplicationContext(), imagesShown, artistNamesShown, songNamesShown);
        listView.setAdapter(adapter);

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String artist = artistNamesShown.get(position);
        String song_name = songNamesShown.get(position);

        // find id of song
        Cursor c = database.rawQuery("SELECT * FROM songs WHERE song_name='" + song_name + "' AND artist='" + artist + "'", null);
        int songId = -1;
        if (c.getCount() > 0) {
            int idIndex = c.getColumnIndex("song_id");

            c.moveToFirst();
            songId = c.getInt(idIndex);
        }
        c.close();

        // open MenuActivity for song
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        intent.putExtra("songId", songId);
        startActivity(intent);
    }

    public void onClickTextView(View view) {
        // for textviews in scrollviews with genres and grammatical constructions

        Intent intent = new Intent(getApplicationContext(), FilteredActivity.class);

        int id = view.getId();
        switch (id) {
            case R.id.textViewPrep:
                intent.putExtra("constrType", "prep");
                intent.putExtra("sortType", "constr");
                break;

            case R.id.textViewPref:
                intent.putExtra("constrType", "pref");
                intent.putExtra("sortType", "constr");
                break;

            case R.id.textViewConj:
                intent.putExtra("constrType", "conj");
                intent.putExtra("sortType", "constr");
                break;

            case R.id.textViewPassive:
                intent.putExtra("constrType", "passive");
                intent.putExtra("sortType", "constr");
                break;

            case R.id.textViewHipHop:
                intent.putExtra("genre", "Hip-Hop");
                intent.putExtra("sortType", "genre");
                break;

            case R.id.textViewRock:
                intent.putExtra("genre", "Rock");
                intent.putExtra("sortType", "genre");
                break;

            case R.id.textViewPop:
                intent.putExtra("genre", "Pop");
                intent.putExtra("sortType", "genre");
                break;

            case R.id.textViewSingSong:
                intent.putExtra("genre", "Singer/Songwriter");
                intent.putExtra("sortType", "genre");
                break;

            case R.id.textViewElectronic:
                intent.putExtra("genre", "Electronic");
                intent.putExtra("sortType", "genre");
                break;

            case R.id.textViewMetal:
                intent.putExtra("genre", "Metal");
                intent.putExtra("sortType", "genre");
                break;

            case R.id.textViewGenre:
                genreScrollView.scrollTo(findViewById(R.id.textViewHipHop).getLeft(), 0);
                intent = null;
                break;

            default:
                topicScrollView.scrollTo(findViewById(R.id.textViewPrep).getLeft(), 0);
                intent = null;

        }

        if (intent != null) {

            startActivity(intent);
        }

    }

    public void onBackPressed() {
        // go out of the app if back button is pressed
        this.finishAffinity();
    }

}
