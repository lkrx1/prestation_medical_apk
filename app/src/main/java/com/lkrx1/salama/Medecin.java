package com.lkrx1.salama;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lkrx1.salama.adapter.MedecinAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Medecin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Medecin extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    View view;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout refresh;
    private static ArrayList<com.lkrx1.salama.model.Medecin> medecins;
    private JsonObjectRequest objectRequest;
    private RecyclerView recyclerView;
    private Dialog dialog;
    private MedecinAdapter medecinAdapter;
    private ViewGroup vg_temp;
    private TextView mdc_state;

    private String url = "http://192.168.124.5:8080/api/rest/medecins";

    @Override
    public void onStart() {
        super.onStart();
        medecins = new ArrayList<com.lkrx1.salama.model.Medecin>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_medecin, container, false);

        vg_temp = container;
        refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipedown_mdc);
        recyclerView = (RecyclerView) view.findViewById(R.id.medecin);
        mdc_state = view.findViewById(R.id.mdc_state);

        dialog = new Dialog(this.vg_temp.getContext());

        FloatingActionButton add_btn = (FloatingActionButton) view.findViewById(R.id.btn_add_mdc);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMedecin();
            }
        });

        refresh.setOnRefreshListener(this::onRefresh);
        refresh.post(new Runnable() {
            @Override
            public void run() {
                medecins.clear();
                getData();
            }
        });

        ImageButton btn_search_medecin = (ImageButton) view.findViewById(R.id.btn_search_medecin);
        EditText et_search_medecin = (EditText) view.findViewById(R.id.et_search_medecin);
        btn_search_medecin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(et_search_medecin.getText().toString());
            }
        });
        return view;
    }

    private void search(String item) {
        refresh.setRefreshing(true);
        objectRequest = new JsonObjectRequest(url + "/find?nom=" + item, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                medecins.clear();
                try {
                    JSONArray array = response.getJSONArray("medecin");
                    for (int i = 0; i < array.length(); i++) {
                        // Get current json object
                        JSONObject medecin_temp = array.getJSONObject(i);

                        com.lkrx1.salama.model.Medecin mdc = new com.lkrx1.salama.model.Medecin();
                        mdc.setId(medecin_temp.getInt("id"));
                        mdc.setNom(medecin_temp.getString("nom"));
                        mdc.setTaux_journalier(Double.parseDouble(medecin_temp.getString("taux_journalier")));
                        medecins.add(mdc);
                    }

                    adapterPush(medecins);
                    String state = (array.length() == 0) ? "Aucun medecin n'a été trouvé." : ((array.length() == 1) ? "Un medecin trouvé." : array.length() + " medecins trouvés.");
                    mdc_state.setText(state);
                    medecinAdapter.notifyDataSetChanged();
                    refresh.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(vg_temp.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    refresh.setRefreshing(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("erreur", error.getMessage());
                Toast.makeText(vg_temp.getContext(), "Erreur lors de la recuperation des medecins.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue = Volley.newRequestQueue(vg_temp.getContext());
        requestQueue.add(objectRequest);
    }

    private void getData() {
        refresh.setRefreshing(true);
        objectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("medecin");
                    for (int i = 0; i < array.length(); i++) {
                        // Get current json object
                        JSONObject medecin_temp = array.getJSONObject(i);

                        com.lkrx1.salama.model.Medecin mdc = new com.lkrx1.salama.model.Medecin();
                        mdc.setId(medecin_temp.getInt("id"));
                        mdc.setNom(medecin_temp.getString("nom"));
                        mdc.setTaux_journalier(Double.parseDouble(medecin_temp.getString("taux_journalier")));
                        medecins.add(mdc);
                    }
                    String state = (array.length() == 0) ? "Aucun medecin n'a été trouvé." : ((array.length() == 1) ? "Un medecin trouvé." : array.length() + " medecins trouvés.");
                    mdc_state.setText(state);
                    adapterPush(medecins);
                    refresh.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.d("erreur", error.getMessage());
                Toast.makeText(vg_temp.getContext(), "Erreur lors de la recuperation des medecins.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue = Volley.newRequestQueue(vg_temp.getContext());
        requestQueue.add(objectRequest);
    }


    private void adapterPush(ArrayList<com.lkrx1.salama.model.Medecin> medecins) {
        medecinAdapter = new MedecinAdapter(vg_temp.getContext(), medecins);
        recyclerView.setLayoutManager(new LinearLayoutManager(vg_temp.getContext()));
        recyclerView.setAdapter(medecinAdapter);
    }

    public void addMedecin() {
        TextView close, title;
        EditText et_nom_mdc, et_tj_mdc;
        Button btn_submit_mdc;

        dialog.setContentView(R.layout.activity_mod_medecin);
        close = (TextView) dialog.findViewById(R.id.textClose);
        title = (TextView) dialog.findViewById(R.id.title_medecin_mod);

        title.setText("Ajout d'un medecin");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        et_nom_mdc = (EditText) dialog.findViewById(R.id.et_nom_mdc);
        et_tj_mdc = (EditText) dialog.findViewById(R.id.et_tj_mdc);
        btn_submit_mdc = (Button) dialog.findViewById(R.id.btn_save_mdc);

        btn_submit_mdc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.lkrx1.salama.model.Medecin mdc = new com.lkrx1.salama.model.Medecin(et_nom_mdc.getText().toString(), Double.parseDouble(et_tj_mdc.getText().toString()));
                Submit(mdc);
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void Submit(com.lkrx1.salama.model.Medecin mdc) {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                refresh.post(new Runnable() {
                    @Override
                    public void run() {
                        medecins.clear();
                        getData();
                    }
                });
                Toast.makeText(vg_temp.getContext(), "Ajout de medecin avec succes.", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
//                Toast.makeText(vg_temp.getContext(), "Erreur lors de l'ajout du medecin.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                return header;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
//                Log.d("our_data", data);
                String jsonString = "{\"nom\":\"" + mdc.getNom() + "\", \"taux_journalier\":\"" + mdc.getTaux_journalier() + "\"}";
                return jsonString.getBytes();
            }
        };
        Volley.newRequestQueue(vg_temp.getContext()).add(request);
    }

    @Override
    public void onRefresh() {
        medecins.clear();
        getData();
    }
}