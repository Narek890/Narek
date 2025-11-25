package com.example.clothes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private TextView tvForgotPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация БД
        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();

        Toast.makeText(this, "Приложение запущено", Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword); // Убедитесь что этот ID есть

        // Проверяем что элементы найдены
        if (tvForgotPassword == null) {
            Toast.makeText(this, "Ошибка: кнопка 'Забыли пароль' не найдена", Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });

        // Обработчик для "Забыли пароль"
        tvForgotPassword.setOnClickListener(v -> {
            openForgotPassword();
        });
    }

    private void openForgotPassword() {
        try {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            // Если активность не найдена, показываем диалог
            showForgotPasswordDialog();
        }
    }

    private void showForgotPasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Восстановление пароля");
        builder.setMessage("Для восстановления пароля обратитесь к администратору.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void attemptLogin() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Проверка данных...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                // Используем DatabaseHelper.User вместо User
                DatabaseHelper.User user = databaseHelper.authenticateUser(email, password);

                runOnUiThread(() -> {
                    if (user != null) {
                        loginSuccessful(user);
                    } else {
                        Toast.makeText(MainActivity.this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Ошибка базы данных", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loginSuccessful(DatabaseHelper.User user) {
        Toast.makeText(this, "Добро пожаловать, " + user.getName() + "!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("user_id", user.getId());
        intent.putExtra("user_name", user.getName());
        intent.putExtra("user_position", user.getPosition());
        intent.putExtra("user_brigade", user.getBrigade());
        intent.putExtra("user_email", user.getEmail());
        intent.putExtra("user_role", user.getRole());
        startActivity(intent);
        finish();
    }
}