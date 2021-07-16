package com.grupoupc.pastillapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddProduct extends AppCompatActivity {
    private ImageView imgProductPhoto;
    private TextView tvTitleAddImage;
    private EditText etPName, etPPresentation, etPCategory, etPRegisterDate, etPDescription;
    private RadioButton rbYes, rbNo;
    private String productPhotoUrl;

    private ProgressDialog progressDialog;
    private Uri imageUri = null;

    private StorageReference storageReference;
    private String PRODUCT_ID;

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
                Constantes.openGallery(AddProduct.this);
            }
        });

        String PRODUCT_ACTION = getIntent().getStringExtra("action");
        PRODUCT_ID = getIntent().getStringExtra(Constantes.P_ID);

        switch (PRODUCT_ACTION) {
            case "add": {
                loadCurrentDate();
                btnSaveProduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        registerProduct();
                    }
                });
            }
            break;
            case "update": {
                btnSaveProduct.setText(getString(R.string.text_update_product));
                loadProductDetails();
                btnSaveProduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateProduct();
                    }
                });
            }
        }
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

    // Recogemos los datos del producto a actualizar
    private void loadProductDetails() {
        tvTitleAddImage.setVisibility(View.GONE);
        String pName = getIntent().getStringExtra(Constantes.P_NAME);
        String pCategory = getIntent().getStringExtra(Constantes.P_CATEGORY);
        String pDescription = getIntent().getStringExtra(Constantes.P_DESCRIPTION);
        String pPresentation = getIntent().getStringExtra(Constantes.P_PRESENTATION);
        String pRegisterDate = getIntent().getStringExtra(Constantes.P_REGISTER_DATE);
        String pPrescription = getIntent().getStringExtra(Constantes.P_PRESCRIPTION);
        String pPhoto = getIntent().getStringExtra(Constantes.P_PHOTO);

        Glide.with(getApplicationContext())
                .load(pPhoto)
                .into(imgProductPhoto);

        productPhotoUrl = pPhoto;
        etPName.setText(pName);
        etPPresentation.setText(pPresentation);
        etPCategory.setText(pCategory);
        etPRegisterDate.setText(pRegisterDate);
        etPDescription.setText(pDescription);

        if (pPrescription.equals(Constantes.YES)) {
            rbYes.setChecked(true);
        } else {
            rbNo.setChecked(true);
        }
    }

    // Registramos el producto
    private void registerProduct() {
        String prescription = "";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference productReference = database.getReference(Constantes.N_PRODUCT).push(); // Obtenemos el ID del producto registrado

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
                    if (task.isSuccessful()) {
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

    // Actualizamos producto
    private void updateProduct() {
        String prescription = "";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference productReference = database.getReference(Constantes.N_PRODUCT).child(PRODUCT_ID);

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
                    "Actualizando producto",
                    "Espere por favor",
                    true,
                    false);

            Map<String, Object> productValuesUpdate = new HashMap<>();
            productValuesUpdate.put(Constantes.P_NAME, productName);
            productValuesUpdate.put(Constantes.P_PRESENTATION, productPresentation);
            productValuesUpdate.put(Constantes.P_CATEGORY, productCategory);
            productValuesUpdate.put(Constantes.P_PRESCRIPTION, prescription);
            productValuesUpdate.put(Constantes.P_REGISTER_DATE, productRegisterDate);
            productValuesUpdate.put(Constantes.P_DESCRIPTION, productDescription);
            productValuesUpdate.put(Constantes.P_PHOTO, productPhotoUrl);

            String productPrescription = prescription;
            productReference.updateChildren(productValuesUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(AddProduct.this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(AddProduct.this, ProductDetail.class);
                        i.putExtra("back", "home");
                        i.putExtra(Constantes.P_ID, PRODUCT_ID);
                        i.putExtra(Constantes.P_PHOTO, productPhotoUrl);
                        i.putExtra(Constantes.P_NAME, productName);
                        i.putExtra(Constantes.P_PRESENTATION, productPresentation);
                        i.putExtra(Constantes.P_CATEGORY, productCategory);
                        i.putExtra(Constantes.P_PRESCRIPTION, productPrescription);
                        i.putExtra(Constantes.P_REGISTER_DATE, productRegisterDate);
                        i.putExtra(Constantes.P_DESCRIPTION, productDescription);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
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