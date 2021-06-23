package com.grupoupc.pastillapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grupoupc.pastillapp.R;
//import com.grupoupc.pastillapp.activities.PharmacyLocation;
import com.grupoupc.pastillapp.models.Available;
import com.grupoupc.pastillapp.utils.Constantes;

import java.util.List;

public class AvailableAdapter extends RecyclerView.Adapter<AvailableAdapter.AvailableViewHolder> {

    Context context;
    List<Available> availableList;

    public AvailableAdapter(Context context, List<Available> availableList) {
        this.context = context;
        this.availableList = availableList;
    }

    @NonNull
    @Override
    public AvailableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AvailableViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_row_available, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AvailableAdapter.AvailableViewHolder holder, int position) {
        if (holder.getAdapterPosition() == 0) {
            // CABECERA de nuestra tabla

            holder.txtName.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.txtPrice.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.txtAddress.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));

            holder.txtName.setText(context.getString(R.string.text_pharmacy));
            holder.txtPrice.setText(context.getString(R.string.text_price));
            holder.txtAddress.setText(context.getString(R.string.text_location));

        } else {
            // Filas de nuestra tabla
            Available available = availableList.get(holder.getAdapterPosition() - 1);
            holder.txtName.setText(available.getPharmacy_name());
            holder.txtPrice.setText(available.getProduct_price());
            holder.txtAddress.setText(context.getString(R.string.text_view_address));

            holder.llAvailable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent i = new Intent(context, PharmacyLocation.class);
                    //i.putExtra(Common.PH_ID, available.getPharmacy_id());
                    //context.startActivity(i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return availableList.size() + 1; // +1 for the header table
    }

    public static class AvailableViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llAvailable;
        TextView txtName, txtPrice, txtAddress;

        public AvailableViewHolder(@NonNull View itemView) {
            super(itemView);

            llAvailable = itemView.findViewById(R.id.ll_available);
            txtName = itemView.findViewById(R.id.tv_pharmacyName);
            txtPrice = itemView.findViewById(R.id.tv_pharmacyPrice);
            txtAddress = itemView.findViewById(R.id.tv_pharmacyAddress);
        }
    }
}
