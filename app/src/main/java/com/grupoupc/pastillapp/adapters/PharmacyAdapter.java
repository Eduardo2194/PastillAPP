package com.grupoupc.pastillapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.activities.PharmacyDetail;
import com.grupoupc.pastillapp.models.Pharmacy;
import com.grupoupc.pastillapp.utils.Constantes;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PharmacyAdapter extends RecyclerView.Adapter<PharmacyAdapter.PharmacyViewHolder> {

    Context context;
    List<Pharmacy> pharmacyList;

    public PharmacyAdapter(Context context, List<Pharmacy> pharmacyList) {
        this.context = context;
        this.pharmacyList = pharmacyList;
    }

    @NonNull
    @Override
    public PharmacyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PharmacyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_pharmacy, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PharmacyAdapter.PharmacyViewHolder holder, int position) {
        Pharmacy pos = pharmacyList.get(position);

        Glide.with(context)
                .load(pos.getPhoto())
                .into(holder.civPharmacyPhoto);

        holder.tvPharmacyName.setText(pos.getName());
        String open_close = context.getString(R.string.text_business_hours) + ": " + pos.getOpen() + " - " + pos.getClose();
        holder.tvPharmacyOC.setText(open_close);

        holder.cardPharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PharmacyDetail.class);
                i.putExtra(Constantes.PH_ID, pos.getId());
                i.putExtra(Constantes.PH_NAME, pos.getName());
                i.putExtra(Constantes.PH_OPEN, pos.getOpen());
                i.putExtra(Constantes.PH_CLOSE, pos.getClose());
                i.putExtra(Constantes.PH_PHONE, pos.getPhone());
                i.putExtra(Constantes.PH_ADDRESS, pos.getAddress());
                i.putExtra(Constantes.PH_PHOTO, pos.getPhoto());
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pharmacyList.size();
    }

    public static class PharmacyViewHolder extends RecyclerView.ViewHolder {
        CardView cardPharmacy;
        CircleImageView civPharmacyPhoto;
        TextView tvPharmacyName, tvPharmacyOC;

        public PharmacyViewHolder(@NonNull View itemView) {
            super(itemView);

            cardPharmacy = itemView.findViewById(R.id.card_pharmacy);
            civPharmacyPhoto = itemView.findViewById(R.id.civ_pharmacyPhoto);
            tvPharmacyName = itemView.findViewById(R.id.tv_pharmacyName);
            tvPharmacyOC = itemView.findViewById(R.id.tv_pharmacyOC);
        }
    }
}
