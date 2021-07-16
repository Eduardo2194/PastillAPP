package com.grupoupc.pastillapp.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.grupoupc.pastillapp.models.User;
import com.grupoupc.pastillapp.utils.Constantes;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ProductDetail extends AppCompatActivity {

    private String pId, pName, pCategory, pDescription, pPresentation, pRegisterDate, pPrescription, pPhoto, back;
    private ImageView imgProduct;
    private TextView txtName, txtCategory, txtDescription, txtPresentation, txtRegisterDate, txtPrescription;
    private String pharmacySelectedId, pharmacySelectedName;

    private RecyclerView recyclerAvailable;
    private List<Available> availableList;

    static ProductDetail INSTANCE;

    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        INSTANCE = this;
        super.onCreate(savedInstanceState);
        Constantes.setFullScreen(ProductDetail.this);
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
        Button btnUpdateProduct = findViewById(R.id.btn_EditProduct);

        recyclerAvailable = findViewById(R.id.recyclerAvailable);
        availableList = new ArrayList<>();

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference(Constantes.N_USER).child(mUser.getUid());

        Constantes.validateUserType(llActions); //Hide actions buttons if user isn't Admin

        pId = getIntent().getStringExtra(Constantes.P_ID);
        pName = getIntent().getStringExtra(Constantes.P_NAME);
        pCategory = getIntent().getStringExtra(Constantes.P_CATEGORY);
        pDescription = getIntent().getStringExtra(Constantes.P_DESCRIPTION);
        pPresentation = getIntent().getStringExtra(Constantes.P_PRESENTATION);
        pRegisterDate = getIntent().getStringExtra(Constantes.P_REGISTER_DATE);
        pPrescription = getIntent().getStringExtra(Constantes.P_PRESCRIPTION);
        pPhoto = getIntent().getStringExtra(Constantes.P_PHOTO);
        back = getIntent().getStringExtra("back");

        btnUpdateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProductDetail.this, AddProduct.class);
                i.putExtra("action", "update");
                i.putExtra(Constantes.P_ID, pId);
                i.putExtra(Constantes.P_PHOTO, pPhoto);
                i.putExtra(Constantes.P_NAME, pName);
                i.putExtra(Constantes.P_PRESENTATION, pPresentation);
                i.putExtra(Constantes.P_CATEGORY, pCategory);
                i.putExtra(Constantes.P_PRESCRIPTION, pPrescription);
                i.putExtra(Constantes.P_REGISTER_DATE, pRegisterDate);
                i.putExtra(Constantes.P_DESCRIPTION, pDescription);
                startActivity(i);
            }
        });

        btnAddPharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogAddPharmacy();
            }
        });

        loadProductDetail();
        loadPharmaciesAvailable();
        setCountVisits();
    }

    // Cargamos los datos del producto
    private void loadProductDetail() {
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
        DatabaseReference pharmacyReference = null;
        if(this.pharmacySelectedId==null){
            Toast.makeText(ProductDetail.this, "Debe eligir una farmacia", Toast.LENGTH_SHORT).show();
            //acPharmaciesList.setError("Debe eligir una farmacia");
        }else {
            pharmacyReference = FirebaseDatabase.getInstance().getReference(Constantes.N_AVAILABLE)
                    .child(pId)
                    .child(this.pharmacySelectedId);
        }
        String productPrice = etProductPrice.getText().toString();

        if (this.pharmacySelectedName==null) {
            Toast.makeText(ProductDetail.this, "Debe eligir una farmacia", Toast.LENGTH_SHORT).show();
            //acPharmaciesList.setError("Debe eligir una farmacia");
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
                for (DataSnapshot dsAvailable : snapshot.getChildren()) {
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

    public static ProductDetail getActivityInstance() {
        return INSTANCE;
    }

    public String getProductName() {
        return this.pName;
    }

    private void setCountVisits() {
        ProgressDialog progressDialog = ProgressDialog.show(this,
                "Cargando información",
                "Espere por favor",
                true,
                false);

        // Obtenemos la fecha actual
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String fecvisita = dateFormat.format(calendar.getTime());

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                if (!user.getType().equals(Constantes.ADMIN)) {
                    // Si es admin no contabiliza las visitas
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String requestJson = "{\n" +
                                        "\t\"visitas\":{\n" +
                                        "\t\t\"fecvisita\": \"" + fecvisita + "\",\n" +
                                        "\t\t\"cantvisita\": \"" + Constantes.ONE + "\"\n" +
                                        "\t}\n" +
                                        "}";

                                HttpHeaders headers = new HttpHeaders();
                                headers.setContentType(MediaType.APPLICATION_JSON);
                                Log.i("POST", requestJson);

                                HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
                                ResponseEntity<String> output = new RestTemplate().exchange(Constantes.VISITS_API_BASE_URL, HttpMethod.POST, entity, String.class);

                                Log.i("POST", output.toString());
                                progressDialog.dismiss();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (back.equals("home")) {
            startActivity(new Intent(ProductDetail.this, Home.class));
            finish();
        }
    }
}