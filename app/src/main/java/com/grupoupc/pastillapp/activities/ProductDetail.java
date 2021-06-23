package com.grupoupc.pastillapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.adapters.AvailableAdapter;
import com.grupoupc.pastillapp.models.Available;
import com.grupoupc.pastillapp.models.Pharmacy;
import com.grupoupc.pastillapp.utils.Constantes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ProductDetail extends AppCompatActivity {

    private String pId;
    private ImageView imgProduct;
    private TextView txtName, txtCategory, txtDescription, txtPresentation, txtRegisterDate, txtPrescription;
    private String pharmacySelectedId, pharmacySelectedName;

    private RecyclerView recyclerAvailable;
    private List<Available> availableList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getDecorView().getWindowInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
        setContentView(R.layout.activity_product_detail);

        imgProduct = findViewById(R.id.img_pdPhoto);
        txtName = findViewById(R.id.tv_pdName);
        txtCategory = findViewById(R.id.tv_pdCategory);
        txtDescription = findViewById(R.id.tv_pdDescription);
        txtPresentation = findViewById(R.id.tv_pdPresentation);
        txtRegisterDate = findViewById(R.id.tv_pdRegisterDate);
        txtPrescription = findViewById(R.id.tv_pdPrescription);
        LinearLayout llActions = findViewById(R.id.ll_actions);
        Button btnAddPharmacy = findViewById(R.id.btn_AddPharmacy);

        recyclerAvailable = findViewById(R.id.recyclerAvailable);
        availableList = new ArrayList<>();

        Constantes.validateUserType(llActions); //Hide actions buttons if user isn't Admin

        btnAddPharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogAddPharmacy();
            }
        });

        loadProductDetail();
        loadPharmaciesAvailable();
    }

    // Cargamos los datos del producto
    private void loadProductDetail() {
        pId = getIntent().getStringExtra(Constantes.P_ID);
        String pName = getIntent().getStringExtra(Constantes.P_NAME);
        String pCategory = getIntent().getStringExtra(Constantes.P_CATEGORY);
        String pDescription = getIntent().getStringExtra(Constantes.P_DESCRIPTION);
        String pPresentation = getIntent().getStringExtra(Constantes.P_PRESENTATION);
        String pRegisterDate = getIntent().getStringExtra(Constantes.P_REGISTER_DATE);
        String pPrescription = getIntent().getStringExtra(Constantes.P_PRESCRIPTION);
        String pPhoto = getIntent().getStringExtra(Constantes.P_PHOTO);

        // Mostramos u ocultamos el elemento si esta o no vacío
        if (pDescription.isEmpty()) {
            txtDescription.setVisibility(View.GONE);
        } else {
            txtDescription.setVisibility(View.VISIBLE);
            txtDescription.setText(pDescription);
        }

        if (pRegisterDate.isEmpty()) {
            txtRegisterDate.setVisibility(View.GONE);
        } else {
            txtRegisterDate.setVisibility(View.VISIBLE);
            String full_register_date = getString(R.string.text_register_date) + ": " + pRegisterDate;
            txtRegisterDate.setText(full_register_date);
        }

        if (pPrescription.equals(Constantes.NO)) {
            txtPrescription.setVisibility(View.GONE);
        } else {
            txtPrescription.setVisibility(View.VISIBLE);
        }

        Glide.with(getApplicationContext())
                .load(pPhoto)
                .into(imgProduct);
        txtName.setText(pName);
        txtCategory.setText(pCategory);

        String full_presentation = getString(R.string.text_presentation) + ": " + pPresentation;
        txtPresentation.setText(full_presentation);
    }

    // Si el admin hace clic en agregar farmacia (para este producto) se abrirá un dialog
    private void openDialogAddPharmacy() {
        ArrayList<String> pharmacyName = new ArrayList<>();
        ArrayList<Pharmacy> pharmacyArrayList = new ArrayList<>();

        AlertDialog.Builder pharmacyDialog = new AlertDialog.Builder(ProductDetail.this);
        LayoutInflater inflater = ProductDetail.this.getLayoutInflater();
        View dialog_add_pharmacy = inflater.inflate(R.layout.dialog_add_pharmacy, null);

        // Inicalizamos los elementos del dialog
        AutoCompleteTextView acPharmaciesList = dialog_add_pharmacy.findViewById(R.id.ac_pharmaciesList);
        EditText etProductPrice = dialog_add_pharmacy.findViewById(R.id.et_productPrice);
        Button btnSavePharmacy = dialog_add_pharmacy.findViewById(R.id.btn_savePharmacy);

        pharmacyDialog.setView(dialog_add_pharmacy);
        AlertDialog dialog = pharmacyDialog.create();

        // Llenamos nuestro DropDownMenu con data de firebase
        DatabaseReference pharmacyReference = FirebaseDatabase.getInstance().getReference(Constantes.N_PHARMACY);
        Query query = pharmacyReference.orderByChild(Constantes.P_NAME);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsPharmacy : snapshot.getChildren()) {
                    Pharmacy pharmacy = dsPharmacy.getValue(Pharmacy.class);

                    assert pharmacy != null;
                    pharmacyName.add(pharmacy.getName());
                    pharmacyArrayList.add(pharmacy);
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_pharmacy_dropdown_menu, pharmacyName);
                arrayAdapter.setDropDownViewResource(R.layout.item_pharmacy_dropdown_menu);
                acPharmaciesList.setAdapter(arrayAdapter);

                acPharmaciesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedId = pharmacyArrayList.get(position).getId();
                        String selectedName = pharmacyArrayList.get(position).getName();

                        pharmacySelectedId = selectedId;
                        pharmacySelectedName = selectedName;
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnSavePharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Guardamos los datos en firebase database
                saveInfo(pharmacySelectedId, pharmacySelectedName, acPharmaciesList, etProductPrice, dialog);
            }
        });

        dialog.show();
    }

    private void saveInfo(String pharmacySelectedId, String pharmacySelectedName, AutoCompleteTextView acPharmaciesList,
                          EditText etProductPrice, AlertDialog dialog) {
        DatabaseReference pharmacyReference = FirebaseDatabase.getInstance().getReference(Constantes.N_AVAILABLE)
                .child(pId)
                .child(this.pharmacySelectedId);

        String productPrice = etProductPrice.getText().toString();

        if (this.pharmacySelectedName.isEmpty()) {
            acPharmaciesList.setError("Debe eligir una farmacia");
        } else if (productPrice.isEmpty()) {
            etProductPrice.setError("El precio es obligatorio");
        } else {
            ProgressDialog progressDialog = ProgressDialog.show(ProductDetail.this,
                    "Asignando farmacia",
                    "Espere por favor",
                    true,
                    false);

            Float priceFloat = Float.parseFloat(productPrice);
            DecimalFormat df = new DecimalFormat("0.00");
            df.setMaximumFractionDigits(2);
            productPrice = df.format(priceFloat);

            HashMap<String, String> values = new HashMap<>();
            values.put(Constantes.A_PH_ID, pharmacySelectedId);
            values.put(Constantes.A_PH_NAME, pharmacySelectedName);
            values.put(Constantes.A_PR_PRICE, productPrice);

            // Guardamos información en el nodo AVAILABLE
            pharmacyReference.setValue(values).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        dialog.dismiss();
                        Toast.makeText(ProductDetail.this, "Farmacia asignada con éxito", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ProductDetail.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Guardamos información en el nodo PHARMACY_PRODUCT
            FirebaseDatabase.getInstance().getReference().child(Constantes.N_PHARMACY_PRODUCT)
                    .child(pharmacySelectedId)
                    .child(pId)
                    .setValue(true);
        }
    }

    // Gargamos las farmacias asignadas a este producto
    private void loadPharmaciesAvailable() {
        DatabaseReference availableReference = FirebaseDatabase.getInstance().getReference(Constantes.N_AVAILABLE)
                .child(pId);

        // Ordemos los resultados por el precio de menor a mayor
        Query query = availableReference.orderByChild(Constantes.A_PR_PRICE);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                availableList.clear();
                for(DataSnapshot dsAvailable: snapshot.getChildren()) {
                    Available available = dsAvailable.getValue(Available.class);
                    availableList.add(available);

                    setAvailableAdapter(availableList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Configuracion del Adapter
    private void setAvailableAdapter(List<Available> availableList) {
        LinearLayoutManager manager = new LinearLayoutManager(ProductDetail.this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        AvailableAdapter availableAdapter = new AvailableAdapter(ProductDetail.this, availableList);
        availableAdapter.notifyDataSetChanged();
        recyclerAvailable.setLayoutManager(manager);
        recyclerAvailable.setAdapter(availableAdapter);
    }
}