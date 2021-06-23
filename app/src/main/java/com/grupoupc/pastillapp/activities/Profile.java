package com.grupoupc.pastillapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.models.User;
import com.grupoupc.pastillapp.utils.Constantes;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    CircleImageView civPhoto;
    TextView txtUsername, txtEmail;

    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        civPhoto = findViewById(R.id.civ_pPhoto);
        txtUsername = findViewById(R.id.txt_pUsername);
        txtEmail = findViewById(R.id.txt_pEmail);
        Button btnLogout = findViewById(R.id.btn_pLogout);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Profile.this, Login.class));
                finish();
            }
        });

        loadUserData();
    }

    // Cargamos los datos del usuario
    private void loadUserData() {
        // Referenciamos al nodo USER de firebase database, y solo mostramos los datos del usuario actual
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(Constantes.N_USER).child(mUser.getUid());

        ProgressDialog progressDialog = ProgressDialog.show(Profile.this,
                "Obteniendo datos del usuario",
                "Espere por favor",
                true,
                false);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                txtUsername.setText(user.getUsername());
                txtEmail.setText(user.getEmail());

                if (user.getPhoto().equals(Constantes.DEFAULT)){
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
}