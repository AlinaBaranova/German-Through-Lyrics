package com.alinabaranova.youtubedemo;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Objects;

public class FilteredActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {

    ArrayList<Bitmap> images = new ArrayList<>();       // all album covers in database
    ArrayList<String> artistNames = new ArrayList<>();  // all artist names for songs
    ArrayList<String> songNames = new ArrayList<>();    // all songs names

    ArrayList<Bitmap> imagesShown = new ArrayList<>();          // images of songs in filter
    ArrayList<String> artistNamesShown = new ArrayList<>();     // artist name for songs in filter
    ArrayList<String> songNamesShown = new ArrayList<>();       // song name of songs in filter

    ListAdapter adapter;    // contains songs, their artists and their album covers
    ListView listView;      // shows songs, their artists and their album covers with adapter

    SQLiteDatabase database;

    String sortType;    // type of sort
    String constrType;  // if type of sort is "constr", contains construction type
    String genre;       // if type of sort is "genre", contains genre

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered);

        // hide bar with app name
        Objects.requireNonNull(getSupportActionBar()).hide();

        // get type of sort
        Intent intent = getIntent();
        sortType = intent.getStringExtra("sortType");

        // load database
//        getApplicationContext().deleteDatabase("app.db");
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        database = dbHelper.getReadableDatabase();

        // query database
        ArrayList<Integer> songIds = new ArrayList<>();
        Cursor c;
        if (sortType.equals("constr")) {
            // find all ids of songs that have certain type of constructions
            constrType = intent.getStringExtra("constrType");
            c = database.rawQuery("SELECT song_id FROM constructions WHERE constr_type='" + constrType + "'", null);

        } else {
            // find all ids of songs that belong to certain genre
            genre = intent.getStringExtra("genre");
            c = database.rawQuery("SELECT song_id FROM songs WHERE genre='" + genre + "'", null);
        }

        while (c.moveToNext()) {
            songIds.add(c.getInt(0));
        }

        // extract song names, artist and images for songs with ids listed in songIds
        for (int i=0; i < songIds.size(); i++) {
            c = database.rawQuery("SELECT song_name, artist, album_cover FROM songs WHERE song_id=" + songIds.get(i), null);
            if (c.moveToFirst()) {
                byte[] imgByte = c.getBlob(2);
                images.add(BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length));
                artistNames.add(c.getString(1));
                songNames.add(c.getString(0));
            }
        }
        c.close();

        // find list view and set its clicklistener
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        // add all values to arraylists for adapter
        imagesShown.addAll(images);
        artistNamesShown.addAll(artistNames);
        songNamesShown.addAll(songNames);

        // set adapter and add it to listview
        adapter = new ListAdapter(getApplicationContext(), imagesShown, artistNamesShown, songNamesShown);
        listView.setAdapter(adapter);

        // find searchview and set its textlistener
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

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
            Log.i("Text", text);
            String[] textParts = text.split(" ");
            for (int i = 0; i < artistNames.size(); i++) {
                Bitmap curImage = images.get(i);
                String curArtistName = artistNames.get(i);
                String curSongName = songNames.get(i);

                // check if artist or song name contain all parts of query
                boolean ifFound = true;
                for (String textPart : textParts) {
                    if (!curArtistName.toLowerCase().contains(textPart) && !curSongName.toLowerCase().contains(textPart)) {
                        ifFound = false;
                    }
                }

                // add information for songs whose artist and song name contain all parts of query
                if (ifFound & !(artistNamesShown.contains(curArtistName) && songNamesShown.contains(curSongName))) {
                    imagesShown.add(curImage);
                    artistNamesShown.add(curArtistName);
                    songNamesShown.add(curSongName);
                }
            }

        } else {
            // if nothing is typed, add all songs
            imagesShown.addAll(images);
            artistNamesShown.addAll(artistNames);
            songNamesShown.addAll(songNames);

        }

        // create adapter with information of songs in the filter or all songs and assign it to listview
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
        intent.putExtra("prevAct", "FilteredActivity");
        intent.putExtra("sortType", sortType);

        if (sortType.equals("constr")) {
            intent.putExtra("constrType", constrType);
        } else {
            intent.putExtra("genre", genre);
        }

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent newIntent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(newIntent);
    }
}
