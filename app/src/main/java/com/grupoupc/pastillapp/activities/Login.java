package com.grupoupc.pastillapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.utils.Constantes;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    //carga la pantalla para ingresar los datos del logueo
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constantes.setFullScreen(Login.this);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_lEmail);
        etPassword = findViewById(R.id.et_lPassword);
        Button btnLogin = findViewById(R.id.btn_lLogin);
        Button btnCreateAccount = findViewById(R.id.btn_lCreateAccount);
        Button btnForgotPassword = findViewById(R.id.btn_lForgotPassword);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
                finish();
            }
        });

        //boton que nos envia a la pantalla de recuperar contrase??a
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, RecoverPassword.class));
                finish();
            }
        });
    }

    // Iniciando sesi??n con usuario y contrase??a
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("El correo elect??nico es obligatorio");
            etEmail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un correo electr??nico v??lido");
            etEmail.requestFocus();
        } else if (password.isEmpty()) {
            etPassword.setError("La contrase??a es obligatoria");
            etPassword.requestFocus();
        } else if (password.length() < 6) {
            etPassword.setError("La contrase??a debe tener al menos 6 caracteres");
            etPassword.requestFocus();
        } else {
            ProgressDialog progressDialog = ProgressDialog.show(Login.this,
                    "Iniciando sesi??n",
                    "Espere por favor",
                    true,
                    false);

            // Utilizamos el servicio de Firebase Auth
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(Login.this, Home.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(Login.this, "Ocurri?? un error, intente de nuevo por favor.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Auto login, valida si el usuario ya ha iniciado sesi??n y se va directo a la pantalla principal
        if (mUser != null) {
            startActivity(new Intent(Login.this, Home.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Salimos de la aplicaci??n cuando estemos en esta pantamma HOME
        Intent close = new Intent(Intent.ACTION_MAIN);
        close.addCategory(Intent.CATEGORY_HOME);
        close.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(close);
    }
}