package com.lkrx1.salama.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lkrx1.salama.R;
import com.lkrx1.salama.model.Medecin;
import com.lkrx1.salama.model.Patient;
import com.lkrx1.salama.model.Traitement;
import com.lkrx1.salama.model.TraitementInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TraitementAdapter extends RecyclerView.Adapter<TraitementAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<TraitementInput> traitements;
    private com.lkrx1.salama.Traitement my_fragment;
    private EditText et_nbJour;
    private String url = "http://192.168.124.5:8080/api/rest/traitements";
    private String url_for_mdc = "http://192.168.124.5:8080/api/rest/medecins";
    private String url_for_pat = "http://192.168.124.5:8080/api/rest/patients";

    public TraitementAdapter(Context context, ArrayList<TraitementInput> traitements, com.lkrx1.salama.Traitement fragment) {
        this.context = context;
        this.traitements = traitements;
        this.my_fragment = fragment;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.traitement_list, parent, false);

        return new TraitementAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.id.setText(String.valueOf(traitements.get(position).getId()));
        holder.nom_patient.setText(traitements.get(position).getPatient().getNom());
        holder.nom_medecin.setText("Dr. " + traitements.get(position).getMedecin().getNom());
        holder.nb_jour.setText(String.valueOf(traitements.get(position).getNbjour()) + "jours (" + (traitements.get(position).getNbjour() * traitements.get(position).getMedecin().getTaux_journalier()) + " Ar)");

        holder.editTraitement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTraitement(new Traitement(traitements.get(position).getId(), traitements.get(position).getMedecin().getId(), traitements.get(position).getPatient().getId(), traitements.get(position).getNbjour()), position);
            }
        });
        holder.deleteTraitement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = traitements.get(position).getId();
                deleteTraitement(id, position);
            }
        });
    }

    private void deleteTraitement(int id, int position) {
        TextView close, title;
        Button btn_confirm_delete_traitement, btn_cancel_delete_traitement;

        final Dialog dialog;
        dialog = new Dialog(context);

        dialog.setContentView(R.layout.delete_traitement);

        close = (TextView) dialog.findViewById(R.id.textClose);
        title = (TextView) dialog.findViewById(R.id.title_traitement_mod);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_confirm_delete_traitement = (Button) dialog.findViewById(R.id.btn_confirm_delete_traitement);
        btn_cancel_delete_traitement = (Button) dialog.findViewById(R.id.btn_cancel_delete_traitement);

        btn_confirm_delete_traitement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Submit("DELETE", null, dialog, id, position);
            }
        });

        btn_cancel_delete_traitement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void editTraitement(Traitement current_traitement, int position) {
        TextView close, title, et_nbjour_traitement;
        Spinner sp_mdc, sp_pat;
        Button submit;
        Medecin mdc_selected = new Medecin();
        Patient pat_selected = new Patient();
        int mdc_toEdit_position = 0;
        int pat_toEdit_position = 0;
        final Dialog dialog;

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.activity_mod_traitement);

        close = (TextView) dialog.findViewById(R.id.textClose);
        title = (TextView) dialog.findViewById(R.id.title_traitement_mod);
        title.setText("Modification d'un traitement");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        sp_mdc = (Spinner) dialog.findViewById(R.id.sp_mdc);
        sp_pat = (Spinner) dialog.findViewById(R.id.sp_pat);

        ArrayList<String> nom_medecins = new ArrayList<String>();
        for (int i = 0; i < my_fragment.list_medecins.size(); i++) {
            nom_medecins.add("Dr. " + my_fragment.list_medecins.get(i).getNom());
            //Log.i("adapter", "Dr. " + list_patients.get(i).getNom());
        }
        ArrayAdapter<String> mdc_adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, nom_medecins);
        sp_mdc.setAdapter(mdc_adapter);
        sp_mdc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                com.lkrx1.salama.model.Medecin mdc_temp = my_fragment.list_medecins.get(i);
                mdc_selected.setId(mdc_temp.getId());
                mdc_selected.setNom(mdc_temp.getNom());
                mdc_selected.setTaux_journalier(mdc_temp.getTaux_journalier());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayList<String> nom_patients = new ArrayList<String>();
        for (int i = 0; i < my_fragment.list_patients.size(); i++) {
            nom_patients.add(my_fragment.list_patients.get(i).getNom());
        }
        ArrayAdapter<String> pat_adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, nom_patients);
        sp_pat.setAdapter(pat_adapter);
        sp_pat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Patient pat_temp = my_fragment.list_patients.get(i);
                pat_selected.setId(pat_temp.getId());
                pat_selected.setNom(pat_temp.getNom());
                pat_selected.setAdresse(pat_temp.getAdresse());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        et_nbJour = (EditText) dialog.findViewById(R.id.et_nbjour_traitement);
        submit = (Button) dialog.findViewById(R.id.btn_save_traitement);

        sp_mdc.setSelection(search_mdc_position(current_traitement.getId_medecin()));
        sp_pat.setSelection(search_pat_position(current_traitement.getId_patient()));
        et_nbJour.setText(String.valueOf(current_traitement.getNb_jour()));

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Traitement final_traitement = new Traitement(mdc_selected.getId(), pat_selected.getId(), Integer.parseInt(et_nbJour.getText().toString()));
                Submit("PUT", final_traitement, dialog, current_traitement.getId(), position);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private int search_mdc_position(int id_mdc) {
        int result = 0;
        int i = 0;
        for (Medecin medecin : my_fragment.list_medecins) {
            if (id_mdc == medecin.getId()) result = i;
            i++;
        }
        return result;
    }

    private int search_pat_position(int id_pat) {
        int result = 0;
        int i = 0;
        for (Patient patient : my_fragment.list_patients) {
            if (id_pat == patient.getId()) result = i;
            i++;
        }
        return result;
    }

    private void Submit(String method, Traitement data, Dialog dialog, int id, final int position) {
        if (method == "PUT") {
            StringRequest request = new StringRequest(Request.Method.PUT, url + "/" + id, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dialog.dismiss();
                    my_fragment.onRefresh();
                    Toast.makeText(context, "Traitement modifié avec succes.", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(context, "Erreur lors de la modification du traitement.", Toast.LENGTH_LONG).show();
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
                    String jsonString = "{\"id_medecin\":\"" + data.getId_medecin() + "\",\"id_patient\":\"" + data.getId_patient() + "\",\"nbJour\":\"" + data.getNb_jour() + "\"}";
                    return jsonString.getBytes();
                }
            };
            Volley.newRequestQueue(context).add(request);
        } else if (method == "DELETE") {
            StringRequest request = new StringRequest(Request.Method.DELETE, url + "/" + id, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    traitements.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, traitements.size());
                    dialog.dismiss();
                    Toast.makeText(context, "La suppression du traitement a bien ete effectuee.", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(context, "Erreur lors du traitement du medecin.", Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            Volley.newRequestQueue(context).add(request);
        }
    }

    @Override
    public int getItemCount() {
        return traitements.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView id, nom_medecin, nom_patient, nb_jour;
        private ImageView editTraitement, deleteTraitement;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.id_traitement);
            nom_medecin = (TextView) itemView.findViewById(R.id.nom_medecin);
            nom_patient = (TextView) itemView.findViewById(R.id.nom_patient);
            nb_jour = (TextView) itemView.findViewById(R.id.nb_jour);

            editTraitement = (ImageView) itemView.findViewById(R.id.editTraitement);
            deleteTraitement = (ImageView) itemView.findViewById(R.id.deleteTraitement);
        }
    }
}
