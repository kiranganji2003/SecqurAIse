package com.netclan.secquraise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    DBHandler dbHandler = new DBHandler(MainActivity.this);
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 15 * 60 * 1000;
    private int countDataRefresh;
    private BatteryManager batteryManager;
    TextView connectivity, batteryCharging, batteryCharge, locationTextView, dateAndTimeTextView, captureCount;
    EditText editTextFrequency;
    Context context;
    FusedLocationProviderClient mFusedLocationClient;
    String locationString;
    int PERMISSION_ID;
    FirebaseDatabase db;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseDatabase.getInstance();
        reference = db.getReference("User");
        context = getApplicationContext();
        batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        captureCount = findViewById(R.id.capture_count);
        connectivity = findViewById(R.id.connectivity);
        batteryCharging = findViewById(R.id.battery_charging);
        batteryCharge = findViewById(R.id.battery_charg);
        locationTextView = findViewById(R.id.location);
        TextView refresh = findViewById(R.id.refresh);
        dateAndTimeTextView = findViewById(R.id.date_and_time);

        editTextFrequency = findViewById(R.id.frequency);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        showData();

        editTextFrequency.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    int curFreq = Math.max(Integer.parseInt(editTextFrequency.getText().toString()), 1);
                    delay = curFreq * 60 * 1000;
                    editTextFrequency.setText(String.valueOf(curFreq));
                    handler.removeCallbacks(runnable);
                    setDelay();
                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_animation);
                refresh.startAnimation(anim);
                storeDataInDatabase();
            }
        });

        if(isInternetConnected()) {
            try {
                ArrayList<Data> arrayList = dbHandler.readData();
                for (Data data : arrayList) {
                    reference.child(data.getTimestamp()).setValue(data);
                }
                dbHandler.removeData();
                dbHandler.close();
            }
            catch (Exception e) {  }
            finally {
                dbHandler.close();
            }
        }
     }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(runnable);
    }

    void storeDataInDatabase() {
        countDataRefresh++;
        captureCount.setText(String.valueOf(countDataRefresh));
        showData();
        String timeStamp = dateAndTimeTextView.getText().toString();
        Data data = new Data(connectivity.getText().toString(), batteryCharging.getText().toString(), batteryCharge.getText().toString(), locationTextView.getText().toString(), timeStamp);
        reference.child(timeStamp).setValue(data);

        if(!isInternetConnected()) {
            dbHandler.addNewData(data.getConnectivity(), data.getBatteryCharging(), data.batteryCharge, data.getLocation(), data.getTimestamp());
            dbHandler.close();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            locationString = String.valueOf(location.getLatitude()).substring(0, 9) + ", " + String.valueOf(location.getLongitude()).substring(0, 9);
                            locationTextView.setText(locationString);
                        }
                    }
                });
            }
            else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            locationTextView.setText(mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());
        }
    };

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        setDelay();
        super.onResume();
    }

    void setDelay() {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnable, delay);
                storeDataInDatabase();
            }
        }, delay);
    }

    String getDateAndTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDateAndTime = simpleDateFormat.format(new Date());
        return currentDateAndTime;
    }

    private void showData() {
        dateAndTimeTextView.setText(getDateAndTime());
        connectivity.setText(isInternetConnected() ? "ON" : "OFF");
        batteryCharging.setText(isChargingConnected() ? "ON" : "OFF");
        batteryCharge.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? String.valueOf(getBatteryPercentage()) : "");
        getLastLocation();
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }



    private boolean isChargingConnected() {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int getBatteryPercentage() {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

}