package com.grupoupc.pastillapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.models.Pharmacy;
import com.grupoupc.pastillapp.utils.APIGoogle.IGoogleAPI;
import com.grupoupc.pastillapp.utils.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PharmacyLocation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap pharmacyMap;
    String pharmacyLat, pharmacyLng, pharmacyName, pharmacyPhone;
    String currentLatitude, currentLongitude;
    String productName, productPrice;

    private TextView txtDistanceTime;
    private DatabaseReference pharmacyReference;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private IGoogleAPI iGoogleAPI;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constantes.setFullScreen(PharmacyLocation.this);
        setContentView(R.layout.activity_pharmacy_location);

        String pharmacyId = getIntent().getStringExtra(Constantes.PH_ID);
        productPrice = getIntent().getStringExtra(Constantes.A_PR_PRICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        txtDistanceTime = findViewById(R.id.tv_distance_time);
        pharmacyReference = FirebaseDatabase.getInstance().getReference(Constantes.N_PHARMACY)
                .child(pharmacyId);

        // Get Google API
        iGoogleAPI = Constantes.getGoogleServices();

        // get string PRODUCT NAME from ProductDetail activity
        productName = ProductDetail.getActivityInstance().getProductName();

        loadPharmacyDetails();
        configCurrentLocation();
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(PharmacyLocation.this,
                "Cargando datos",
                "Espere por favor",
                true,
                false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        pharmacyMap = googleMap;

        loadPharmacyMap();
    }

    private void loadPharmacyMap() {
        pharmacyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);

                assert pharmacy != null;
                pharmacyLat = pharmacy.getLatitude();
                pharmacyLng = pharmacy.getLongitude();
                pharmacyName = pharmacy.getName();
                pharmacyPhone = pharmacy.getPhone();

                moveMapCamera(pharmacyLat, pharmacyLng, pharmacyName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void moveMapCamera(String pharmacyLat, String pharmacyLng, String pharmacyName) {
        double lat = Double.parseDouble(pharmacyLat);
        double lng = Double.parseDouble(pharmacyLng);
        LatLng latLng = new LatLng(lat, lng);
        pharmacyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(pharmacyName)
                .icon(bitmapDescriptorFromVector(this));
        pharmacyMap.addMarker(markerOptions);
    }

    // Convertimos nuestro vector a imagen
    private BitmapDescriptor bitmapDescriptorFromVector(Context context) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_custom_marker);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void configCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(PharmacyLocation.this);
        checkedLocationPermissions();
    }

    private void checkedLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(PharmacyLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(PharmacyLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Si el usuario acepto los permisos entonces calcularemos su ubicación actual
            getCurrentLocation();
        } else {
            // Si lo permisos no fueron concedidos
            ActivityCompat.requestPermissions(PharmacyLocation.this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1000);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Revisamos si los servicios de ubicación estan habilitados
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            // obtenemos la última ubicación
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        // Si la ubicación no es nula, traemos las coordenadas
                        currentLatitude = String.valueOf(location.getLatitude());
                        currentLongitude = String.valueOf(location.getLongitude());
                    } else {
                        // Si la ubicación es nula, solicitamos acceso
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                Location rLocation = locationResult.getLastLocation();
                                currentLatitude = String.valueOf(rLocation.getLatitude());
                                currentLongitude = String.valueOf(rLocation.getLongitude());
                            }
                        };

                        // Actualizamos última ubicación
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                    setLatLng(currentLatitude, currentLongitude);
                }
            });
        } else {
            // Si los servicios no estan habilitados ...
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void setLatLng(String currentLatitude, String currentLongitude) {
        pharmacyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);

                assert pharmacy != null;
                String destLatitude = pharmacy.getLatitude();
                String destLongitude = pharmacy.getLongitude();

                getTimeToDestination(currentLatitude, currentLongitude, destLatitude, destLongitude);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Obtenemos la distancia y el tiempo entre lugar origen vs destino
    private void getTimeToDestination(String currentLatitude, String currentLongitude, String destLatitude, String destLongitude) {
        String requestApi;
        showProgressDialog();

        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentLatitude + "," + currentLongitude + "&" +
                    "destination=" + destLatitude + "," + destLongitude + "&" +
                    "key=" + getResources().getString(R.string.google_browser_key);

            Log.i("REQUEST_API", requestApi);

            iGoogleAPI.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    try {
                        assert response.body() != null;
                        JSONObject jsonObject = new JSONObject(response.body());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        // get distance
                        JSONObject distanceObject = legsObject.getJSONObject("distance");
                        String txt_distance = distanceObject.getString("text");
                        double distance_value = Double.parseDouble(txt_distance.replaceAll("[^0-9\\\\.]+", ""));

                        // get time
                        JSONObject timeObject = legsObject.getJSONObject("duration");
                        String txt_time = timeObject.getString("text");
                        int time_value = Integer.parseInt(txt_time.replaceAll("\\D+", ""));

                        String full_text_distTime;

                        //VALIDAMOS SI EL TIEMPO ES MAYOR A 60 MIN CALCULAMOS EN HORAS, CASO CONTRARIO EN MINUTOS
                        if (time_value > 60) {
                            int hours = time_value / 60;
                            int minutes_remaining = time_value % 60;

                            full_text_distTime = ("Desde tu ubicación demorarías " + hours + " horas y " + minutes_remaining + " minutos en auto. \nDistancia promedio de " + distance_value + " km.");
                        } else {
                            full_text_distTime = ("Desde tu ubicación demorarías " + time_value + " minutos en auto. \nDistancia promedio de " + distance_value + " km.");
                        }

                        txtDistanceTime.setText(full_text_distTime);
                        progressDialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPharmacyDetails() {
        CircleImageView photo;
        TextView txtName, txtAddress;
        Button btnCallPharmacy, btnShare;

        photo = findViewById(R.id.civ_photo);
        txtName = findViewById(R.id.tv_name);
        txtAddress = findViewById(R.id.tv_address);
        btnCallPharmacy = findViewById(R.id.btn_call);
        btnShare = findViewById(R.id.btn_share);

        showProgressDialog();

        pharmacyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);

                assert pharmacy != null;
                Glide.with(getApplicationContext())
                        .load(pharmacy.getPhoto())
                        .into(photo);

                txtName.setText(pharmacy.getName());
                txtAddress.setText(pharmacy.getAddress());

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });

        btnCallPharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPharmacy(pharmacyPhone);
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent();
            }
        });
    }

    private void callPharmacy(String phone) {
        Toast.makeText(this, "Llamando a " + pharmacyName, Toast.LENGTH_SHORT).show();

        Intent call = new Intent(Intent.ACTION_CALL);
        call.setData(Uri.parse("tel:" + "+" + phone));

        // validamos si tenemos permisos para llamar
        if (ContextCompat.checkSelfPermission(PharmacyLocation.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PharmacyLocation.this, new String[]{
                    Manifest.permission.CALL_PHONE
            }, 1001);
        } else {
            // Si el usuario ya acepto los permisos
            try {
                startActivity(call);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

    }

    private void shareContent() {
        String textToShare = "Hola, encontré este producto " + productName + " en " + pharmacyName + " al precio de " + productPrice + " soles.\nBy Pastillapp";
        Intent send = new Intent();
        send.setAction(Intent.ACTION_SEND);
        send.putExtra(Intent.EXTRA_TEXT, textToShare);
        send.setType("text/plain");

        Intent share = Intent.createChooser(send, null);
        startActivity(share);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // Si los permisos han sido otorgados
                    getCurrentLocation();
                } else {
                    txtDistanceTime.setText(getString(R.string.text_location_denied));
                    txtDistanceTime.setTextColor(getResources().getColor(R.color.colorRed));

                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 1001: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Si el permiso ha sido otorgado
                    callPharmacy(pharmacyPhone);
                } else {
                    Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}