package com.grupoupc.pastillapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.adapters.HomeViewPagerAdapter;
import com.grupoupc.pastillapp.fragments.FPharmacies;
import com.grupoupc.pastillapp.fragments.FProducts;
import com.grupoupc.pastillapp.models.User;
import com.grupoupc.pastillapp.utils.Constantes;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {

    private CircleImageView civProfile;
    private TextView txtUsername;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FProducts fProducts;
    private FPharmacies fPharmacies;

    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        civProfile = findViewById(R.id.civ_hProfile);
        txtUsername = findViewById(R.id.tv_hUsername);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        fProducts = new FProducts();
        fPharmacies = new FPharmacies();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        civProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Profile.class));
            }
        });

        loadUserData();
        configViewPager();
    }

    // Cargamos los datos del usuario como el nombre y su foto
    private void loadUserData() {
        ProgressDialog progressDialog = ProgressDialog.show(this,
                "Cargando información",
                "Espere por favor",
                true,
                false);

        // Referenciamos el nodo USER de firebase database
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(Constantes.N_USER).child(mUser.getUid());
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                String txtHi = "Hola " + user.getUsername() + "!";
                txtUsername.setText(txtHi);

                // Validamos si el usuario cambio su foto
                if (user.getPhoto().equals(Constantes.DEFAULT)) {
                    civProfile.setImageResource(R.drawable.logo_app);
                } else {
                    Glide.with(getApplicationContext())
                            .load(user.getPhoto())
                            .into(civProfile);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Configuramos las opciones del Tab Layout
    private void configViewPager() {
        tabLayout.setupWithViewPager(viewPager);
        HomeViewPagerAdapter homeViewPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager(), 0);
        homeViewPagerAdapter.addFragment(fProducts, getString(R.string.text_products));
        homeViewPagerAdapter.addFragment(fPharmacies, getString(R.string.text_pharmacies));
        viewPager.setAdapter(homeViewPagerAdapter);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.ic_products);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.ic_pharmacies);

    }
}