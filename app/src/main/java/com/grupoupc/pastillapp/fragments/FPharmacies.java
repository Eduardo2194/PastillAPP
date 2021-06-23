package com.grupoupc.pastillapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.activities.AddPharmacy;
import com.grupoupc.pastillapp.adapters.PharmacyAdapter;
import com.grupoupc.pastillapp.models.Pharmacy;
import com.grupoupc.pastillapp.utils.Constantes;

import java.util.ArrayList;
import java.util.List;

public class FPharmacies extends Fragment {

    private RecyclerView recyclerPharmacy;
    private List<Pharmacy> pharmacyList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fView = inflater.inflate(R.layout.fragment_pharmacies, container, false);

        recyclerPharmacy = fView.findViewById(R.id.recyclerPharmacy);
        FloatingActionButton fabAddPharmacy = fView.findViewById(R.id.fab_addPharmacy);

        pharmacyList = new ArrayList<>();

        fabAddPharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddPharmacy.class));
            }
        });

        loadPharmacies();

        // Validate if user is Admin
        Constantes.validateUserType(fabAddPharmacy);

        return  fView;
    }

    private void loadPharmacies() {
        DatabaseReference pharmacyReference = FirebaseDatabase.getInstance().getReference(Constantes.N_PHARMACY);
        pharmacyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pharmacyList.clear();
                for (DataSnapshot pSnapshot: snapshot.getChildren()){
                    Pharmacy pharmacy = pSnapshot.getValue(Pharmacy.class);
                    pharmacyList.add(pharmacy);

                    setPharmacyAdapter(pharmacyList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setPharmacyAdapter(List<Pharmacy> pharmacyList) {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(getActivity(), pharmacyList);
        pharmacyAdapter.notifyDataSetChanged();
        recyclerPharmacy.setLayoutManager(manager);
        recyclerPharmacy.setAdapter(pharmacyAdapter);
    }

}