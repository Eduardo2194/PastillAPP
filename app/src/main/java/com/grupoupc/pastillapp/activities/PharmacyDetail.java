package com.grupoupc.pastillapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.adapters.PharmacyAdapter;
import com.grupoupc.pastillapp.adapters.PharmacyProductAdapter;
import com.grupoupc.pastillapp.models.Product;
import com.grupoupc.pastillapp.utils.Constantes;

import java.util.ArrayList;
import java.util.List;

public class PharmacyDetail extends AppCompatActivity {

    private String pId;
    private ImageView imgPharmacy;
    private TextView txtName, txtOpenClose, txtPhone, txtAddress;

    private List<String> productsAvailableList;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_detail);

        imgPharmacy = findViewById(R.id.civ_pdPhoto);
        txtName = findViewById(R.id.txt_pdName);
        txtOpenClose = findViewById(R.id.txt_pdOpenClose);
        txtPhone = findViewById(R.id.txt_pdPhone);
        txtAddress = findViewById(R.id.txt_pdAddress);

        pId = getIntent().getStringExtra(Constantes.PH_ID);
        productList = new ArrayList<>();

        loadPharmacyDetail();
    }

    private void loadPharmacyDetail() {
        String name = getIntent().getStringExtra(Constantes.PH_NAME);
        String open = getIntent().getStringExtra(Constantes.PH_OPEN);
        String close = getIntent().getStringExtra(Constantes.PH_CLOSE);
        String phone = getIntent().getStringExtra(Constantes.PH_PHONE);
        String address = getIntent().getStringExtra(Constantes.PH_ADDRESS);
        String photo = getIntent().getStringExtra(Constantes.PH_PHOTO);

        Glide.with(getApplicationContext())
                .load(photo)
                .into(imgPharmacy);

        txtName.setText(name);
        txtPhone.setText(phone);
        txtAddress.setText(address);

        String open_close = getString(R.string.text_business_hours) + ": " + open + " - " + close;
        txtOpenClose.setText(open_close);
    }

    private void readProductsAvailable() {
        productsAvailableList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constantes.N_PHARMACY_PRODUCT)
                .child(pId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsAvailable: snapshot.getChildren()){
                    productsAvailableList.add(dsAvailable.getKey());
                }

                loadProductsAvailable();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadProductsAvailable() {
        DatabaseReference productReference = FirebaseDatabase.getInstance().getReference(Constantes.N_PRODUCT);
        productReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot dsProducts: snapshot.getChildren()){
                    Product product = dsProducts.getValue(Product.class);
                    for (String id: productsAvailableList){

                        assert product != null;
                        if (product.getId().equals(id)){
                            productList.add(product);
                        }
                    }

                    setProductsAdapter(productList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProductsAdapter(List<Product> productList) {
        RecyclerView recyclerProducts = findViewById(R.id.recyclerProducts);
        LinearLayoutManager manager = new GridLayoutManager(this, 3);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        PharmacyProductAdapter pharmacyProductAdapter = new PharmacyProductAdapter(this, productList);
        pharmacyProductAdapter.notifyDataSetChanged();
        recyclerProducts.setLayoutManager(manager);
        recyclerProducts.setAdapter(pharmacyProductAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        readProductsAvailable();
    }
}