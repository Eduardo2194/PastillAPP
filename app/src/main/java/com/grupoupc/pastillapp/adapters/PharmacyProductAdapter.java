package com.grupoupc.pastillapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.activities.ProductDetail;
import com.grupoupc.pastillapp.models.Product;
import com.grupoupc.pastillapp.utils.Constantes;

import java.util.List;

public class PharmacyProductAdapter extends RecyclerView.Adapter<PharmacyProductAdapter.PharmacyProductViewHolder> {

    Context context;
    List<Product> productList;

    public PharmacyProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public PharmacyProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PharmacyProductViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_grid_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PharmacyProductAdapter.PharmacyProductViewHolder holder, int position) {
        Product pos = productList.get(position);

        Glide.with(context)
                .load(pos.getPhoto())
                .into(holder.imgProduct);

        holder.cardProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ProductDetail.class);
                i.putExtra("back","pharmacy");
                i.putExtra(Constantes.P_ID, pos.getId());
                i.putExtra(Constantes.P_NAME, pos.getName());
                i.putExtra(Constantes.P_CATEGORY, pos.getCategory());
                i.putExtra(Constantes.P_DESCRIPTION, pos.getDescription());
                i.putExtra(Constantes.P_PRESENTATION, pos.getPresentation());
                i.putExtra(Constantes.P_REGISTER_DATE, pos.getRegister_date());
                i.putExtra(Constantes.P_PRESCRIPTION, pos.getPrescription());
                i.putExtra(Constantes.P_PHOTO, pos.getPhoto());
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class PharmacyProductViewHolder extends RecyclerView.ViewHolder {
        CardView cardProduct;
        ImageView imgProduct;

        public PharmacyProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProduct = itemView.findViewById(R.id.card_gridProduct);
            imgProduct = itemView.findViewById(R.id.img_productAvailable);
        }
    }
}
