package com.grupoupc.pastillapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.models.User;
import com.grupoupc.pastillapp.utils.Constantes;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private CircleImageView civPhoto;
    private TextView txtUsername, txtEmail;
    private EditText et_username;

    private FirebaseUser mUser;
    private DatabaseReference drUserReference;

    private String profilePhotoUrl;
    private Uri profileUri = null;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        civPhoto = findViewById(R.id.civ_pPhoto);
        LinearLayout llViewReport = findViewById(R.id.ll_viewReport);
        txtUsername = findViewById(R.id.txt_pUsername);
        txtEmail = findViewById(R.id.txt_pEmail);
        ImageView imgEdit = findViewById(R.id.img_edit);
        ImageView imgSave = findViewById(R.id.img_save);
        et_username = findViewById(R.id.et_username);
        Button btnLogout = findViewById(R.id.btn_pLogout);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        drUserReference = FirebaseDatabase.getInstance().getReference(Constantes.N_USER).child(mUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference(Constantes.S_USER);

        // Ocultamos opcion "Ver reporte" (esra opcion es solo para admin)
        Constantes.validateUserType(llViewReport);

        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtUsername.setVisibility(View.GONE);
                et_username.setVisibility(View.VISIBLE);

                imgSave.setVisibility(View.VISIBLE);
                imgEdit.setVisibility(View.GONE);
            }
        });

        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtUsername.setVisibility(View.VISIBLE);
                et_username.setVisibility(View.GONE);

                imgSave.setVisibility(View.GONE);
                imgEdit.setVisibility(View.VISIBLE);

                updateUsername();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Profile.this, Login.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });

        civPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constantes.openGallery(Profile.this);
            }
        });

        llViewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, ReportAdmin.class));
            }
        });

        loadUserData();
    }

    // Cargamos los datos del usuario
    private void loadUserData() {
        ProgressDialog progressDialog = ProgressDialog.show(Profile.this,
                "Obteniendo datos del usuario",
                "Espere por favor",
                true,
                false);

        drUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                txtUsername.setText(user.getUsername());
                txtEmail.setText(user.getEmail());
                et_username.setText(user.getUsername());

                if (user.getPhoto().equals(Constantes.DEFAULT)) {
                    civPhoto.setImageResource(R.drawable.logo_app);
                } else {
                    Glide.with(getApplicationContext())
                            .load(user.getPhoto())
                            .into(civPhoto);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Subimos a Firebase storage la imagen recortada
    private void uploadPhotoToStorage() {
        ProgressDialog progressDialog = ProgressDialog.show(Profile.this,
                "Cargando imagen",
                "Espere por favor",
                true,
                false);

        if (profileUri != null) {
            // Referenciamos la ruta donde se guardara nuestra imagen (product_photo)
            StorageReference fileReference = storageReference.child(mUser.getUid() + Constantes.EXTENSION_JPG); // Le damos un nombre y su extension
            StorageTask task = fileReference.putFile(profileUri);

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
                        profilePhotoUrl = downloadUri.toString();

                        Glide.with(getApplicationContext())
                                .load(profilePhotoUrl) // Cargamos nuestro String (Url)
                                .into(civPhoto); // En nuestro ImageView

                        // Seteamos la url de la foto a nuestra bd
                        drUserReference.child(Constantes.U_PHOTO).setValue(profilePhotoUrl);

                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(Profile.this, "No se puedo cargar la imagen, intente de nuevo", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUsername(){
        ProgressDialog progressDialog = ProgressDialog.show(Profile.this,
                "Actualizando nombre",
                "Espere por favor",
                true,
                false);

        HashMap<String, Object> username = new HashMap<>();
        username.put(Constantes.U_NAME, et_username.getText().toString().trim());
        drUserReference.updateChildren(username);

        progressDialog.dismiss();
        Toast.makeText(this, "Nombre actualizado con exito!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                profileUri = cropResult.getUri();
                civPhoto.setImageURI(profileUri);

                uploadPhotoToStorage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception e = cropResult.getError();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}