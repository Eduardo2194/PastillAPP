package com.grupoupc.pastillapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.models.Report;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    Context context;
    List<Report> reportList;

    public ReportAdapter(Context context, List<Report> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReportViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_row_report, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ReportViewHolder holder, int position) {
        if (holder.getAdapterPosition() == 0) {
            // HEADER TABLE
            holder.txtPos.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.txtDate.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.txtCount.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));

            holder.txtPos.setText(context.getString(R.string.header_table_pos));
            holder.txtDate.setText(context.getString(R.string.header_table_date));
            holder.txtCount.setText(context.getString(R.string.header_table_count));
        }else{
            // ROWS TABLE
            Report Report = reportList.get(holder.getAdapterPosition() - 1);

            // Ponemos en negrita la primera fila de nuestra tabla (Fecha actual)
            if (holder.getAdapterPosition() == 1) {
                holder.txtPos.setTypeface(holder.txtPos.getTypeface(), Typeface.BOLD);
                holder.txtDate.setTypeface(holder.txtDate.getTypeface(), Typeface.BOLD);
                holder.txtCount.setTypeface(holder.txtCount.getTypeface(), Typeface.BOLD);
            }

            holder.txtPos.setText(String.valueOf(position));
            holder.txtDate.setText(Report.getFecvisita());
            holder.txtCount.setText(Report.getCantvisita());
        }
    }

    @Override
    public int getItemCount() {
        return reportList.size() + 1; // +1 (header table)
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llReport;
        TextView txtPos, txtDate, txtCount;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            llReport = itemView.findViewById(R.id.ll_report);
            txtPos = itemView.findViewById(R.id.tv_position);
            txtDate = itemView.findViewById(R.id.tv_date);
            txtCount = itemView.findViewById(R.id.tv_count);
        }
    }
}
