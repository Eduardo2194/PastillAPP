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
import com.google.firebase.auth.FirebaseAuth;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.utils.Constantes;

public class RecoverPassword extends AppCompatActivity {

    private EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constantes.setFullScreen(RecoverPassword.this);
        setContentView(R.layout.activity_recover_password);

        etEmail = findViewById(R.id.et_fEmail);
        Button btnSendEmail = findViewById(R.id.btn_fSendEmail);

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
            }
        });
    }

    // Metodo para enviar email con el link para recuperar contraseña
    private void recoverPassword() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("El correo electrónico es obligatorio");
            etEmail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un correo electrónico válido");
            etEmail.requestFocus();
        } else {
            ProgressDialog progressDialog = ProgressDialog.show(RecoverPassword.this,
                    "Enviando correo de recuperación",
                    "Espere por favor",
                    true,
                    false);

            // Enviamos link para recuperar la contraseña
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(RecoverPassword.this, "Correo enviado con éxito!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RecoverPassword.this, Login.class));
                                finish();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(RecoverPassword.this, "Ocurrió un error, intente de nuevo por favor.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(RecoverPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(RecoverPassword.this, Login.class));
        finish();
    }
}