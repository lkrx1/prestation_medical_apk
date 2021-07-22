package com.lkrx1.salama;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.lkrx1.salama.adapter.TraitementAdapter;
import com.lkrx1.salama.model.Medecin;
import com.lkrx1.salama.model.Patient;
import com.lkrx1.salama.model.TraitementInput;
import com.lkrx1.salama.model.TraitementView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Traitement#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Traitement extends Fragment {

    View view;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout refresh;
    private ArrayList<TraitementInput> traitements = new ArrayList<>();
    public ArrayList<Patient> list_patients = new ArrayList<>();
    public ArrayList<Medecin> list_medecins = new ArrayList<>();
    private JsonObjectRequest objectRequest;
    private RecyclerView recyclerView;
    private Dialog dialog;
    private TraitementAdapter traitementAdapter;
    private ViewGroup vg_temp;
    private Double total_recette = 0.00;
    private TextView val_total_recette;
    Medecin mdc_search_selected = new Medecin();

    private String url = "http://192.168.124.5:8080/api/rest/traitements";
    private String url_for_mdc = "http://192.168.124.5:8080/api/rest/medecins";
    private String url_for_pat = "http://192.168.124.5:8080/api/rest/patients";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_traitement, container, false);

        vg_temp = container;
        refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipedown_traitement);
        recyclerView = (RecyclerView) view.findViewById(R.id.traitement);

        val_total_recette = (TextView) view.findViewById(R.id.val_total_recette);

        dialog = new Dialog(this.vg_temp.getContext());
        list_medecins.clear();
        list_patients.clear();
        get_all_medecins();
        get_all_patients();

        FloatingActionButton add_btn = (FloatingActionButton) view.findViewById(R.id.btn_add_traitement);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTraitement();
            }
        });

        refresh.setOnRefreshListener(this::onRefresh);
        refresh.post(new Runnable() {
            @Override
            public void run() {
                traitements.clear();
                getData(mdc_search_selected);
            }
        });

        return view;
    }

    public void onRefresh() {
        traitements.clear();
        getData(mdc_search_selected);
    }

    private void init_spinner_medecin(ArrayList<Medecin> _liste_medecins) {
        Spinner sp_mdc_search_traitement = (Spinner) view.findViewById(R.id.sp_mdc_search_traitement);
        TextView val_tx_j_mdc = (TextView) view.findViewById(R.id.val_tx_j_mdc);
        ArrayList<String> nom_medecins_search = new ArrayList<String>();
        for (int i = 0; i < _liste_medecins.size(); i++) {
            nom_medecins_search.add("Dr. " + _liste_medecins.get(i).getNom());
        }
        ArrayAdapter<String> mdc_search_adapter = new ArrayAdapter<String>(vg_temp.getContext(), android.R.layout.simple_spinner_item, nom_medecins_search);
        sp_mdc_search_traitement.setAdapter(mdc_search_adapter);
        traitements.clear();
        //  getData(mdc_search_selected);
        sp_mdc_search_traitement.setSelection(search_mdc_position(_liste_medecins, mdc_search_selected.getId()));
        sp_mdc_search_traitement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Medecin mdc_temp = _liste_medecins.get(i);
                mdc_search_selected.setId(mdc_temp.getId());
                mdc_search_selected.setNom(mdc_temp.getNom());
                mdc_search_selected.setTaux_journalier(mdc_temp.getTaux_journalier());
                val_tx_j_mdc.setText("" + mdc_search_selected.getTaux_journalier() + " Ar");
                getData(mdc_search_selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private int search_mdc_position(ArrayList<Medecin> lists, int id_mdc) {
        int result = 0;
        int i = 0;
        for (Medecin medecin : lists) {
            if (id_mdc == medecin.getId()) result = i;
            i++;
        }
        return result;
    }

    private void getData(Medecin mdc) {
        refresh.setRefreshing(true);
        Log.i("url---", url + "/medecin/" + String.valueOf(mdc.getId()));
        objectRequest = new JsonObjectRequest(url + "/medecin/" + String.valueOf(mdc.getId()), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    traitements.clear();
                    JSONArray array = response.getJSONArray("traitement");
                    total_recette = 0.00;
                    for (int i = 0; i < array.length(); i++) {
                        // Get current json object
                        JSONObject traitement_temp = array.getJSONObject(i);
                        JSONObject mdc_temp = traitement_temp.getJSONObject("medecin");
                        JSONObject pat_temp = traitement_temp.getJSONObject("patient");

                        TraitementInput traitement_input = new TraitementInput();
                        traitement_input.setId(traitement_temp.getInt("id"));
                        traitement_input.setMedecin(new Medecin(mdc_temp.getInt("id"), mdc_temp.getString("nom"), mdc_temp.getDouble("taux_journalier")));
                        traitement_input.setPatient(new Patient(pat_temp.getInt("id"), pat_temp.getString("nom"), pat_temp.getString("adresse")));
                        traitement_input.setNbjour(traitement_temp.getInt("nb_jour"));

                        Double prix_prestation = traitement_input.getMedecin().getTaux_journalier() * traitement_input.getNbjour();
                        TraitementView traitementView = new TraitementView(traitement_input.getId(), traitement_input.getMedecin().getNom(), traitement_input.getPatient().getNom(), prix_prestation, traitement_input.getNbjour());
                        total_recette += prix_prestation;
                        traitements.add(traitement_input);
                    }
                    adapterPush(traitements);
                    val_total_recette.setText("" + total_recette + " Ar");
                    refresh.setRefreshing(false);
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

    private void adapterPush(ArrayList<TraitementInput> traitements) {
        traitementAdapter = new TraitementAdapter(vg_temp.getContext(), traitements, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(vg_temp.getContext()));
        recyclerView.setAdapter(traitementAdapter);
    }

    public void get_all_medecins() {
        list_medecins.clear();
        objectRequest = new JsonObjectRequest(url_for_mdc, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("medecin");
                    for (int i = 0; i < array.length(); i++) {
                        // Get current json object
                        JSONObject medecin_temp = array.getJSONObject(i);
                        Medecin mdc = new Medecin(medecin_temp.getInt("id"), medecin_temp.getString("nom"), medecin_temp.getDouble("taux_journalier"));
                        list_medecins.add(mdc);
                    }
                    init_spinner_medecin(list_medecins);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(vg_temp.getContext(), "Erreur lors de la recuperation des medecins.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue = Volley.newRequestQueue(vg_temp.getContext());
        requestQueue.add(objectRequest);
    }

    public void get_all_patients() {
        list_patients.clear();
        objectRequest = new JsonObjectRequest(url_for_pat, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("patient");
                    for (int i = 0; i < array.length(); i++) {
                        // Get current json object
                        JSONObject patient_temp = array.getJSONObject(i);
                        Patient pat = new Patient(patient_temp.getInt("id"), patient_temp.getString("nom"), patient_temp.getString("adresse"));
                        list_patients.add(pat);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(vg_temp.getContext(), "Erreur lors de la recuperation des medecins.", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue = Volley.newRequestQueue(vg_temp.getContext());
        requestQueue.add(objectRequest);
    }

    private void addTraitement() {
        TextView close, title;
        Button btn_submit_traitement;
        EditText et_nbjour_traitement;
        Spinner sp_mdc, sp_pat;
        Medecin mdc_selected = new Medecin();
        Patient pat_selected = new Patient();

        dialog.setContentView(R.layout.activity_mod_traitement);
        close = (TextView) dialog.findViewById(R.id.textClose);
        title = (TextView) dialog.findViewById(R.id.title_traitement_mod);

        title.setText("Ajout d'un traitement");
        sp_mdc = (Spinner) dialog.findViewById(R.id.sp_mdc);
        sp_pat = (Spinner) dialog.findViewById(R.id.sp_pat);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ArrayList<String> nom_medecins = new ArrayList<String>();
        for (int i = 0; i < list_medecins.size(); i++) {
            nom_medecins.add("Dr. " + list_medecins.get(i).getNom());
            //Log.i("adapter", "Dr. " + list_patients.get(i).getNom());
        }
        ArrayAdapter<String> mdc_adapter = new ArrayAdapter<String>(vg_temp.getContext(), android.R.layout.simple_spinner_item, nom_medecins);
        sp_mdc.setAdapter(mdc_adapter);
        sp_mdc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Medecin mdc_temp = list_medecins.get(i);
                mdc_selected.setId(mdc_temp.getId());
                mdc_selected.setNom(mdc_temp.getNom());
                mdc_selected.setTaux_journalier(mdc_temp.getTaux_journalier());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayList<String> nom_patients = new ArrayList<String>();
        for (int i = 0; i < list_patients.size(); i++) {
            nom_patients.add(list_patients.get(i).getNom());
        }
        ArrayAdapter<String> pat_adapter = new ArrayAdapter<String>(vg_temp.getContext(), android.R.layout.simple_spinner_item, nom_patients);
        sp_pat.setAdapter(pat_adapter);
        sp_pat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Patient pat_temp = list_patients.get(i);
                pat_selected.setId(pat_temp.getId());
                pat_selected.setNom(pat_temp.getNom());
                pat_selected.setAdresse(pat_temp.getAdresse());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        et_nbjour_traitement = (EditText) dialog.findViewById(R.id.et_nbjour_traitement);
        btn_submit_traitement = (Button) dialog.findViewById(R.id.btn_save_traitement);

        btn_submit_traitement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.lkrx1.salama.model.Traitement traitement = new com.lkrx1.salama.model.Traitement(mdc_selected.getId(), pat_selected.getId(), Integer.parseInt(et_nbjour_traitement.getText().toString()));
                Submit(traitement);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void Submit(com.lkrx1.salama.model.Traitement traitement) {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                refresh.post(new Runnable() {
                    @Override
                    public void run() {
                        traitements.clear();
                        getData(mdc_search_selected);
                    }
                });
                Toast.makeText(vg_temp.getContext(), "Ajout du traitement avec succes.", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(vg_temp.getContext(), "Erreur lors de l'ajout du traitement.", Toast.LENGTH_LONG).show();
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
                String jsonString = "{\"id_medecin\":\"" + traitement.getId_medecin() + "\", \"id_patient\":\"" + traitement.getId_patient() + "\", \"nbJour\":\"" + traitement.getNb_jour() + "\"}";

                return jsonString.getBytes();
            }
        };
        Volley.newRequestQueue(vg_temp.getContext()).add(request);
    }
}