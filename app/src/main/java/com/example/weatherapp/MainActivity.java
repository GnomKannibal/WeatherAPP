package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GetDataFromInternet.AsyncResponse, SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "MainActivity";

    private Button searchBtn;
    private EditText searchField;
    private TextView cityName;

    protected static boolean showWind=true;
    protected static boolean showPressure=true;
    protected static String color="red";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchField=findViewById(R.id.seatchField);
        cityName=findViewById(R.id.cityName);
        searchBtn = findViewById(R.id.searchButton);
        searchBtn.setOnClickListener(this);

        setupSharedPreferences();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showPressure=sharedPreferences.getBoolean(getString(R.string.show_pressure_settings_key), true);
        showWind=sharedPreferences.getBoolean(getString(R.string.show_wind_settings_key), true);
        color=sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value));

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.show_pressure_settings_key))){
            showPressure=sharedPreferences.getBoolean(getString(R.string.show_pressure_settings_key), true);
        } else if (key.equals(getString(R.string.show_wind_settings_key))){
            showWind=sharedPreferences.getBoolean(getString(R.string.show_wind_settings_key), true);
        } else if(key.equals(getString(R.string.pref_color_key))){
            color=sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // return super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //return super.onOptionsItemSelected(item);
        int id=item.getItemId();
        if (id==R.id.action_settings){
            Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
            //NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        //URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=London,uk&APPID=745e6193c0666ad60065381960dea811");

        URL url=buildUrl(searchField.getText().toString());

        cityName.setText(searchField.getText().toString());

        new GetDataFromInternet(this).execute(url);
    }


    private URL buildUrl(String city){
        String Base_url="https://api.openweathermap.org/data/2.5/weather";
        String PARAM_CITY="q";
        String PARAM_APPID="appid";
        String appid_value="745e6193c0666ad60065381960dea811";

        Uri buildUri= Uri.parse(Base_url).buildUpon().appendQueryParameter(PARAM_CITY,city).appendQueryParameter(PARAM_APPID,appid_value).build();
        URL url=null;

        try {
            url=new URL(buildUri.toString());
        } catch(MalformedURLException e){
            e.printStackTrace();
        }
        Log.d(TAG, "buildUrl: "+url);
        return url;
    }

    @Override
    public void proccessFinish(String output) {
        Log.d(TAG, "proccessFinish: "+ output);
        try {

            JSONObject resultJSON = new JSONObject(output);
            JSONObject weather = resultJSON.getJSONObject("main");
            JSONObject sys = resultJSON.getJSONObject("sys");

            TextView temp=findViewById(R.id.tempValue);
            String temp_K=weather.getString("temp");
            float temp_C=Float.parseFloat(temp_K);
            temp_C-=(float)273.15;
            String temp_C_string=Float.toString(temp_C);

            if (showWind) {
                temp.setText(temp_C_string);
            } else {
                temp.setText("");

            }

            TextView pressure=findViewById(R.id.pressureValue);

            if (showPressure) {
                pressure.setText(weather.getString("pressure"));
            } else{
                pressure.setText("");
            }

            TextView sunrise=findViewById(R.id.timeSunRise);
            String timeSunRise=sys.getString("sunrise");
            Locale myLocale=new Locale("en","GB");
            SimpleDateFormat formatter=new SimpleDateFormat("HH:mm:ss", myLocale);

            String dateString=formatter.format(new Date(Long.parseLong(timeSunRise) *1000+(60*60*1000)*3));


            sunrise.setTextColor(Color.parseColor(color));
            sunrise.setText(dateString);

            TextView sunset=findViewById(R.id.timeSunSet);
            String timeSunSet=sys.getString("sunset");
            //Locale myLocale=new Locale("en","GB");
            //SimpleDateFormat formatter=new SimpleDateFormat("HH:mm:ss", myLocale);

            dateString=formatter.format(new Date(Long.parseLong(timeSunSet) *1000+(60*60*1000)*3));


            sunset.setTextColor(Color.parseColor(color));
            sunset.setText(dateString);

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

}