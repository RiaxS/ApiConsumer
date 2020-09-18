package cr.ac.ucr.apiconsumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;

import org.w3c.dom.Text;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cr.ac.ucr.apiconsumer.api.RetrofitBuilder;
import cr.ac.ucr.apiconsumer.api.WeatherService;
import cr.ac.ucr.apiconsumer.modelo.Main;
import cr.ac.ucr.apiconsumer.modelo.Sys;
import cr.ac.ucr.apiconsumer.modelo.Weather;
import cr.ac.ucr.apiconsumer.modelo.WeatherResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private final String TAG = "MainActivity";
    private final int LOCATION_CODE_REQUEST = 1;
    private final int REQUEST_CODE = 23;
    private TextView tvGreeting;
    private TextView tvDescription;
    private ImageView ivImage;
    private TextView tvMinMax;
    private TextView tvCity;
    private TextView tvTemperatura;
    private ConstraintLayout clContainer;
    private String day;
    private Location location;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitude = 9.97625;
        longitude = -84.83836;

        AppBarLayout ablAppBarLayout = findViewById(R.id.abl_appbar);
        ablAppBarLayout.setOutlineProvider(null);
        ablAppBarLayout.setElevation(0);

        Toolbar tToolbar = findViewById(R.id.t_toolbar);
        tToolbar.setTitle("");
        setSupportActionBar(tToolbar);

        clContainer = findViewById(R.id.cl_container);

        tvGreeting = findViewById(R.id.tv_greeting);
        tvDescription = findViewById(R.id.tv_descripction);

        tvMinMax = findViewById(R.id.tv_minmax);
        tvCity = findViewById(R.id.tv_city);
        tvTemperatura = findViewById(R.id.tv_temperature);

        ivImage = findViewById(R.id.iv_image);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        checkPermission();

        setBackgroundGreeting();

        //getWeather(latitude, longitude);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.im_open_search:

                openSearchActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        //intent.putExtra("name", "name");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode ==  REQUEST_CODE && resultCode == RESULT_OK){
                String city = data.getStringExtra(SearchActivity.KEY);
                Log.i(TAG, "city" + city);

                LatLng latlon = getLocalLocationFromAddress(city);

                if(latlon != null){
                    Log.i(TAG, latlon.latitude +"--"+ latlon.longitude);
                    getWeather(latlon.latitude,  latlon.longitude);
                }
            }

        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage().toString());
        }

    }

    private LatLng getLocalLocationFromAddress(String city) {

        Geocoder geocoder = new Geocoder(this);
        List<Address> address;
        LatLng latLng = null;

        try {
            address = geocoder.getFromLocationName(city, 5);
            if (address == null){
                return null;
            }

            Address location = address.get(0);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e){

            Log.e(TAG, "Error" + e.getMessage().toString());


        }


        return latLng;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_CODE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission();
            } else {
                getWeather(latitude, longitude);
                //No dieron permiso
            }
        }
    }

    private void checkPermission() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,

                    },
                    LOCATION_CODE_REQUEST
            );
            return;
        }
    }
    //si da error probar con crearla local
         location =locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        try{
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                onLocationChanged(location);
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Para mejor funcionalidad active el GPS")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();

                getWeather(latitude, longitude);
                //LATLONG default
                }
            } catch (Exception e){
            //latlong default
            getWeather(latitude, longitude);
            //e.printStackTrace();
        }

    }

    public void setBackgroundGreeting(){

        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 5 && timeOfDay < 12){
            tvGreeting.setText(R.string.day);
            clContainer.setBackgroundResource(R.drawable.background_day);

           // ivImage.setImageDrawable(R.drawable.ic_sunny_background);
        } else if (timeOfDay >=12 && timeOfDay < 19){
            tvGreeting.setText(R.string.afternoon);
            clContainer.setBackgroundResource(R.drawable.background_afternoon);
        } else{
            tvGreeting.setText(R.string.night);
            clContainer.setBackgroundResource(R.drawable.background_night);
        }

        String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

    }


    private void getWeather(double latitude, double longitude){
        WeatherService service = RetrofitBuilder.createService(WeatherService.class);

        Call<WeatherResponse> response = service.getWeatherByCoodinates(latitude,longitude);

        final AppCompatActivity activity = this;
        response.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {

                Log.i(TAG, String.valueOf(call.request().url()));

                if(response.isSuccessful()){
                 WeatherResponse weatherResponse = response.body();
                 Log.i(TAG, "onResponse: " + weatherResponse.toString());

                 Main main = weatherResponse.getMain();
                 List<Weather> weatherList = weatherResponse.getWeather();
                 Sys sys = weatherResponse.getSys();
                 tvTemperatura.setText(String.valueOf(main.getTemp()));

                    String minmax = getString(R.string.minmax, String.valueOf(Math.round(main.getTemp_min())), String.valueOf(Math.round(main.getTemp_max())));

                    tvMinMax.setText(String.valueOf(main.getTemp_min()) + " | " + String.valueOf(main.getTemp_max()));

                 tvMinMax.setText(minmax);

                 if(weatherList.size()>0){
                     Weather weather = weatherList.get(0);
                     tvDescription.setText(String.format("$s, $s", day, weather.getMain()));
                     String imageUrl = String.format("https://openweathermap.org/img/wn/$s@2x.png", weather.getIcon());

                     RequestOptions options = new RequestOptions()
                             .placeholder(R.drawable.ic_sunny_background)
                             .error(R.mipmap.ic_sunny)//corregir
                             .centerCrop()
                             .diskCacheStrategy(DiskCacheStrategy.ALL)
                             .priority(Priority.HIGH);

                     Glide.with(activity)
                             .load(imageUrl)
                             .apply(options)
                             .into(ivImage);
                 }

                 tvCity.setText(String.format("%s, %s", weatherResponse.getName(), sys.getCountry()));



             } else {
                 Log.e(TAG, "OnError: "+ response.errorBody().toString());
                    getWeather(latitude, longitude);
             }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, Throwable t) {
                throw  new RuntimeException(t);

            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //Log.i(TAG, location.getLatitude() + "" + location.getLongitude());
        //getWeather(location.getLatitude(), location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        getWeather(latitude, longitude);

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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}