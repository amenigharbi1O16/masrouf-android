package com.example.masrouf;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.User;
import com.example.masrouf.util.PasswordUtil;

public class SignUpActivity extends BaseActivity {

    public static final String EXTRA_TYPE = "account_type";
    public static final String TYPE_PERSONAL = "personal";
    public static final String TYPE_FAMILY = "family";

    private boolean family;

    private View tabPersonal, tabFamily, banner;
    private ImageView ivTabPersonal, ivTabFamily, ivBanner;
    private TextView tvTabPersonal, tvTabFamily, tvBanner;
    private EditText etName, etEmail, etPassword, etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        tabPersonal = findViewById(R.id.tab_personal);
        tabFamily = findViewById(R.id.tab_family);
        ivTabPersonal = findViewById(R.id.iv_tab_personal);
        ivTabFamily = findViewById(R.id.iv_tab_family);
        tvTabPersonal = findViewById(R.id.tv_tab_personal);
        tvTabFamily = findViewById(R.id.tv_tab_family);
        banner = findViewById(R.id.banner_info);
        ivBanner = findViewById(R.id.iv_banner);
        tvBanner = findViewById(R.id.tv_banner);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);

        setupLangChips();

        family = TYPE_FAMILY.equals(getIntent().getStringExtra(EXTRA_TYPE));
        selectType(family);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        tabPersonal.setOnClickListener(v -> selectType(false));
        tabFamily.setOnClickListener(v -> selectType(true));
        findViewById(R.id.btn_signup).setOnClickListener(v -> submit());
        findViewById(R.id.btn_go_login).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void selectType(boolean isFamily) {
        family = isFamily;
        int dark = getColor(R.color.dark);
        int muted = getColor(R.color.muted);
        int blue = getColor(R.color.blue_dark);

        tabPersonal.setBackgroundResource(isFamily ? R.drawable.bg_tab_off : R.drawable.bg_tab_personal_on);
        tabFamily.setBackgroundResource(isFamily ? R.drawable.bg_tab_family_on : R.drawable.bg_tab_off);

        int personalColor = isFamily ? muted : dark;
        int familyColor = isFamily ? blue : muted;
        tvTabPersonal.setTextColor(personalColor);
        ivTabPersonal.setImageTintList(ColorStateList.valueOf(personalColor));
        tvTabFamily.setTextColor(familyColor);
        ivTabFamily.setImageTintList(ColorStateList.valueOf(familyColor));
        tvTabPersonal.setTypeface(null, isFamily ? Typeface.NORMAL : Typeface.BOLD);
        tvTabFamily.setTypeface(null, isFamily ? Typeface.BOLD : Typeface.NORMAL);

        banner.setBackgroundResource(isFamily ? R.drawable.bg_info_family : R.drawable.bg_info_personal);
        ivBanner.setImageResource(isFamily ? R.drawable.ic_info : R.drawable.ic_account_circle);
        ivBanner.setImageTintList(ColorStateList.valueOf(isFamily ? blue : dark));
        tvBanner.setText(isFamily ? R.string.info_family : R.string.info_personal);
        tvBanner.setTextColor(isFamily ? blue : dark);
    }

    private void submit() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        if (name.isEmpty()) {
            etName.setError(getString(R.string.err_name));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.err_email));
            return;
        }
        if (pwd.length() < 6) {
            etPassword.setError(getString(R.string.err_password_len));
            return;
        }
        if (!pwd.equals(confirm)) {
            etConfirm.setError(getString(R.string.err_password_match));
            return;
        }

        UserDao userDao = new UserDao(this);
        User existingUser = userDao.findByEmail(email);
        if (existingUser != null) {
            etEmail.setError(getString(R.string.err_email_exists));
            return;
        }

        String passwordHash = PasswordUtil.hashPassword(pwd);
        String role = family ? User.ROLE_PARENT : User.ROLE_USER;

        User user = new User(email, passwordHash, name, role);
        long userId = userDao.insertUser(user);

        if (userId != -1) {
            user.setId(userId);
            Toast.makeText(this, R.string.signup_ok, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, R.string.error_generic_database, Toast.LENGTH_SHORT).show();
        }
    }
}
