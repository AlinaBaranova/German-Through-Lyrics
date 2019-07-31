package com.alinabaranova.youtubedemo;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

class KaraokeText implements Serializable {

    private ArrayList<Integer> times = new ArrayList<>();
    private ArrayList<String> lines = new ArrayList<>();

    KaraokeText(InputStream is) {

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line = reader.readLine();
            while (line != null) {

                String[] lineInfo = line.split("\t");
                if (lineInfo.length > 1) {

                    times.add(Integer.parseInt(lineInfo[0]));
                    lines.add(lineInfo[1]);

                } else {

                    lines.add(line);

                }

                line = reader.readLine();

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    ArrayList<Integer> getTimes() {
        return times;
    }

    ArrayList<String> getLines() {
        return lines;
    }

}
