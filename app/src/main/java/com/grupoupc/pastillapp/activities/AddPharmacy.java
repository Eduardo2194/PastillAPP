package com.grupoupc.pastillapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.utils.Constantes;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddPharmacy extends AppCompatActivity {

    private CircleImageView civPharmacyPhoto;
    private EditText etPName, etPAddress, etPPhone, etPOpen, etPClose;
    private String placeId;
    private String pharmacyPhotoUrl;

    private static final int AUTOCOMPLETE_REQUEST_CODE = 2610;
    private ProgressDialog progressDialog;
    private Uri imageUri = null;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pharmacy);

        civPharmacyPhoto = findViewById(R.id.civ_apPhoto);
        etPName = findViewById(R.id.et_apName);
        etPAddress = findViewById(R.id.et_apAddress);
        etPPhone = findViewById(R.id.et_apPhone);
        etPOpen = findViewById(R.id.et_apOpen);
        etPClose = findViewById(R.id.et_apClose);
        Button btnSavePharmacy = findViewById(R.id.btn_apSave);

        storageReference = FirebaseStorage.getInstance().getReference(Constantes.S_PHARMACY);
        civPharmacyPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Initialize Google Places
        initializeGooglePlaces();

        btnSavePharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerPharmacy();
            }
        });

        etPOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatetimePicker(etPOpen);
            }
        });

        etPClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatetimePicker(etPClose);
            }
        });

    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(AddPharmacy.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddPharmacy.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                cropImagePicker();
            }
        } else {
            cropImagePicker();
        }
    }

    private void cropImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(AddPharmacy.this);
    }

    private void uploadPhotoToStorage() {
        progressDialog = ProgressDialog.show(AddPharmacy.this,
                "Cargando imagen",
                "Espere por favor",
                true,
                false);

        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(Constantes.getTimeInMilliSeconds() + Constantes.EXTENSION_JPG);
            StorageTask task = fileReference.putFile(imageUri);

            task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        assert downloadUri != null;
                        pharmacyPhotoUrl = downloadUri.toString();

                        Glide.with(getApplicationContext())
                                .load(pharmacyPhotoUrl)
                                .into(civPharmacyPhoto);

                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(AddPharmacy.this, "No se puedo cargar la imagen, intente de nuevo", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPharmacy.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Utilizamos el servicio de Google Maps (Google Places API)
    private void initializeGooglePlaces() {
        // SI el servico no ha sido inicializado, lo inicializamos
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_places_key));
        }

        // Llamamos al servicio de PlacesCliente (aunque no lo utilizamos más, es necesario para que funciones el buscador de calles)
        PlacesClient placesClient = Places.createClient(this);

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);

        // Al hacer clic en nuestro edittext se abrirá el buscador de calles de google
        etPAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .build(AddPharmacy.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

            }
        });
    }

    // Metodo propio de android para recoger los datos de la actividad externa (CropImageActivity y AutocompleteActivity)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            // Recogemos los resultados de Places API
            if (resultCode == RESULT_OK) {

                // Result for Auto complete places
                assert data != null;
                Place place = Autocomplete.getPlaceFromIntent(data);
                etPAddress.setText(place.getAddress());
                placeId = place.getId();

            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);

            // Recogemos resultados de CropImageActivity
            if (resultCode == RESULT_OK) {

                // Result for Crop Image
                assert cropResult != null;
                imageUri = cropResult.getUri();
                civPharmacyPhoto.setImageURI(imageUri);
                uploadPhotoToStorage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert cropResult != null;
                Exception e = cropResult.getError();
                Toast.makeText(AddPharmacy.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Abrimos un dialog para elegir la hora de atencion
    private void showDatetimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                editText.setText(simpleDateFormat.format(calendar.getTime()));
            }
        };

        new TimePickerDialog(AddPharmacy.this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false)
                .show();
    }

    // Registramos farmacia
    private void registerPharmacy() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference pharmacyReference = database.getReference(Constantes.N_PHARMACY).push();

        String pharmacyId = pharmacyReference.getKey();
        String pharmacyName = etPName.getText().toString().trim();
        String pharmacyAddress = etPAddress.getText().toString().trim();
        String pharmacyPhone = etPPhone.getText().toString().trim();
        String pharmacyOpen = etPOpen.getText().toString().trim();
        String pharmacyClose = etPClose.getText().toString().trim();

        // Validamos campos obligatorios
        if (pharmacyName.isEmpty()) {
            etPName.setError("El nombre de la farmacia es obligatorio");
            etPName.requestFocus();
        } else if (pharmacyAddress.isEmpty()) {
            etPAddress.setError("La dirección de la farmacia es obligatoria");
            etPAddress.requestFocus();
        } else if (pharmacyPhone.isEmpty()) {
            etPPhone.setError("El teléfono de la farmacia es obligatorio");
            etPPhone.requestFocus();
        } else if (pharmacyOpen.isEmpty()) {
            etPOpen.setError("El horario de apertura es obligatorio");
            etPOpen.requestFocus();
        } else if (pharmacyClose.isEmpty()) {
            etPClose.setError("El horario de cierre es obligatorio");
            etPClose.requestFocus();
        } else if (pharmacyPhotoUrl == null) {
            Toast.makeText(AddPharmacy.this, "La foto de la farmacia es obligatoria", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog = ProgressDialog.show(AddPharmacy.this,
                    "Registrando farmacia",
                    "Espere por favor",
                    true,
                    false);

            HashMap<String, String> pharmacyValues = new HashMap<>();
            pharmacyValues.put(Constantes.PH_ID, pharmacyId);
            pharmacyValues.put(Constantes.PH_NAME, pharmacyName);
            pharmacyValues.put(Constantes.PH_ADDRESS, pharmacyAddress);
            pharmacyValues.put(Constantes.PH_PLACE_ID, placeId);
            pharmacyValues.put(Constantes.PH_PHONE, pharmacyPhone);
            pharmacyValues.put(Constantes.PH_OPEN, pharmacyOpen);
            pharmacyValues.put(Constantes.PH_CLOSE, pharmacyClose);
            pharmacyValues.put(Constantes.PH_PHOTO, pharmacyPhotoUrl);

            // Guardamos los datos de la farmacia en firebase database
            pharmacyReference.setValue(pharmacyValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPharmacy.this, "Farmacia agregada con éxito", Toast.LENGTH_SHORT).show();
                        restartViews();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(AddPharmacy.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Limpiamos los edittext y nuestro imageview
    private void restartViews() {
        civPharmacyPhoto.setImageResource(R.drawable.logo_app);
        etPName.setText("");
        etPAddress.setText("");
        etPPhone.setText("");
        etPOpen.setText("");
        etPClose.setText("");
    }
}