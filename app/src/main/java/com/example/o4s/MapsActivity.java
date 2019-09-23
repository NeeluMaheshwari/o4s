package com.example.o4s;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    static final int DATE_PICKER_ID = 1111;
    private Date reqdDate;
    private Button dateButton;
    private String latitude;
    private String longitude;
    String selectedtimezoneidentifier = "Asia/Kolkata";
    private TextView officialSunriseTimeTV,officialMoonriseTimeTV;
    private TextView officialSunsetTimeTV,officialMoonsetTimeTV;
    private String officialSunrise,officialMoonrise;
    private String officialSunset,officialMoonset;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        dateButton = (Button) findViewById(R.id.set_date_btn);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            int year = selectedYear;
            int month = selectedMonth;
            int day = selectedDay;

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            reqdDate = cal.getTime();
            dateButton.setText(formatDate(reqdDate));

        }
    };

    public void DecrementDateByOne(View v) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(reqdDate);
        cal.add(Calendar.DATE, -1);
        reqdDate = cal.getTime();
        dateButton.setText(formatDate(reqdDate));

    }

    @SuppressLint("SimpleDateFormat")
    public String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
        return sdf.format(date);
    }

    public void IncrementDateByOne(View v) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(reqdDate);
        cal.add(Calendar.DATE, 1);
        reqdDate = cal.getTime();
        dateButton.setText(formatDate(reqdDate));
    }

    @SuppressWarnings("deprecation")
    public void onSetDateBtnClicked(View v) {
        showDialog(DATE_PICKER_ID);
    }


    private void updateDateOnLaunch() {
        reqdDate = new Date();
        dateButton.setText(formatDate(reqdDate));

    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_PICKER_ID:
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(this, datePickerListener, year, month,
                        day);
        }
        return null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void searchLocation(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        try{
            if (location != null || !location.equals("")) {
                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(location, 1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                latitude = String.format("%.3f", address.getLatitude());
                longitude = String.format("%.3f", address.getLongitude());
                updateDataAndCalculateSunriseSunset();
                updateDataAndCalculateMoonriseMoonset();

            }
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"Kindly fill the location",Toast.LENGTH_LONG).show();

        }


    }

    public void updateDataAndCalculateSunriseSunset() {

        try{
            // get data
            SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");

            try {
                reqdDate = sdf.parse(dateButton.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            // process data
            com.example.o4s.Location location = new com.example.o4s.Location(latitude,longitude);
            SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, selectedtimezoneidentifier);

            Calendar cal = Calendar.getInstance();
            cal.setTime(reqdDate);

            officialSunrise = calculator.getOfficialSunriseForDate(cal);
            officialSunset = calculator.getOfficialSunsetForDate(cal);

            updateUIWithResults();
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"Kindly select the date",Toast.LENGTH_LONG).show();

        }



    }



    private void updateUIWithResults() {

        officialSunriseTimeTV = (TextView) findViewById(R.id.official_sunrise_time);
        officialSunsetTimeTV = (TextView) findViewById(R.id.official_sunset_time);
        officialSunriseTimeTV.setText(officialSunrise);
        officialSunsetTimeTV.setText(officialSunset);


    }

    public void updateDataAndCalculateMoonriseMoonset() {

        try{
            // get data
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

            try {
                reqdDate = sdf.parse(dateButton.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(reqdDate);


            int year = cal.get(Calendar.YEAR), month = cal.get(Calendar.MONTH), day = cal.get(Calendar.DAY_OF_MONTH), h = cal.get(Calendar.HOUR_OF_DAY), m = cal.get(Calendar.MINUTE), s = cal.get(Calendar.SECOND); // in UT !!!
            try {
                MoonCalculator smc = new MoonCalculator(year, month, day, h, m, s, longitude, latitude);
                smc.calcSunAndMoon();
                officialMoonrise = MoonCalculator.getDateAsString(smc.moonRise);
                officialMoonset =  MoonCalculator.getDateAsString(smc.moonSet);

                updateMoon();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"Kindly select the date",Toast.LENGTH_LONG).show();

        }


    }

    private void updateMoon() {

        officialMoonriseTimeTV = (TextView) findViewById(R.id.official_moonrise_time);
        officialMoonsetTimeTV = (TextView) findViewById(R.id.official_moonset_time);
        officialMoonriseTimeTV.setText(officialMoonrise);
        officialMoonsetTimeTV.setText(officialMoonset);


    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                latitude = String.format("%.3f",
                        data.getDoubleExtra("latitude", 0.000));
                longitude = String.format("%.3f",
                        data.getDoubleExtra("longitude", 0.000));
                updateDataAndCalculateSunriseSunset();

            }
            if (resultCode == RESULT_CANCELED) {
                latitude = "0.00";
                longitude = "0.00";
            }
            updateDataAndCalculateSunriseSunset();
        }
    }



}