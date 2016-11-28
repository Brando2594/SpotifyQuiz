package com.bgray.spotifyquiz;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    // List of Song objects
    private List<Song> songList = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // create openweathermap.org web service URL using city
    private URL createURL(String song) {
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);

        try {
            // create URL for specified city and imperial units (Fahrenheit)
            String urlString = baseUrl + URLEncoder.encode(song) + apiKey;
            return new URL(urlString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null; // URL was malformed
    }

    // makes the REST web service call to get song data and
    // saves the data to a local HTML file
    private class GetSongTask
            extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }

                    return new JSONObject(builder.toString());
                }
            }
            catch (Exception e) {
                Snackbar.make(findViewById(R.id.activity_main),
                        R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); // close the HttpURLConnection
            }

            return null;
        }

        // process JSON response and update ListView
        @Override
        protected void onPostExecute(JSONObject song) {
            convertJSONtoArrayList(song); // repopulate songList
        }
    }

    // create Song objects from JSONObject containing the song's info
    private void convertJSONtoArrayList(JSONObject forecast) {
        songList.clear(); // clear old song data

        try {
            // get song's "list" JSONArray
            JSONArray list = forecast.getJSONArray("list");

            // convert each element of list to a Song object
            for (int i = 0; i < list.length(); ++i) {
                JSONObject song = list.getJSONObject(i); // get the name of the song

                // get the song's artist ("artist") JSONObject
                JSONObject artist = song.getJSONObject("artist");

                // get the song's album JSONObject
                JSONObject album =
                        song.getJSONArray("album").getJSONObject(0);

                // add new Weather object to weatherList
                songList.add(new Song(
                        song.getString("name"),
                        artist.getString("artist"), // minimum temperature
                        album.getString("album"))); // maximum temperature
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
