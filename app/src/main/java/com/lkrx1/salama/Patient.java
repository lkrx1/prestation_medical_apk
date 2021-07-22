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
import com.lkrx1.salama.adapter.PatientAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Patient#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Patient extends Fragment {

    View view;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout refresh;
    private ArrayList<com.lkrx1.salama.model.Patient> patients = new ArrayList<>();
    private JsonObjectRequest objectRequest;
    private RecyclerView recyclerView;
    private Dialog dialog;
    private PatientAdapter patientAdapter;
    private ViewGroup vg_temp_pat;
    private TextView pat_state;

    private String url = "http://192.168.124.5:8080/api/rest/patients";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_patient, container, false);
        vg_temp_pat = container;
        refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipedown_pat);
        recyclerView = (RecyclerView) view.findViewById(R.id.patient);
        pat_state = view.findViewById(R.id.pat_state);

        dialog = new Dialog(this.vg_temp_pat.getContext());

        FloatingActionButton add_btn = (FloatingActionButton) view.findViewById(R.id.btn_add_patient);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPatient();
            }
        });

        refresh.setOnRefreshListener(this::onRefresh);
        refresh.post(new Runnable() {
            @Override
            public void run() {
                patients.clear();
                getData();
            }
        });

        ImageButton btn_search_patient = (ImageButton) view.findViewById(R.id.btn_search_patient);
        EditText et_search_patient = (EditText) view.findViewById(R.id.et_search_patient);
        btn_search_patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(et_search_patient.getText().toString());
            }
        });
        return view;
    }

    private void onRefresh() {
        patients.clear();
        getData();
    }

    private void addPatient() {
        TextView close, title;
        EditText et_nom_pat, et_adresse_pat;
        Button btn_submit_pat;

        dialog.setContentView(R.layout.activity_mod_patient);
        close = (TextView) dialog.findViewById(R.id.textClose);
        title = (TextView) dialog.findViewById(R.id.title_patient_mod);

        title.setText("Ajout d'un patient");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        et_nom_pat = (EditText) dialog.findViewById(R.id.et_nom_pat);
        et_adresse_pat = (EditText) dialog.findViewById(R.id.et_adresse_pat);
        btn_submit_pat = (Button) dialog.findViewById(R.id.btn_save_pat);

        btn_submit_pat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.lkrx1.salama.model.Patient pat = new com.lkrx1.salama.model.Patient(et_nom_pat.getText().toString(), et_adresse_pat.getText().toString());
                Submit(pat);
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void Submit(com.lkrx1.salama.model.Patient pat) {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                refresh.post(new Runnable() {
                    @Override
                    public void run() {
                        patients.clear();
                        getData();
                    }
                });
                Toast.makeText(vg_temp_pat.getContext(), "Ajout de patient avec succes.", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
//                Toast.makeText(vg_temp_pat.getContext(), "Erreur lors de l'ajout du patient.", Toast.LENGTH_LONG).show();
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
                String jsonString = "{\"nom\":\"" + pat.getNom() + "\", \"adresse\":\"" + pat.getAdresse() + "\"}";

                return jsonString.getBytes();
            }
        };
        Volley.newRequestQueue(vg_temp_pat.getContext()).add(request);
    }

    private void search(String item) {
        refresh.setRefreshing(true);
        objectRequest = new JsonObjectRequest(url + "/find?nom=" + item, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                patients.clear();
                try {
                    JSONArray array = response.getJSONArray("patient");
                    for (int i = 0; i < array.length(); i++) {
                        // Get current json object
                        JSONObject patient_temp = array.getJSONObject(i);

                        com.lkrx1.salama.model.Patient pat = new com.lkrx1.salama.model.Patient();
                        pat.setId(patient_temp.getInt("id"));
                        pat.setNom(patient_temp.getString("nom"));
                        pat.setAdresse(patient_temp.getString("adresse"));
                        patients.add(pat);
                    }

                    adapterPush(patients);
                    String state = (array.length() == 0) ? "Aucun patient n'a été trouvé." : ((array.length() == 1) ? "Un patient trouvé." : array.length() + " patients trouvés.");
                    pat_state.setText(state);
                    patientAdapter.notifyDataSetChanged();
                    refresh.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(vg_temp_pat.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    refresh.setRefreshing(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("erreur", error.getMessage());
                Toast.makeText(vg_temp_pat.getContext(), "Erreur lors de la recuperation des patients.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue = Volley.newRequestQueue(vg_temp_pat.getContext());
        requestQueue.add(objectRequest);
    }

    private void getData() {
        refresh.setRefreshing(true);
        objectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("patient");
                    for (int i = 0; i < array.length(); i++) {
                        // Get current json object
                        JSONObject patient_temp = array.getJSONObject(i);

                        com.lkrx1.salama.model.Patient pat = new com.lkrx1.salama.model.Patient();
                        pat.setId(patient_temp.getInt("id"));
                        pat.setNom(patient_temp.getString("nom"));
                        pat.setAdresse(patient_temp.getString("adresse"));
                        patients.add(pat);
                    }
                    String state = (array.length() == 0) ? "Aucun patient n'a été trouvé." : ((array.length() == 1) ? "Un patient trouvé." : array.length() + " patients trouvés.");
                    pat_state.setText(state);
                    adapterPush(patients);
                    refresh.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(vg_temp_pat.getContext(), "Erreur lors de la recuperation des patients.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue = Volley.newRequestQueue(vg_temp_pat.getContext());
        requestQueue.add(objectRequest);
    }

    private void adapterPush(ArrayList<com.lkrx1.salama.model.Patient> patients) {
        patientAdapter = new PatientAdapter(vg_temp_pat.getContext(), patients);
        recyclerView.setLayoutManager(new LinearLayoutManager(vg_temp_pat.getContext()));
        recyclerView.setAdapter(patientAdapter);
    }
}