package com.example.clothes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword, etName, etBrigade, etPosition;
    private Button btnRegister, btnBackToLogin;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        System.out.println("📝 РЕГИСТРАЦИЯ ЗАПУЩЕНА");

        // Инициализация БД
        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etName = findViewById(R.id.etName);
        etBrigade = findViewById(R.id.etBrigade);
        etPosition = findViewById(R.id.etPosition);

        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());

        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegistration() {
        // Получаем значения из полей ввода
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String brigade = etBrigade.getText().toString().trim();
        String position = etPosition.getText().toString().trim();

        System.out.println("🔄 ПОПЫТКА РЕГИСТРАЦИИ: " + email);

        // Валидация полей
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Если бригада и должность не указаны, устанавливаем значения по умолчанию
        if (brigade.isEmpty()) {
            brigade = "Не указана";
        }
        if (position.isEmpty()) {
            position = "Не указана";
        }

        // Создаем final копии для использования в лямбда-выражении
        final String finalEmail = email;
        final String finalPassword = password;
        final String finalName = name;
        final String finalBrigade = brigade;
        final String finalPosition = position;

        // Регистрация в БД в фоновом потоке
        new Thread(() -> {
            boolean success = databaseHelper.registerUser(finalEmail, finalPassword, finalName, finalBrigade, finalPosition);

            runOnUiThread(() -> {
                if (success) {
                    registrationSuccessful();
                } else {
                    Toast.makeText(RegistrationActivity.this, "Ошибка регистрации. Возможно, email уже используется", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void registrationSuccessful() {
        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();

        // Возвращаемся на главный экран
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("registration_success", true);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("❌ РЕГИСТРАЦИЯ ЗАКРЫТА");
    }
}
