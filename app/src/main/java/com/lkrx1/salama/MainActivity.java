package com.lkrx1.salama;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;

public class MainActivity extends AppCompatActivity {

    private final int ID_MEDECIN = 1;
    private final int ID_PATIENT = 2;
    private final int ID_TRAITEMENT = 3;
    private final int ID_STATISTIQUES = 4;
    private final int ID_ABOUT = 5;
    private EditText et_search_item;
    private TextView header_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        TextView selected_page = findViewById(R.id.selected_page);
        MeowBottomNavigation bottomNavigation = findViewById(R.id.bottomNavigation);
        header_title = (TextView) findViewById((R.id.header_title));

        bottomNavigation.add(new MeowBottomNavigation.Model(ID_MEDECIN, R.drawable.ic_baseline_supervised_user_circle_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_PATIENT, R.drawable.ic_baseline_sports_kabaddi_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_TRAITEMENT, R.drawable.ic_baseline_work_outline_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_STATISTIQUES, R.drawable.ic_baseline_insert_chart_outlined_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_ABOUT, R.drawable.ic_baseline_person_24));

        bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                String title = new String(charger_nom_section(item.getId()));
                header_title.setText(title);
                Toast.makeText(MainActivity.this, "" + title.toUpperCase(), Toast.LENGTH_SHORT).show();
            }
        });

        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
//                String name;
                switch (item.getId()) {
                    case ID_MEDECIN:
                        replaceFragment(new Medecin());
                        break;

                    case ID_PATIENT:
                        replaceFragment(new Patient());
                        break;

                    case ID_TRAITEMENT:
                        replaceFragment(new Traitement());
                        break;

                    case ID_STATISTIQUES:
                        replaceFragment(new Statistique());
                        break;

                    case ID_ABOUT:
                        replaceFragment(new APropos());
                        break;
                }
            }
        });
        bottomNavigation.show(ID_MEDECIN, true);
    }

    private String charger_nom_section(int id) {
        switch (id) {
            case 1:
                return "Medecins";
            case 2:
                return "Patients";
            case 3:
                return "Traitements";
            case 4:
                return "Statistiques";
            case 5:
                return "A propos";
        }
        return "";
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.detach(fragment);
        fragmentTransaction.attach(fragment);
        fragmentTransaction.replace(R.id.frameLayouts, fragment);
        fragmentTransaction.commit();
    }
}