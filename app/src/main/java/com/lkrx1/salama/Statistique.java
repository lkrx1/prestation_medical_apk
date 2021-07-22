package com.lkrx1.salama;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lkrx1.salama.model.Medecin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Statistique#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Statistique extends Fragment {

    View view;
    private ViewGroup vg_temp;
    private JsonObjectRequest objectRequest;
    private RequestQueue requestQueue;
    private static String TAG = "Statistiques";
    private ArrayList<com.lkrx1.salama.model.Statistique> statistiques = new ArrayList<com.lkrx1.salama.model.Statistique>();
    private ArrayList<Float> yData = new ArrayList<Float>();
    private ArrayList<String> xData = new ArrayList<String>();
    private String url = "http://192.168.124.5:8080/api/rest/statistiques";
    private int effectif_total = 0;
    PieChart pieChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_statistique, container, false);
        vg_temp = container;
        getData();
        return view;
    }

    private void initDataSet() {
        Log.d(TAG, "onCreate: starting to create chart");

        pieChart = (PieChart) view.findViewById(R.id.salama_diagramme);

        pieChart.getDescription().setText("Statistiques des medecins.");
        pieChart.setRotationEnabled(true);
        //pieChart.setUsePercentValues(true);
        //pieChart.setHoleColor(Color.BLUE);
        //pieChart.setCenterTextColor(Color.BLACK);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("SALAMA");
        pieChart.setCenterTextSize(10);
        //pieChart.setDrawEntryLabels(true);
        //pieChart.setEntryLabelTextSize(20);
        //More options just check out the documentation!
        addDataSet();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "onValueSelected: Value select from chart.");
                Log.d(TAG, "onValueSelected: " + e.toString());
                Log.d(TAG, "onValueSelected: " + h.toString());

                int pos1 = e.toString().indexOf("Entry, ");
                String sales = e.toString().substring(pos1 + 17);

                for (int i = 0; i < yData.size(); i++) {
                    if (yData.get(i) == Float.parseFloat(sales)) {
                        pos1 = i;
                        break;
                    }
                }
                String medecin = xData.get(pos1 + 1);
                Toast.makeText(vg_temp.getContext(), "" + medecin + "\n" + "Pourcentage: " + sales + "%, effectif: " + effectif_total + ".", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void addDataSet() {
        Log.d(TAG, "addDataSet started");
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for (int i = 0; i < yData.size(); i++) {
            yEntrys.add(new PieEntry(yData.get(i), i));
        }

        for (int i = 1; i < xData.size(); i++) {
            xEntrys.add(xData.get(i));
        }

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Medecins");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);

        pieDataSet.setColors(colors);

        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void getData() {
        objectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("statistique");
                    for (int i = 0; i < array.length(); i++) {
                        // Get current json object
                        JSONObject _statistique = array.getJSONObject(i);
                        JSONObject mdc_temp = _statistique.getJSONObject("medecin");

                        com.lkrx1.salama.model.Statistique statistique = new com.lkrx1.salama.model.Statistique();
                        statistique.setId(_statistique.getInt("id"));
                        statistique.setMedecin(new Medecin(mdc_temp.getInt("id"), mdc_temp.getString("nom"), mdc_temp.getDouble("taux_journalier")));
                        statistique.setEffectif(_statistique.getInt("effectif"));
                        statistique.setPercent(_statistique.getDouble("percent"));

                        yData.add(statistique.getPercent().floatValue());
                        xData.add("Dr. " + statistique.getMedecin().getNom());
                        effectif_total += statistique.getEffectif();
                        statistiques.add(statistique);
                    }
                    initDataSet();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
//                Toast.makeText(vg_temp.getContext(), "Erreur lors de la recuperation des traitements.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue = Volley.newRequestQueue(vg_temp.getContext());
        requestQueue.add(objectRequest);
    }
}