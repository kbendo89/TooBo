package com.example.weatherapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.weatherapp.Common.Common;
import com.example.weatherapp.Helper.Helper;
import com.example.weatherapp.Model.OpenWeatherMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;

public class WeatherApp extends AppCompatActivity implements LocationListener {

    TextView txtCity, txtLastUpdate, txtHumidity, txtDeg, txtDescription, txtTime;
    ImageView imageView;
    int MY_PERMISSION = 0;
    LocationManager locationManager;
    String provider;
    static double lat, lon;
    OpenWeatherMap openWeatherMap = new OpenWeatherMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCity = (TextView) findViewById(R.id.text_city);
        txtLastUpdate = (TextView) findViewById(R.id.text_lastUpdate);
        txtHumidity = (TextView) findViewById(R.id.text_humidity);
        txtDeg = (TextView) findViewById(R.id.text_deg);
        txtDescription = (TextView) findViewById(R.id.text_description);
        txtTime = (TextView) findViewById(R.id.text_time);
        imageView = (ImageView) findViewById(R.id.idImage);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(WeatherApp.this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, MY_PERMISSION);
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if(location == null)
            Log.e("TAG","No location");

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(WeatherApp.this,new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, MY_PERMISSION);
        }
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WeatherApp.this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, MY_PERMISSION);
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lat=location.getLatitude();
        lon=location.getLongitude();

        new GetWeather().execute(Common.apiRequest(String.valueOf(lat),String.valueOf(lon)));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    private class GetWeather extends AsyncTask<String,Void,String> {

        ProgressDialog pd= new ProgressDialog(WeatherApp.this);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setTitle("Please wait ...");
            pd.show();
        }

        @Override
        protected String doInBackground(String...params){
            String stream=null;
            String urlString=params[0];
            Helper http=new Helper();
            stream=http.getHTTPData(urlString);
            return stream;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Gson gson=new Gson();
            Type mType= new TypeToken<OpenWeatherMap>(){}.getType();
            openWeatherMap=gson.fromJson(s, mType);
            pd.dismiss();


            txtCity.setText(String.format(" City: %s,%s",openWeatherMap.getName(),openWeatherMap.getSys().getCountry()));
            txtLastUpdate.setText(String.format("Last update : %s", Common.getDateNow()));
            txtDescription.setText(String.format("Description: %s",openWeatherMap.getWeather().get(0).getDescription()));
            txtHumidity.setText(String.format("Humidity: %d",openWeatherMap.getMain().getHumidity()));
            txtTime.setText(String.format("%s/%s,",openWeatherMap.getSys().getSunrise(),openWeatherMap.getSys().getSunset()));
            txtDeg.setText(String.format("Temperature: %.2f °C",openWeatherMap.getMain().getTemp()));

            Picasso.get()
                    .load(Common.getImage(openWeatherMap.getWeather().get(0).getIcon()))
                    .into(imageView);

        }
    }
}