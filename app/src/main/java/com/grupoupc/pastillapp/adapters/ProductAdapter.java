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
import com.grupoupc.pastillapp.activities.ProductDetail;
import com.grupoupc.pastillapp.models.Product;
import com.grupoupc.pastillapp.utils.Constantes;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    Context context;
    List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_list_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ProductViewHolder holder, int position) {
        Product pos = productList.get(position);

        Glide.with(context)
                .load(pos.getPhoto())
                .into(holder.civProductPhoto);

        holder.tvProductName.setText(pos.getName());

        holder.cardProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ProductDetail.class);
                i.putExtra(Constantes.P_ID, pos.getId());
                i.putExtra(Constantes.P_NAME, pos.getName());
                i.putExtra(Constantes.P_CATEGORY, pos.getCategory());
                i.putExtra(Constantes.P_DESCRIPTION, pos.getDescription());
                i.putExtra(Constantes.P_PRESENTATION, pos.getPresentation());
                i.putExtra(Constantes.P_REGISTER_DATE, pos.getRegister_date());
                i.putExtra(Constantes.P_PRESCRIPTION, pos.getPrescription());
                i.putExtra(Constantes.P_PHOTO, pos.getPhoto());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Para la BUSQUEDA de productos
    public void filterList(List<Product> productsArrayList) {
        productList = productsArrayList;
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        CardView cardProduct;
        CircleImageView civProductPhoto;
        TextView tvProductName;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            cardProduct = itemView.findViewById(R.id.card_product);
            civProductPhoto = itemView.findViewById(R.id.civ_productPhoto);
            tvProductName = itemView.findViewById(R.id.tv_productName);
        }
    }
}
