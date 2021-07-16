package com.grupoupc.pastillapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grupoupc.pastillapp.models.User;
import com.grupoupc.pastillapp.utils.APIClient.CountApiClient;
import com.grupoupc.pastillapp.utils.APIClient.ICountAPI;
import com.grupoupc.pastillapp.utils.APIGoogle.GoogleAPIClient;
import com.grupoupc.pastillapp.utils.APIGoogle.IGoogleAPI;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;

public class Constantes {
    // TODO: Firebase Database
    public static final String N_USER = "User";
    public static final String U_ID = "id";
    public static final String U_NAME = "username";
    public static final String U_EMAIL = "email";
    public static final String U_PHOTO = "photo";
    public static final String U_TYPE = "type";

    public static final String N_PHARMACY = "Pharmacy";
    public static final String PH_ID = "id";
    public static final String PH_NAME = "name";
    public static final String PH_ADDRESS = "address";
    public static final String PH_LATITUDE = "latitude";
    public static final String PH_LONGITUDE = "longitude";
    public static final String PH_PHONE = "phone";
    public static final String PH_OPEN = "open";
    public static final String PH_CLOSE = "close";
    public static final String PH_PHOTO = "photo";

    public static final String N_PRODUCT = "Product";
    public static final String P_ID = "id";
    public static final String P_NAME = "name";
    public static final String P_PRESENTATION = "presentation";
    public static final String P_CATEGORY = "category";
    public static final String P_PRESCRIPTION = "prescription";
    public static final String P_REGISTER_DATE = "register_date";
    public static final String P_DESCRIPTION = "description";
    public static final String P_PHOTO = "photo";

    public static final String N_AVAILABLE = "Available";
    public static final String A_PH_ID = "pharmacy_id";
    public static final String A_PH_NAME = "pharmacy_name";
    public static final String A_PR_PRICE = "product_price";

    public static final String N_PHARMACY_PRODUCT = "PharmacyProduct";

    // TODO: Firebase Storage
    public static final String S_PHARMACY = "pharmacy_profile";
    public static final String S_PRODUCT = "product_photo";
    public static final String S_USER = "user_photo";

    // TODO: Other values
    private static final String GOOGLE_API_BASE_URL = "https://maps.googleapis.com";
    public static final String VISITS_API_BASE_URL = "https://app-sistemas-distribuidos.herokuapp.com/metro-app-service/usuario/visita/";

    // TODO: Other values
    public static final String ADMIN = "admin";
    public static final String DEFAULT = "default";
    public static final String EXTENSION_JPG = ".jpg";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String ONE = "1";

    // TODO: Global Methods
    public static String getTimeInMilliSeconds() {
        Calendar rightNow = Calendar.getInstance();
        long offSet = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
        return String.valueOf((rightNow.getTimeInMillis() + offSet) % (24 * 60 * 60 * 1000));
    }

    //validamos si la vista se va mostrar u ocultar, según el perfil admin o user
    public static void validateUserType(View view) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        assert mUser != null;
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(Constantes.N_USER).child(mUser.getUid());
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                if (user.getType().equals(Constantes.ADMIN)) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void setFullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 o superior
            final WindowInsetsController insetsController = activity.getWindow().getDecorView().getWindowInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        } else { // Menor a android 11
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
    }

    public static IGoogleAPI getGoogleServices() {
        return GoogleAPIClient.getClient(GOOGLE_API_BASE_URL)
                .create(IGoogleAPI.class);
    }

    public static ICountAPI getVisitsServices() {
        return CountApiClient.getClient(VISITS_API_BASE_URL)
                .create(ICountAPI.class);
    }

    public static void openGallery(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                cropImagePicker(activity);
            }
        } else {
            cropImagePicker(activity);
        }
    }

    public static void cropImagePicker(Activity activity) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(activity);
    }

}
