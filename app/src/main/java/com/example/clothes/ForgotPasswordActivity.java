package com.example.clothes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etNewPassword, etConfirmPassword;
    private Button btnResetPassword, btnBackToLogin;
    private TextView tvStep1, tvStep2;
    private DatabaseHelper databaseHelper;

    private String userEmail;
    private boolean isEmailVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Log.d("ForgotPassword", "🔐 Активность восстановления пароля запущена");

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();

        // Показываем первый шаг (ввод email)
        showStep1();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        tvStep1 = findViewById(R.id.tvStep1);
        tvStep2 = findViewById(R.id.tvStep2);
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(v -> {
            if (!isEmailVerified) {
                verifyEmail();
            } else {
                resetPassword();
            }
        });

        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void verifyEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Введите ваш email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем существование email в БД
        new Thread(() -> {
            boolean emailExists = databaseHelper.isEmailExists(email);

            runOnUiThread(() -> {
                if (emailExists) {
                    userEmail = email;
                    isEmailVerified = true;
                    showStep2();
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Email подтвержден. Установите новый пароль", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Пользователь с таким email не найден", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Обновляем пароль в БД
        new Thread(() -> {
            boolean success = databaseHelper.updatePassword(userEmail, newPassword);

            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Пароль успешно изменен!", Toast.LENGTH_SHORT).show();

                    // Возвращаемся на экран входа
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("password_reset", true);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Ошибка изменения пароля", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showStep1() {
        tvStep1.setVisibility(TextView.VISIBLE);
        tvStep2.setVisibility(TextView.GONE);
        etEmail.setVisibility(EditText.VISIBLE);
        etNewPassword.setVisibility(EditText.GONE);
        etConfirmPassword.setVisibility(EditText.GONE);
        btnResetPassword.setText("ПРОВЕРИТЬ EMAIL");
    }

    private void showStep2() {
        tvStep1.setVisibility(TextView.GONE);
        tvStep2.setVisibility(TextView.VISIBLE);
        etEmail.setVisibility(EditText.GONE);
        etNewPassword.setVisibility(EditText.VISIBLE);
        etConfirmPassword.setVisibility(EditText.VISIBLE);
        btnResetPassword.setText("СБРОСИТЬ ПАРОЛЬ");

        // Получаем имя пользователя для персонализации (используем DatabaseHelper.User)
        new Thread(() -> {
            DatabaseHelper.User user = databaseHelper.getUserByEmail(userEmail);
            if (user != null) {
                runOnUiThread(() -> {
                    tvStep2.setText("Сброс пароля для " + user.getName());
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ForgotPassword", "❌ Активность восстановления пароля закрыта");
    }
}