package com.example.masrouf.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.masrouf.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * RÔLE : Bottom Sheet Modal pour la sélection de la source d'une photo (Appareil photo vs Galerie)
 * COUCHE MVC : Vue (View).
 */
public class PhotoSourceModal {

    public enum PhotoAction {
        CAMERA,
        GALLERY,
        DELETE
    }

    public interface OnPhotoSourceSelectedListener {
        void onActionSelected(PhotoAction action);
    }

    public static void show(Activity activity, boolean showDeleteOption, OnPhotoSourceSelectedListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.modal_photo_source, null);
        dialog.setContentView(dialogView);

        LinearLayout btnCamera = dialogView.findViewById(R.id.btnOptionCamera);
        LinearLayout btnGallery = dialogView.findViewById(R.id.btnOptionGallery);
        LinearLayout btnDelete = dialogView.findViewById(R.id.btnOptionDeletePhoto);
        Button btnCancel = dialogView.findViewById(R.id.btnOptionCancel);

        if (showDeleteOption) {
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        btnCamera.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onActionSelected(PhotoAction.CAMERA);
        });

        btnGallery.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onActionSelected(PhotoAction.GALLERY);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onActionSelected(PhotoAction.DELETE);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
