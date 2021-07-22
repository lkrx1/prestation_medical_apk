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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lkrx1.salama.R;
import com.lkrx1.salama.model.Medecin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MedecinAdapter extends RecyclerView.Adapter<MedecinAdapter.MyViewHolder> {
    private Context context;
    private SwipeRefreshLayout refresh;
    private ArrayList<Medecin> medecins;
    private EditText et_nom_mdc, et_tj_mdc;
    private String url = "http://192.168.124.5:8080/api/rest/medecins";

    public MedecinAdapter(Context context, ArrayList<Medecin> medecins) {
        this.context = context;
        this.medecins = medecins;
    }

    @NonNull
    @Override
    public MedecinAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.medecin_list, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedecinAdapter.MyViewHolder holder, int position) {
        holder.id.setText(String.valueOf(medecins.get(position).getId()));
        holder.nom.setText(medecins.get(position).getNom());
        holder.taux_journalier.setText("Prestation à " + medecins.get(position).getTaux_journalier().toString() + "Ar");

        holder.editMdc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMedecin(new Medecin(medecins.get(position).getId(), medecins.get(position).getNom(), Double.parseDouble(medecins.get(position).getTaux_journalier().toString())), position);
            }
        });
        holder.deleteMdc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = medecins.get(position).getId();
                deleteMdc(id, position);
            }
        });
        holder.setIsRecyclable(true);// Do not use multiplexed
    }

    private void deleteMdc(int id, int position) {
        TextView close, title;
        Button btn_confirm_delete_mdc, btn_cancel_delete_mdc;

        final Dialog dialog;
        dialog = new Dialog(context);

        dialog.setContentView(R.layout.delete_mdc);

        close = (TextView) dialog.findViewById(R.id.textClose);
        title = (TextView) dialog.findViewById(R.id.title_medecin_mod);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_confirm_delete_mdc = (Button) dialog.findViewById(R.id.btn_confirm_delete_mdc);
        btn_cancel_delete_mdc = (Button) dialog.findViewById(R.id.btn_cancel_delete_mdc);

        btn_confirm_delete_mdc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Submit("DELETE", null, dialog, id, position);
            }
        });

        btn_cancel_delete_mdc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void editMedecin(Medecin current_mdc, int position) {
        TextView close, title;
        Button submit;

        final Dialog dialog;
        dialog = new Dialog(context);

        dialog.setContentView(R.layout.activity_mod_medecin);

        close = (TextView) dialog.findViewById(R.id.textClose);
        title = (TextView) dialog.findViewById(R.id.title_medecin_mod);
        title.setText("Modification d'un medecin");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        et_nom_mdc = (EditText) dialog.findViewById(R.id.et_nom_mdc);
        et_tj_mdc = (EditText) dialog.findViewById(R.id.et_tj_mdc);
        submit = (Button) dialog.findViewById(R.id.btn_save_mdc);

        et_nom_mdc.setText(current_mdc.getNom());
        et_tj_mdc.setText(current_mdc.getTaux_journalier().toString());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Medecin final_mdc = new Medecin(et_nom_mdc.getText().toString(), Double.parseDouble(et_tj_mdc.getText().toString()));
                Submit("PUT", final_mdc, dialog, current_mdc.getId(), position);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void Submit(String method, final Medecin data, final Dialog dialog, final int id, final int position) {
        if (method == "PUT") {
            StringRequest request = new StringRequest(Request.Method.PUT, url + "/" + id, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dialog.dismiss();
                    notifyDataSetChanged();
                    Toast.makeText(context, "Modification de medecin avec succes.", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(context, "Erreur lors de la modification du medecin.", Toast.LENGTH_LONG).show();
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
                    String jsonString = "{\"nom\":\"" + data.getNom() + "\",\"taux_journalier\":\"" + data.getTaux_journalier() + "\"}";
                    return jsonString.getBytes();
                }
            };
            Volley.newRequestQueue(context).add(request);
        } else if (method == "DELETE") {
            StringRequest request = new StringRequest(Request.Method.DELETE, url + "/" + id, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    medecins.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, medecins.size());
                    dialog.dismiss();
                    Toast.makeText(context, "La suppression du medecin a bien ete effectuee.", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(context, "Erreur lors de la suppresion du medecin.", Toast.LENGTH_LONG).show();
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
        return medecins.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView id, nom, taux_journalier;
        private ImageView editMdc, deleteMdc;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.id_medecin);
            nom = (TextView) itemView.findViewById(R.id.nom_medecin);
            taux_journalier = (TextView) itemView.findViewById(R.id.tj_mdc);

            editMdc = (ImageView) itemView.findViewById(R.id.editMedecin);
            deleteMdc = (ImageView) itemView.findViewById(R.id.deleteMedecin);
        }
    }
}
