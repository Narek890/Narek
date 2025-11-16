package com.example.clothes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private int userId;
    private String userRole;
    private String userBrigade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Получаем данные пользователя
        Intent intent = getIntent();
        userRole = intent.getStringExtra("user_role");
        userId = intent.getIntExtra("user_id", -1);
        userBrigade = intent.getStringExtra("user_brigade");

        databaseHelper = new DatabaseHelper(this);

        // В зависимости от роли показываем разный интерфейс
        switch (userRole) {
            case "worker":
                setContentView(R.layout.activity_dashboard_worker);
                setupWorkerDashboard();
                break;
            case "master":
                setContentView(R.layout.activity_dashboard_master);
                setupMasterDashboard();
                break;
            case "storekeeper":
                setContentView(R.layout.activity_dashboard_storekeeper);
                setupStorekeeperDashboard();
                break;
            case "manager":
                setContentView(R.layout.activity_dashboard_manager);
                setupManagerDashboard();
                break;
            default:
                setContentView(R.layout.activity_dashboard);
                setupGeneralDashboard();
                break;
        }

        Log.d("Dashboard", "🎯 Открыт дашборд для роли: " + userRole);
    }

    private void setupGeneralDashboard() {
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvUserInfo = findViewById(R.id.tvUserInfo);
        Button btnLogout = findViewById(R.id.btnLogout);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");
        String userEmail = intent.getStringExtra("user_email");

        tvWelcome.setText("Добро пожаловать, " + userName + "!");
        tvUserInfo.setText(userName + "\n" + userEmail);

        btnLogout.setOnClickListener(v -> logout());
    }

    // === WORKER DASHBOARD ===
    private void setupWorkerDashboard() {
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvPosition = findViewById(R.id.tvPosition);
        TextView tvCompletedCount = findViewById(R.id.tvCompletedCount);
        TextView tvDefectsCount = findViewById(R.id.tvDefectsCount);
        TextView tvDefectsPercent = findViewById(R.id.tvDefectsPercent);
        TextView tvOperation1 = findViewById(R.id.tvOperation1);
        TextView tvOperation2 = findViewById(R.id.tvOperation2);
        TextView tvOperation3 = findViewById(R.id.tvOperation3);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Устанавливаем данные пользователя
        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");
        String userPosition = intent.getStringExtra("user_position");

        tvWelcome.setText(userName);
        tvPosition.setText("(" + (userPosition != null ? userPosition : "Работник") + ")");

        // Загружаем данные из БД
        if (userId != -1) {
            new Thread(() -> {
                WorkerStats stats = databaseHelper.getWorkerStats(userId);
                runOnUiThread(() -> {
                    tvCompletedCount.setText("06:45 Выполнено: " + stats.completed + " шт");
                    tvDefectsCount.setText("Брак: " + stats.defects + " шт");
                    tvDefectsPercent.setText(String.format("(%.1f%%)", stats.getDefectsPercent()));

                    String[] assignments = stats.todayAssignments.split("\n");
                    if (assignments.length > 0) tvOperation1.setText(assignments[0]);
                    if (assignments.length > 1) tvOperation2.setText(assignments[1]);
                    if (assignments.length > 2) tvOperation3.setText(assignments[2]);
                });
            }).start();
        }

        btnLogout.setOnClickListener(v -> logout());
    }

    // === MASTER DASHBOARD ===
    private void setupMasterDashboard() {
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvBrigade = findViewById(R.id.tvBrigade);
        TextView tvWorkersCount = findViewById(R.id.tvWorkersCount);
        TextView tvTotalCompleted = findViewById(R.id.tvTotalCompleted);
        TextView tvTotalDefects = findViewById(R.id.tvTotalDefects);
        TextView tvDefectsPercent = findViewById(R.id.tvDefectsPercent);
        TextView tvWorker1 = findViewById(R.id.tvWorker1);
        TextView tvWorker2 = findViewById(R.id.tvWorker2);
        TextView tvWorker3 = findViewById(R.id.tvWorker3);
        Button btnLogout = findViewById(R.id.btnLogout);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");

        tvWelcome.setText(userName);
        tvBrigade.setText(userBrigade != null ? userBrigade : "Бригада №1");

        // Загружаем данные из БД
        new Thread(() -> {
            MasterStats stats = databaseHelper.getMasterStats(userId, userBrigade);
            runOnUiThread(() -> {
                tvWorkersCount.setText("Работников: " + stats.workersCount);
                tvTotalCompleted.setText("Выполнено: " + stats.totalCompleted + " шт");
                tvTotalDefects.setText("Брак: " + stats.totalDefects + " шт");
                tvDefectsPercent.setText(String.format("(%.1f%%)", stats.getDefectsPercent()));

                // Показываем топ работников
                if (stats.workers.size() > 0) {
                    tvWorker1.setText(stats.workers.get(0).name + " - " + stats.workers.get(0).completed + " шт");
                }
                if (stats.workers.size() > 1) {
                    tvWorker2.setText(stats.workers.get(1).name + " - " + stats.workers.get(1).completed + " шт");
                }
                if (stats.workers.size() > 2) {
                    tvWorker3.setText(stats.workers.get(2).name + " - " + stats.workers.get(2).completed + " шт");
                }
            });
        }).start();

        btnLogout.setOnClickListener(v -> logout());
    }

    // === STOREKEEPER DASHBOARD ===
    private void setupStorekeeperDashboard() {
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvMaterial1 = findViewById(R.id.tvMaterial1);
        TextView tvMaterial2 = findViewById(R.id.tvMaterial2);
        TextView tvMaterial3 = findViewById(R.id.tvMaterial3);
        TextView tvRecentUsage = findViewById(R.id.tvRecentUsage);
        Button btnLogout = findViewById(R.id.btnLogout);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");

        tvWelcome.setText(userName);

        // Загружаем данные из БД
        new Thread(() -> {
            StorekeeperStats stats = databaseHelper.getStorekeeperStats();
            runOnUiThread(() -> {
                // Материалы с низким запасом
                if (stats.lowStockMaterials.size() > 0) {
                    Material m = stats.lowStockMaterials.get(0);
                    tvMaterial1.setText(m.name + ": " + m.currentStock + " " + m.unit + " (мин: " + m.minStock + ")");
                }
                if (stats.lowStockMaterials.size() > 1) {
                    Material m = stats.lowStockMaterials.get(1);
                    tvMaterial2.setText(m.name + ": " + m.currentStock + " " + m.unit + " (мин: " + m.minStock + ")");
                }
                if (stats.lowStockMaterials.size() > 2) {
                    Material m = stats.lowStockMaterials.get(2);
                    tvMaterial3.setText(m.name + ": " + m.currentStock + " " + m.unit + " (мин: " + m.minStock + ")");
                }

                tvRecentUsage.setText(stats.recentUsage);
            });
        }).start();

        btnLogout.setOnClickListener(v -> logout());
    }

    // === MANAGER DASHBOARD ===
    private void setupManagerDashboard() {
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvTotalOrders = findViewById(R.id.tvTotalOrders);
        TextView tvCompletedOrders = findViewById(R.id.tvCompletedOrders);
        TextView tvInProgressOrders = findViewById(R.id.tvInProgressOrders);
        TextView tvCompletionPercent = findViewById(R.id.tvCompletionPercent);
        TextView tvBrigadePerformance = findViewById(R.id.tvBrigadePerformance);
        Button btnLogout = findViewById(R.id.btnLogout);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");

        tvWelcome.setText(userName);

        // Загружаем данные из БД
        new Thread(() -> {
            ManagerStats stats = databaseHelper.getManagerStats();
            runOnUiThread(() -> {
                tvTotalOrders.setText("Всего заказов: " + stats.totalOrders);
                tvCompletedOrders.setText("Выполнено: " + stats.completedOrders);
                tvInProgressOrders.setText("В работе: " + stats.inProgressOrders);
                tvCompletionPercent.setText("Выполнение: " + stats.getCompletionPercent() + "%");
                tvBrigadePerformance.setText(stats.brigadePerformance);
            });
        }).start();

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        Toast.makeText(this, "Выход из системы", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

