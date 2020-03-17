package com.alinabaranova.youtubedemo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {

    ArrayList<String> songs;
    ArrayList<String> songsShown;

    ArrayAdapter<String> adapter;

    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // find all songs with their artist in database and add them to arraylist
        songs = new ArrayList<>();

        // load database
        getApplicationContext().deleteDatabase("app.db");
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        database = dbHelper.getReadableDatabase();

        // query database
        Cursor c = database.rawQuery("SELECT * FROM songs", null);
        if (c.getCount() > 0) {
            int songNameIndex = c.getColumnIndex("song_name");
            int artistIndex = c.getColumnIndex("artist");

            c.moveToFirst();
            do {
                songs.add(c.getString(artistIndex) + " — " + c.getString(songNameIndex));
            } while (c.moveToNext());
        }

        // find listview and set adapter to it
        ListView listView = findViewById(R.id.listView);

        songsShown = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, songsShown);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        // find searchview
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

        songsShown.clear();

        // add songs that contain query
        if (text.length() > 0) {
            for (String song : songs) {
                if (song.toLowerCase().contains(text)) {
                    songsShown.add(song);
                }
            }
        }

        adapter.notifyDataSetChanged();

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String[] info = songsShown.get(position).split(" — ");
        String artist = info[0];
        String song_name = info[1];

        // find id of song
        Cursor c = database.rawQuery("SELECT * FROM songs WHERE song_name='" + song_name + "' AND artist='" + artist + "'", null);

        int songId = -1;
        if (c.getCount() > 0) {
            int idIndex = c.getColumnIndex("song_id");

            c.moveToFirst();
            songId = c.getInt(idIndex);
        }

        // open MenuActivity for song
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        intent.putExtra("songId", songId);
        Log.i("songId", Integer.toString(songId));
        startActivity(intent);
    }
}
