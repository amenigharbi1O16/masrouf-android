package com.example.masrouf.view.family;

import android.os.Bundle;
import com.example.masrouf.BaseActivity;

/**
 * Ancien écran de demandes d'argent supprimé définitivement.
 * Le profil Parent utilise désormais le système budgétaire centralisé unique.
 */
@Deprecated
public class FamilyRequestsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
