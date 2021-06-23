package com.grupoupc.pastillapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddProduct extends AppCompatActivity {
    private ImageView imgProductPhoto;
    private TextView tvTitleAddImage;
    private EditText etPName, etPPresentation, etPCategory, etPRegisterDate, etPDescription;
    private RadioButton rbYes, rbNo;
    private String productPhotoUrl;

    private ProgressDialog progressDialog;
    private Uri imageUri = null;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        tvTitleAddImage = findViewById(R.id.tv_apText);
        imgProductPhoto = findViewById(R.id.img_apPhoto);
        etPName = findViewById(R.id.et_apName);
        etPPresentation = findViewById(R.id.et_apPresentation);
        etPCategory = findViewById(R.id.et_apCategory);
        etPRegisterDate = findViewById(R.id.et_apRegisterDate);
        etPDescription = findViewById(R.id.et_apDescription);
        rbYes = findViewById(R.id.rb_apYes);
        rbNo = findViewById(R.id.rb_apNo);
        Button btnSaveProduct = findViewById(R.id.btn_apSave);

        storageReference = FirebaseStorage.getInstance().getReference(Constantes.S_PRODUCT);

        // Al hacer clic en el ImageView abrimos la galeria del usuario
        imgProductPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        loadCurrentDate();

        // Al hacer clic en el Button llamamos al metodo registerUser
        btnSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerProduct();
            }
        });
    }

    // Abrimos la galeria del usuario
    private void openGallery() {
        // Validamos si tenemos los permisos de acceso a la galeria, solo para android 6 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(AddProduct.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddProduct.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                cropImagePicker(); // Llamamos al servico de CropImageActivity
            }
        } else {
            // Versiones anteriores a android 5.1 no necesitan solicitar permiso
            cropImagePicker(); // Llamamos al servico de CropImageActivity
        }
    }

    private void cropImagePicker() {
        // Configuramos el recorte de la imagen, aspecto cuadrado
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(AddProduct.this);
    }

    // Subimos a Firebase storage la imagen recortada
    private void uploadPhotoToStorage() {
        progressDialog = ProgressDialog.show(AddProduct.this,
                "Cargando imagen",
                "Espere por favor",
                true,
                false);

        if (imageUri != null) {
            // Referenciamos la ruta donde se guardara nuestra imagen (product_photo)
            StorageReference fileReference = storageReference.child(Constantes.getTimeInMilliSeconds() + Constantes.EXTENSION_JPG); // Le damos un nombre y su extension
            StorageTask task = fileReference.putFile(imageUri);

            // Servicio StorageTask para cargar la imagen
            task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    // Obtenemos la url de la imagen cargada
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    // Validamos si la tarea fue exitosa
                    if (task.isSuccessful()) {
                        // Tomamos el resultado de la tareas y la guardamos como URI
                        Uri downloadUri = task.getResult();

                        assert downloadUri != null;
                        // Nuestro resultado URI lo convertimos a String
                        productPhotoUrl = downloadUri.toString();

                        tvTitleAddImage.setVisibility(View.GONE); // Hide text
                        Glide.with(getApplicationContext())
                                .load(productPhotoUrl) // Cargamos nuestro String (Url)
                                .into(imgProductPhoto); // En nuestro ImageView

                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(AddProduct.this, "No se puedo cargar la imagen, intente de nuevo", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Metodo propio de android para recoger los datos de la actividad externa (CropImageActivity)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                // Result for Crop Image
                assert cropResult != null;
                imageUri = cropResult.getUri();

                tvTitleAddImage.setVisibility(View.GONE); // Hide Text
                imgProductPhoto.setImageURI(imageUri);

                uploadPhotoToStorage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert cropResult != null;
                Exception e = cropResult.getError();
                Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Metodo para optener la fecha actual
    private void loadCurrentDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = simpleDateFormat.format(date);
        etPRegisterDate.setText(currentDate);
    }

    // Registramos el producto
    private void registerProduct() {
        String prescription = "";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference productReference = database.getReference(Constantes.N_PRODUCT).push();

        String productId = productReference.getKey();
        String productName = etPName.getText().toString().trim();
        String productPresentation = etPPresentation.getText().toString().trim();
        String productCategory = etPCategory.getText().toString().trim();
        String productRegisterDate = etPRegisterDate.getText().toString().trim();
        String productDescription = etPDescription.getText().toString().trim();

        // Seteamos un valor de acuerdo al RadioButton seleccionado (Tiene o no receta medica)
        if (rbYes.isChecked()) {
            prescription = Constantes.YES;
        } else if (rbNo.isChecked()) {
            prescription = Constantes.NO;
        }

        // Validamos los campos obligatorios
        if (productName.isEmpty()) {
            etPName.setError("El nombre del producto es obligatorio");
            etPName.requestFocus();
        } else if (productPresentation.isEmpty()) {
            etPPresentation.setError("La presentación del prodcuto es obligatorio");
            etPPresentation.requestFocus();
        } else if (productCategory.isEmpty()) {
            etPCategory.setError("La categoría del producto es obligatorio");
            etPCategory.requestFocus();
        } else if (productPhotoUrl == null) {
            tvTitleAddImage.setText(getString(R.string.text_required_image));

            // Si el usuario no ha cargado una imagen, el background de nuestro ImageView se veré rojo
            imgProductPhoto.setBackgroundColor(getResources().getColor(R.color.colorRed));
            Toast.makeText(AddProduct.this, "La foto del producto es obligatorio", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog = ProgressDialog.show(AddProduct.this,
                    "Registrando producto",
                    "Espere por favor",
                    true,
                    false);

            HashMap<String, String> productValues = new HashMap<>();
            productValues.put(Constantes.P_ID, productId);
            productValues.put(Constantes.P_NAME, productName);
            productValues.put(Constantes.P_PRESENTATION, productPresentation);
            productValues.put(Constantes.P_CATEGORY, productCategory);
            productValues.put(Constantes.P_PRESCRIPTION, prescription);
            productValues.put(Constantes.P_REGISTER_DATE, productRegisterDate);
            productValues.put(Constantes.P_DESCRIPTION, productDescription);
            productValues.put(Constantes.P_PHOTO, productPhotoUrl);

            // Registramos los datos del producto en firebase database
            productReference.setValue(productValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(AddProduct.this, "Producto agregado con éxito", Toast.LENGTH_SHORT).show();
                        restartViews();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(AddProduct.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Limpiamos los edittext y el imageview
    private void restartViews() {
        imgProductPhoto.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        imgProductPhoto.setImageDrawable(null);
        tvTitleAddImage.setVisibility(View.VISIBLE);
        etPName.setText("");
        etPPresentation.setText("");
        etPCategory.setText("");
        rbYes.setChecked(true);
        etPDescription.setText("");
    }
}