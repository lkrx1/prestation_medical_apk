package com.lkrx1.salama.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.lkrx1.salama.model.Patient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Patient> patients;
    private EditText et_nom_patient, et_adresse_patient;
    private String url = "http://192.168.124.5:8080/api/rest/patients";

    public PatientAdapter(Context context, ArrayList<Patient> patients) {
        this.context = context;
        this.patients = patients;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.patient_list, parent, false);

        return new PatientAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.id.setText(String.valueOf(patients.get(position).getId()));
        holder.nom.setText(patients.get(position).getNom());
        holder.adresse.setText(patients.get(position).getAdresse());

        holder.editPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editPatient(new Patient(patients.get(position).getId(), patients.get(position).getNom(), patients.get(position).getAdresse()));
            }
        });
        holder.deletePatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = patients.get(position).getId();
                deleteMdc(id);
            }
        });
    }

    private void deleteMdc(int id) {
        TextView close, title;
        Button btn_confirm_delete_pat, btn_cancel_delete_pat;

        final Dialog dialog;
        dialog = new Dialog(context);

        dialog.setContentView(R.layout.delete_pat);

        close = (TextView) dialog.findViewById(R.id.textClose);
        title = (TextView) dialog.findViewById(R.id.title_patient_mod);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_confirm_delete_pat = (Button) dialog.findViewById(R.id.btn_confirm_delete_pat);
        btn_cancel_delete_pat = (Button) dialog.findViewById(R.id.btn_cancel_delete_pat);

        btn_confirm_delete_pat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Submit("DELETE", null, dialog, id);
            }
        });

        btn_cancel_delete_pat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void editPatient(Patient current_patient) {
        TextView close, title;
        Button submit;

        final Dialog dialog;
        dialog = new Dialog(context);

        dialog.setContentView(R.layout.activity_mod_patient);

        close = (TextView) dialog.findViewById(R.id.textClose);
        title = (TextView) dialog.findViewById(R.id.title_patient_mod);
        title.setText("Modification d'un patient");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        et_nom_patient = (EditText) dialog.findViewById(R.id.et_nom_pat);
        et_adresse_patient = (EditText) dialog.findViewById(R.id.et_adresse_pat);
        submit = (Button) dialog.findViewById(R.id.btn_save_pat);

        et_nom_patient.setText(current_patient.getNom());
        et_adresse_patient.setText(current_patient.getAdresse());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Patient final_pat = new Patient(et_nom_patient.getText().toString(), et_adresse_patient.getText().toString());
                Submit("PUT", final_pat, dialog, current_patient.getId());
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void Submit(String method, Patient data, Dialog dialog, int id) {
        if (method == "PUT") {
            StringRequest request = new StringRequest(Request.Method.PUT, url + "/" + id, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dialog.dismiss();
                    Toast.makeText(context, "Patient modifie avec succes.", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(context, "Erreur lors de la modification du patent.", Toast.LENGTH_LONG).show();
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
                    String jsonString = "{\"nom\":\"" + data.getNom() + "\",\"adresse\":\"" + data.getAdresse() + "\"}";
                    return jsonString.getBytes();
                }
            };
            Volley.newRequestQueue(context).add(request);
        } else if (method == "DELETE") {
            StringRequest request = new StringRequest(Request.Method.DELETE, url + "/" + id, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dialog.dismiss();
                    Toast.makeText(context, "La suppression du patient a bien ete effectuee.", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(context, "Erreur lors du patient du medecin.", Toast.LENGTH_LONG).show();
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
        return patients.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView id, nom, adresse;
        private ImageView editPatient, deletePatient;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.id_patient);
            nom = (TextView) itemView.findViewById(R.id.nom_patient);
            adresse = (TextView) itemView.findViewById(R.id.adresse_patient);

            editPatient = (ImageView) itemView.findViewById(R.id.editPatient);
            deletePatient = (ImageView) itemView.findViewById(R.id.deletePatient);
        }
    }
}
