package com.grupoupc.pastillapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.activities.AddProduct;
import com.grupoupc.pastillapp.adapters.ProductAdapter;
import com.grupoupc.pastillapp.models.Product;
import com.grupoupc.pastillapp.utils.Constantes;

import java.util.ArrayList;
import java.util.List;

public class FProducts extends Fragment {

    private RecyclerView recyclerProduct;
    private List<Product> productList;
    private EditText etSearchProducts;

    private DatabaseReference productReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fView = inflater.inflate(R.layout.fragment_products, container, false);

        etSearchProducts = fView.findViewById(R.id.et_searchProduct);
        recyclerProduct = fView.findViewById(R.id.recyclerProduct);
        FloatingActionButton fabAddProduct = fView.findViewById(R.id.fab_addProduct);

        productReference = FirebaseDatabase.getInstance().getReference(Constantes.N_PRODUCT);

        productList = new ArrayList<>();

        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddProduct.class));
            }
        });

        loadProducts();

        // Validate if user is Admin
        Constantes.validateUserType(fabAddProduct);

        return fView;
    }

    private void loadProducts() {
        Query queryProducts = productReference.orderByChild(Constantes.P_NAME);

        queryProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot pSnapshot : snapshot.getChildren()) {
                    Product product = pSnapshot.getValue(Product.class);
                    productList.add(product);

                    setProductAdapter(productList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Config edittext for search products
        etSearchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProducts(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchProducts(String keyword) {
        ArrayList<Product> productFilter = new ArrayList<>();
        Query queryProducts = productReference.orderByChild(Constantes.P_NAME);

        queryProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot dsSearch : snapshot.getChildren()) {
                    Product product = dsSearch.getValue(Product.class);

                    assert product != null;
                    if (product.getName().toLowerCase().contains(keyword) || product.getCategory().toLowerCase().contains(keyword)
                            || product.getDescription().toLowerCase().contains(keyword)) {
                        productFilter.add(product);
                    }

                    setProductAdapter(productFilter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProductAdapter(List<Product> productList) {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        ProductAdapter productAdapter = new ProductAdapter(getActivity(), productList);
        productAdapter.notifyDataSetChanged();
        recyclerProduct.setLayoutManager(manager);
        recyclerProduct.setAdapter(productAdapter);

        productAdapter.filterList(productList);
    }

}