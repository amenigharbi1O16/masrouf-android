package com.example.masrouf.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Écran d'historique complet des achats avec recherche en temps réel (Figma 14).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec TransactionDao).
 *   - Matrice §4 ligne 30 (Adapter personnalisé avec ViewHolder).
 *   - Clic sur une transaction pour l'éditer (Figma 24).
 */
import com.example.masrouf.BaseActivity;

public class PurchaseHistoryActivity extends BaseActivity {

    private EditText etSearchPurchases;
    private TextView tvTotalPurchasesCount;
    private ListView lvPurchaseHistory;

    private SessionManager sessionManager;
    private TransactionDao transactionDao;
    private List<Transaction> allPurchases = new ArrayList<>();
    private List<Transaction> filteredPurchases = new ArrayList<>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history);

        sessionManager = new SessionManager(this);
        transactionDao = new TransactionDao(this);

        initViews();
        loadPurchases();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPurchases();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackPurchaseHistory);
        ivBack.setOnClickListener(v -> finish());

        etSearchPurchases = findViewById(R.id.etSearchPurchases);
        tvTotalPurchasesCount = findViewById(R.id.tvTotalPurchasesCount);
        lvPurchaseHistory = findViewById(R.id.lvPurchaseHistory);

        etSearchPurchases.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        lvPurchaseHistory.setOnItemClickListener((parent, view, position, id) -> {
            Transaction t = filteredPurchases.get(position);
            Intent intent = new Intent(PurchaseHistoryActivity.this, EditPurchaseActivity.class);
            intent.putExtra(EditPurchaseActivity.EXTRA_TRANSACTION_ID, t.getId());
            startActivity(intent);
        });
    }

    private void loadPurchases() {
        long userId = sessionManager.getUserId();
        allPurchases = transactionDao.getAllTransactions(userId);
        filter(etSearchPurchases.getText().toString());
    }

    private void filter(String query) {
        filteredPurchases.clear();
        String q = query.toLowerCase().trim();

        for (Transaction t : allPurchases) {
            if (q.isEmpty() ||
                    (t.getTitle() != null && t.getTitle().toLowerCase().contains(q)) ||
                    (t.getStore() != null && t.getStore().toLowerCase().contains(q)) ||
                    (t.getCategoryName() != null && t.getCategoryName().toLowerCase().contains(q))) {
                filteredPurchases.add(t);
            }
        }

        tvTotalPurchasesCount.setText(filteredPurchases.size() + " TRANSACTIONS RECORDED");

        if (adapter == null) {
            adapter = new HistoryAdapter(this, filteredPurchases);
            lvPurchaseHistory.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    public static class HistoryAdapter extends BaseAdapter {
        private final Context context;
        private final List<Transaction> list;

        public HistoryAdapter(Context context, List<Transaction> list) {
            this.context = context;
            this.list = list;
        }

        @Override public int getCount() { return list.size(); }
        @Override public Object getItem(int position) { return list.get(position); }
        @Override public long getItemId(int position) { return list.get(position).getId(); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LinearLayout card = new LinearLayout(context);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setGravity(Gravity.CENTER_VERTICAL);
                card.setPadding(28, 20, 28, 20);

                GradientDrawable gd = new GradientDrawable();
                gd.setColor(Color.WHITE);
                gd.setCornerRadius(20);
                gd.setStroke(2, Color.parseColor("#E5E2DA"));
                card.setBackground(gd);

                LinearLayout textCol = new LinearLayout(context);
                textCol.setOrientation(LinearLayout.VERTICAL);
                textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

                TextView tvTitle = new TextView(context);
                tvTitle.setTextColor(Color.parseColor("#111A16"));
                tvTitle.setTextSize(15);
                tvTitle.setTypeface(null, Typeface.BOLD);
                textCol.addView(tvTitle);

                TextView tvSub = new TextView(context);
                tvSub.setTextColor(Color.parseColor("#6B7C74"));
                tvSub.setTextSize(12);
                textCol.addView(tvSub);

                card.addView(textCol);

                TextView tvAmount = new TextView(context);
                tvAmount.setTextColor(Color.parseColor("#102B27"));
                tvAmount.setTextSize(15);
                tvAmount.setTypeface(null, Typeface.BOLD);
                card.addView(tvAmount);

                holder = new ViewHolder();
                holder.tvTitle = tvTitle;
                holder.tvSub = tvSub;
                holder.tvAmount = tvAmount;
                card.setTag(holder);
                convertView = card;
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Transaction t = list.get(position);
            holder.tvTitle.setText(t.getTitle());
            String store = t.getStore() != null && !t.getStore().isEmpty() ? " · " + t.getStore() : "";
            holder.tvSub.setText(t.getDate() + store);
            holder.tvAmount.setText("-" + MillimesUtil.formatDt(t.getAmount()));

            return convertView;
        }

        private static class ViewHolder {
            TextView tvTitle;
            TextView tvSub;
            TextView tvAmount;
        }
    }
}
