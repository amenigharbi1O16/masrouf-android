package com.example.masrouf.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Écran dédié à la sélection d'une catégorie budgétaire.
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Matrice §4 ligne 28 (ListView associée à un ArrayAdapter standard, Ch.6 du cours).
 *   - Matrice §4 ligne 10 (Communication par startActivityForResult / onActivityResult : renvoi du résultat via setResult(RESULT_OK, data) puis finish(), Ch.5 du cours).
 */
import com.example.masrouf.BaseActivity;

public class SelectCategoryActivity extends BaseActivity {

    public static final String EXTRA_CATEGORY_ID = "EXTRA_SELECTED_CATEGORY_ID";
    public static final String EXTRA_CATEGORY_NAME = "EXTRA_SELECTED_CATEGORY_NAME";

    private ListView lvCategories;
    private Button btnCancelCategory;
    private CategoryDao categoryDao;
    private List<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        categoryDao = new CategoryDao(this);
        lvCategories = findViewById(R.id.lvCategories);
        btnCancelCategory = findViewById(R.id.btnCancelCategory);

        // 1. Récupération des catégories depuis SQLite via le DAO
        categoryList = categoryDao.getAllCategories();

        // 2. Préparation de la liste des libellés textuels pour l'ArrayAdapter
        List<String> categoryNames = new ArrayList<>();
        for (Category c : categoryList) {
            categoryNames.add(c.getNameEn() + " (" + c.getType() + ")");
        }

        // 3. EXIGENCE MATRICE §4 LIGNE 28 : Création et liaison d'un ArrayAdapter<String> standard (Ch.6)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1, // Layout système simple fourni par Android
                categoryNames
        );
        lvCategories.setAdapter(adapter);

        // 4. Gestion du clic sur un élément de la liste
        lvCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category selected = categoryList.get(position);

                // EXIGENCE MATRICE §4 LIGNE 10 : Construction de l'Intent résultat
                Intent resultData = new Intent();
                resultData.putExtra(EXTRA_CATEGORY_ID, selected.getId());
                resultData.putExtra(EXTRA_CATEGORY_NAME, selected.getNameEn());

                // Transmission du code succès RESULT_OK et fermeture de l'activité
                setResult(RESULT_OK, resultData);
                finish();
            }
        });

        // Annulation sans sélection
        btnCancelCategory.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
