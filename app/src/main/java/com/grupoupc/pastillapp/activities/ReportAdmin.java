package com.grupoupc.pastillapp.activities;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grupoupc.pastillapp.R;
import com.grupoupc.pastillapp.adapters.ReportAdapter;
import com.grupoupc.pastillapp.models.Report;
import com.grupoupc.pastillapp.utils.APIClient.ICountAPI;
import com.grupoupc.pastillapp.utils.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportAdmin extends AppCompatActivity {

    private ICountAPI iCountAPI;
    private ProgressDialog progressDialog;

    private List<Report> reportList;
    private RecyclerView recyclerReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_admin);

        recyclerReport = findViewById(R.id.recyclerReport);

        reportList = new ArrayList<>();

        iCountAPI = Constantes.getVisitsServices();
        loadDataFromApi();
    }

    private void loadDataFromApi() {
        String countRequestApi;

        progressDialog = ProgressDialog.show(ReportAdmin.this,
                "Cargando reporte",
                "Espere por favor",
                true,
                false);

        try {
            countRequestApi = "https://app-sistemas-distribuidos.herokuapp.com/metro-app-service/usuario/listarvisitas";
            iCountAPI.getPath(countRequestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    try {
                        assert response.body() != null;
                        JSONObject responseObject = new JSONObject(response.body());
                        JSONArray indicador = responseObject.getJSONArray("indicador");

                        for (int i = 0; i < indicador.length(); i++) {
                            JSONObject object = indicador.getJSONObject(i);
                            String fecVisita = object.getString("fecvisita");
                            String cantVisita = object.getString("cantvisita");

                            Report report = new Report(fecVisita, cantVisita);
                            reportList.add(report);

                            // Ordenamos nuestra lista
                            Collections.sort(reportList); // ordena de menor a mayor
                            Collections.reverse(reportList); // "damos vuelta a la lista" se mostrará de mayor a menor (fecha más reciente a las más antigua)

                            setReportAdapter(reportList);
                            progressDialog.dismiss();
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setReportAdapter(List<Report> reportList) {
        LinearLayoutManager manager = new LinearLayoutManager(ReportAdmin.this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        ReportAdapter reportAdapter = new ReportAdapter(ReportAdmin.this, reportList);
        reportAdapter.notifyDataSetChanged();
        recyclerReport.setLayoutManager(manager);
        recyclerReport.setAdapter(reportAdapter);
    }

}