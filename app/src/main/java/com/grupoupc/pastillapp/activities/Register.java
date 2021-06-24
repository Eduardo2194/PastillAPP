package com.grupoupc.pastillapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.utils.Constantes;
import com.grupoupc.pastillapp.utils.Constantes;

import java.util.HashMap;
import java.util.Objects;
import java.util.logging.ConsoleHandler;

public class Register extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 o superior
            final WindowInsetsController insetsController = getWindow().getDecorView().getWindowInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        } else { // Menor a android 11
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
        setContentView(R.layout.activity_register);

        ImageView imgBack = findViewById(R.id.img_back);
        etUsername = findViewById(R.id.et_rUsername);
        etEmail = findViewById(R.id.et_rEmail);
        etPassword = findViewById(R.id.et_rPassword);
        etConfirmPassword = findViewById(R.id.et_rConfirmPassword);
        Button btnRegister = findViewById(R.id.btn_rCreateAccount);
        Button btnHaveAccount = findViewById(R.id.btn_rHaveAccount);

        mAuth = FirebaseAuth.getInstance();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToLoginScreen();
            }
        });

        btnHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToLoginScreen();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    // Registramos usuario
    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm_password = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("El nombre es obligatorio");
            etUsername.requestFocus();
        } else if (email.isEmpty()) {
            etEmail.setError("El correo electrónico es obligatorio");
            etEmail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un correo electrónico válido");
            etEmail.requestFocus();
        } else if (password.isEmpty()) {
            etPassword.setError("La contraseña es obligatoria");
            etPassword.requestFocus();
        } else if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
        } else if (!confirm_password.equals(password)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
        } else {
            progressDialog = ProgressDialog.show(Register.this,
                    "Registrando usuario",
                    "Espere por favor",
                    true,
                    false);

            // Registramos usuario (email, contraseña) con el servicio de Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String uId = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();

                                // Registramos los datos del usuario en Firebase Realtime database
                                createUserInDatabase(uId, username, email);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(Register.this, "Ocurrió un error, intente de nuevo", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createUserInDatabase(String uId, String username, String email) {
        // Creamos usuario en Firebase Realtime Database
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(Constantes.N_USER).child(uId);
        HashMap<String, String> userValues = new HashMap<>();
        userValues.put(Constantes.U_ID, uId);
        userValues.put(Constantes.U_NAME, username);
        userValues.put(Constantes.U_EMAIL, email);
        userValues.put(Constantes.U_PHOTO, Constantes.DEFAULT);
        userValues.put(Constantes.U_TYPE, Constantes.DEFAULT);

        userReference.setValue(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(Register.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Register.this, Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(Register.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Accion de volver atras
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Especificamos que queremos que haga cuando el usario retroceda
        backToLoginScreen();
    }

    private void backToLoginScreen() {
        // Regresamos a la pantalla de login
        startActivity(new Intent(Register.this, Login.class));
        finish();
    }
}