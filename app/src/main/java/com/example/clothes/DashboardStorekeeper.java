package com.example.clothes;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardStorekeeper extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private DatabaseHelper.User currentUser;
    private TextView tvWelcome, tvMaterial1, tvMaterial2, tvMaterial3, tvRecentUsage;
    private Button btnLogout, btnInventory, btnReceiveMaterials, btnIssueMaterials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_storekeeper);

        Log.d("DashboardStorekeeper", "=== СТАРТ АКТИВНОСТИ КЛАДОВЩИКА ===");

        // Получаем данные пользователя
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("user_id")) {
            int userId = intent.getIntExtra("user_id", -1);
            Log.d("DashboardStorekeeper", "Получен user_id из intent: " + userId);
            dbHelper = new DatabaseHelper(this);
            currentUser = getUserById(userId);
            Log.d("DashboardStorekeeper", "Пользователь получен: " + (currentUser != null ? currentUser.getName() : "null"));
        }

        // Если пользователь не передан, пытаемся найти по email
        if (currentUser == null) {
            Log.d("DashboardStorekeeper", "Пользователь не найден по ID, ищем по email...");
            dbHelper = new DatabaseHelper(this);
            currentUser = dbHelper.getUserByEmail("petr@factory.com");
            Log.d("DashboardStorekeeper", "Пользователь найден по email: " + (currentUser != null ? currentUser.getName() : "null"));
        }

        initViews();
        setupClickListeners();

        // Проверяем и добавляем тестовые данные если нужно
        checkAndAddTestData();

        // Загружаем данные
        loadStorekeeperData();
    }

    private void initViews() {
        Log.d("DashboardStorekeeper", "Инициализация View элементов...");

        tvWelcome = findViewById(R.id.tvWelcome);
        tvMaterial1 = findViewById(R.id.tvMaterial1);
        tvMaterial2 = findViewById(R.id.tvMaterial2);
        tvMaterial3 = findViewById(R.id.tvMaterial3);
        tvRecentUsage = findViewById(R.id.tvRecentUsage);

        btnLogout = findViewById(R.id.btnLogout);
        btnInventory = findViewById(R.id.btnInventory);
        btnReceiveMaterials = findViewById(R.id.btnReceiveMaterials);
        btnIssueMaterials = findViewById(R.id.btnIssueMaterials);

        // Проверяем что все View найдены
        if (tvWelcome == null) Log.e("DashboardStorekeeper", "tvWelcome не найден!");
        if (tvMaterial1 == null) Log.e("DashboardStorekeeper", "tvMaterial1 не найден!");
        if (tvMaterial2 == null) Log.e("DashboardStorekeeper", "tvMaterial2 не найден!");
        if (tvMaterial3 == null) Log.e("DashboardStorekeeper", "tvMaterial3 не найден!");
        if (tvRecentUsage == null) Log.e("DashboardStorekeeper", "tvRecentUsage не найден!");
        if (btnLogout == null) Log.e("DashboardStorekeeper", "btnLogout не найден!");
        if (btnInventory == null) Log.e("DashboardStorekeeper", "btnInventory не найден!");
        if (btnReceiveMaterials == null) Log.e("DashboardStorekeeper", "btnReceiveMaterials не найден!");
        if (btnIssueMaterials == null) Log.e("DashboardStorekeeper", "btnIssueMaterials не найден!");

        // Устанавливаем приветствие
        if (currentUser != null) {
            tvWelcome.setText(currentUser.getName() + "\nКладовщик");
            Log.d("DashboardStorekeeper", "Установлено приветствие: " + currentUser.getName());
        } else {
            tvWelcome.setText("Кладовщик");
            Log.d("DashboardStorekeeper", "Установлено приветствие по умолчанию");
        }

        Log.d("DashboardStorekeeper", "Инициализация View завершена");
    }

    private void setupClickListeners() {
        Log.d("DashboardStorekeeper", "Настройка обработчиков кликов...");

        // Выход из системы
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // ИНВЕНТАРИЗАЦИЯ
        btnInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInventoryDialog();
            }
        });

        // ПРИНЯТЬ МАТЕРИАЛЫ
        btnReceiveMaterials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReceiveMaterialsDialog();
            }
        });

        // ВЫДАТЬ МАТЕРИАЛЫ
        btnIssueMaterials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIssueMaterialsDialog();
            }
        });

        Log.d("DashboardStorekeeper", "Обработчики кликов настроены");
    }

    private void checkAndAddTestData() {
        Log.d("DashboardStorekeeper", "Проверка тестовых данных...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (dbHelper == null) {
                        dbHelper = new DatabaseHelper(DashboardStorekeeper.this);
                    }

                    android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();

                    // Проверяем материалы
                    Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM materials", null);
                    cursor.moveToFirst();
                    int materialCount = cursor.getInt(0);
                    cursor.close();

                    Log.d("DashboardStorekeeper", "Количество материалов в БД: " + materialCount);

                    if (materialCount == 0) {
                        Log.d("DashboardStorekeeper", "Добавляем тестовые материалы...");

                        // Добавляем тестовые материалы
                        db.execSQL("INSERT INTO materials (name, unit, current_stock, min_stock, created_at) VALUES " +
                                "('Нитки #40', 'катушка', 2.5, 5.0, datetime('now')), " +
                                "('Пуговицы', 'шт', 45.0, 100.0, datetime('now')), " +
                                "('Молния', 'шт', 8.0, 15.0, datetime('now')), " +
                                "('Ткань х/б', 'метр', 50.0, 20.0, datetime('now'))");

                        Log.d("DashboardStorekeeper", "Тестовые материалы добавлены");

                        // Проверяем есть ли заказ с ID=1
                        cursor = db.rawQuery("SELECT COUNT(*) FROM orders WHERE id = 1", null);
                        cursor.moveToFirst();
                        int orderCount = cursor.getInt(0);
                        cursor.close();

                        if (orderCount == 0) {
                            Log.d("DashboardStorekeeper", "Добавляем тестовый заказ...");
                            db.execSQL("INSERT INTO orders (id, order_number, customer_name, product_id, quantity, status, created_at) VALUES " +
                                    "(1, 'TEST-001', 'Тестовый заказ', 1, 10, 'completed', datetime('now'))");
                        }

                        // Добавляем тестовые списания
                        Log.d("DashboardStorekeeper", "Добавляем тестовые списания...");
                        db.execSQL("INSERT INTO material_usage (order_id, material_id, quantity_used, usage_date, user_id) VALUES " +
                                "(1, 1, -2.5, date('now', '-2 days'), 4), " +
                                "(1, 2, -10.0, date('now', '-3 days'), 4), " +
                                "(1, 3, -3.0, date('now', '-1 days'), 4)");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DashboardStorekeeper.this,
                                        "Добавлены тестовые материалы", Toast.LENGTH_SHORT).show();
                                loadStorekeeperData(); // Перезагружаем данные
                            }
                        });
                    } else {
                        Log.d("DashboardStorekeeper", "Материалы уже существуют в БД");

                        // Проверяем есть ли списания
                        cursor = db.rawQuery("SELECT COUNT(*) FROM material_usage", null);
                        cursor.moveToFirst();
                        int usageCount = cursor.getInt(0);
                        cursor.close();

                        Log.d("DashboardStorekeeper", "Количество списаний в БД: " + usageCount);
                    }

                    db.close();
                } catch (Exception e) {
                    Log.e("DashboardStorekeeper", "Ошибка добавления тестовых данных: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadStorekeeperData() {
        Log.d("DashboardStorekeeper", "=== НАЧАЛО ЗАГРУЗКИ ДАННЫХ ===");

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        // Загружаем данные в фоновом потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("DashboardStorekeeper", "Загрузка данных в фоновом потоке...");

                    // 1. Получаем материалы с низким запасом
                    List<DatabaseHelper.Material> lowStockMaterials = getLowStockMaterials();
                    Log.d("DashboardStorekeeper", "Найдено материалов с низким запасом: " +
                            (lowStockMaterials != null ? lowStockMaterials.size() : 0));

                    // 2. Получаем количество материалов с низким запасом
                    int totalLowStockCount = getTotalLowStockCount();
                    Log.d("DashboardStorekeeper", "Всего материалов с низким запасом: " + totalLowStockCount);

                    // 3. Получаем последние списания
                    String recentUsage = getRecentMaterialUsage();
                    Log.d("DashboardStorekeeper", "Получены последние списания: " + recentUsage);

                    // 4. Обновляем UI в основном потоке
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("DashboardStorekeeper", "Обновление UI...");

                                // Обновляем заголовок с информацией о низких запасах
                                if (currentUser != null) {
                                    String welcomeText = currentUser.getName() + "\nКладовщик";
                                    if (totalLowStockCount > 0) {
                                        welcomeText = currentUser.getName() +
                                                "\n⚠️ ВНИМАНИЕ: " + totalLowStockCount +
                                                " материалов с низким запасом!";
                                    }
                                    tvWelcome.setText(welcomeText);
                                }

                                // Обновляем низкий запас
                                if (lowStockMaterials != null && !lowStockMaterials.isEmpty()) {
                                    Log.d("DashboardStorekeeper", "Обновляем TextView с материалами...");

                                    for (int i = 0; i < Math.min(3, lowStockMaterials.size()); i++) {
                                        DatabaseHelper.Material material = lowStockMaterials.get(i);
                                        String text = material.name + ": " + material.currentStock + " " +
                                                material.unit + " (мин: " + material.minStock + ")";

                                        Log.d("DashboardStorekeeper", "Материал " + i + ": " + text);

                                        switch (i) {
                                            case 0:
                                                tvMaterial1.setText(text);
                                                break;
                                            case 1:
                                                tvMaterial2.setText(text);
                                                break;
                                            case 2:
                                                tvMaterial3.setText(text);
                                                break;
                                        }
                                    }

                                    // Если материалов меньше 3, очищаем оставшиеся TextView
                                    if (lowStockMaterials.size() < 3) {
                                        tvMaterial3.setText("");
                                    }
                                    if (lowStockMaterials.size() < 2) {
                                        tvMaterial2.setText("");
                                    }
                                } else {
                                    Log.d("DashboardStorekeeper", "Нет материалов с низким запасом");
                                    tvMaterial1.setText("Низкий запас: нет данных");
                                    tvMaterial2.setText("");
                                    tvMaterial3.setText("");
                                }

                                // Обновляем последние списания
                                if (recentUsage != null && !recentUsage.isEmpty()) {
                                    tvRecentUsage.setText(recentUsage);
                                    Log.d("DashboardStorekeeper", "Установлены последние списания: " + recentUsage);
                                } else {
                                    tvRecentUsage.setText("За неделю: нет данных об использовании");
                                    Log.d("DashboardStorekeeper", "Установлен текст по умолчанию для списаний");
                                }

                                Toast.makeText(DashboardStorekeeper.this,
                                        "Данные обновлены", Toast.LENGTH_SHORT).show();
                                Log.d("DashboardStorekeeper", "=== ЗАГРУЗКА ДАННЫХ ЗАВЕРШЕНА ===");

                            } catch (Exception e) {
                                Log.e("DashboardStorekeeper", "Ошибка обновления UI: " + e.getMessage());
                                e.printStackTrace();
                                Toast.makeText(DashboardStorekeeper.this,
                                        "Ошибка обновления интерфейса", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("DashboardStorekeeper", "Ошибка загрузки данных: " + e.getMessage());
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DashboardStorekeeper.this,
                                    "Ошибка загрузки данных", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private List<DatabaseHelper.Material> getLowStockMaterials() {
        List<DatabaseHelper.Material> materials = new ArrayList<>();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        Cursor cursor = null;
        try {
            Log.d("DashboardStorekeeper", "Выполняем запрос низкого запаса...");
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT name, unit, current_stock, min_stock FROM materials " +
                            "WHERE current_stock <= min_stock AND min_stock > 0 " +
                            "ORDER BY current_stock ASC LIMIT 5",
                    null
            );

            if (cursor != null) {
                Log.d("DashboardStorekeeper", "Курсор создан, количество строк: " + cursor.getCount());

                while (cursor.moveToNext()) {
                    DatabaseHelper.Material material = new DatabaseHelper.Material();
                    material.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    material.unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
                    material.currentStock = cursor.getDouble(cursor.getColumnIndexOrThrow("current_stock"));
                    material.minStock = cursor.getDouble(cursor.getColumnIndexOrThrow("min_stock"));
                    materials.add(material);

                    Log.d("DashboardStorekeeper", "Добавлен материал: " + material.name +
                            " (" + material.currentStock + "/" + material.minStock + ")");
                }
            } else {
                Log.d("DashboardStorekeeper", "Курсор равен null");
            }
        } catch (Exception e) {
            Log.e("DashboardStorekeeper", "Ошибка получения низкого запаса: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return materials;
    }

    private int getTotalLowStockCount() {
        int count = 0;

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT COUNT(*) FROM materials WHERE current_stock <= min_stock AND min_stock > 0",
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                Log.d("DashboardStorekeeper", "Количество материалов с низким запасом: " + count);
            }
        } catch (Exception e) {
            Log.e("DashboardStorekeeper", "Ошибка подсчета низкого запаса: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    private String getRecentMaterialUsage() {
        StringBuilder usage = new StringBuilder();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        Cursor cursor = null;
        try {
            Log.d("DashboardStorekeeper", "Выполняем запрос последних списаний...");
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT m.name, SUM(ABS(mu.quantity_used)) as total_used, m.unit " +
                            "FROM material_usage mu " +
                            "JOIN materials m ON mu.material_id = m.id " +
                            "WHERE mu.usage_date >= date('now', '-7 days') AND mu.quantity_used < 0 " +
                            "GROUP BY m.name, m.unit " +
                            "ORDER BY total_used DESC LIMIT 3",
                    null
            );

            if (cursor != null) {
                Log.d("DashboardStorekeeper", "Курсор списаний создан, количество строк: " + cursor.getCount());

                usage.append("За неделю: ");
                boolean first = true;

                while (cursor.moveToNext()) {
                    if (!first) {
                        usage.append(", ");
                    }
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    double totalUsed = cursor.getDouble(cursor.getColumnIndexOrThrow("total_used"));
                    String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));

                    usage.append(name).append(" - ").append(totalUsed).append(" ").append(unit);
                    first = false;

                    Log.d("DashboardStorekeeper", "Добавлено списание: " + name + " - " + totalUsed + " " + unit);
                }

                if (first) {
                    usage.append("нет данных об использовании");
                    Log.d("DashboardStorekeeper", "Нет данных о списаниях");
                }
            } else {
                Log.d("DashboardStorekeeper", "Курсор списаний равен null");
                usage.append("За неделю: нет данных об использовании");
            }

        } catch (Exception e) {
            Log.e("DashboardStorekeeper", "Ошибка получения списаний: " + e.getMessage());
            e.printStackTrace();
            usage.append("За неделю: ошибка загрузки данных");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return usage.toString();
    }

    // === МЕТОДЫ ДЛЯ ФУНКЦИОНАЛА КНОПОК ===

    private void showInventoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ИНВЕНТАРИЗАЦИЯ СКЛАДА");
        builder.setMessage("Выберите действие:");

        builder.setPositiveButton("ПРОВЕСТИ ИНВЕНТАРИЗАЦИЮ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMaterialListForInventory();
            }
        });

        builder.setNegativeButton("ИСТОРИЯ ИНВЕНТАРИЗАЦИЙ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showInventoryHistory();
            }
        });

        builder.setNeutralButton("ОТМЕНА", null);
        builder.show();
    }

    private void showMaterialListForInventory() {
        List<Material> allMaterials = getAllMaterials();

        if (allMaterials.isEmpty()) {
            Toast.makeText(this, "Нет материалов в базе данных", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder message = new StringBuilder("Текущие остатки:\n\n");
        for (Material material : allMaterials) {
            message.append(String.format(Locale.getDefault(),
                    "• %s: %.1f %s (мин: %.1f)\n",
                    material.getName(),
                    material.getCurrentStock(),
                    material.getUnit(),
                    material.getMinStock()));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ИНВЕНТАРИЗАЦИЯ");
        builder.setMessage(message.toString());
        builder.setPositiveButton("НАЧАТЬ ПРОВЕРКУ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startInventoryProcess(allMaterials);
            }
        });
        builder.setNegativeButton("ОТМЕНА", null);
        builder.show();
    }

    private void startInventoryProcess(List<Material> materials) {
        if (!materials.isEmpty()) {
            Material material = materials.get(0);
            showInventoryItemDialog(material, materials, 0);
        }
    }

    private void showInventoryItemDialog(Material material, List<Material> materials, int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Инвентаризация: " + material.getName());

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Введите фактическое количество");
        input.setText(String.valueOf(material.getCurrentStock()));
        builder.setView(input);

        builder.setPositiveButton("СОХРАНИТЬ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                try {
                    double actualCount = Double.parseDouble(inputText);
                    updateMaterialStock(material.getId(), actualCount, "inventory");

                    if (index + 1 < materials.size()) {
                        showInventoryItemDialog(materials.get(index + 1), materials, index + 1);
                    } else {
                        Toast.makeText(DashboardStorekeeper.this,
                                "Инвентаризация завершена!", Toast.LENGTH_SHORT).show();
                        loadStorekeeperData();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(DashboardStorekeeper.this,
                            "Введите корректное число", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("ПРОПУСТИТЬ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (index + 1 < materials.size()) {
                    showInventoryItemDialog(materials.get(index + 1), materials, index + 1);
                } else {
                    Toast.makeText(DashboardStorekeeper.this,
                            "Инвентаризация завершена!", Toast.LENGTH_SHORT).show();
                    loadStorekeeperData();
                }
            }
        });

        builder.show();
    }

    private void showInventoryHistory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<MaterialTransaction> history = getInventoryHistory();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (history.isEmpty()) {
                            Toast.makeText(DashboardStorekeeper.this,
                                    "История инвентаризаций пуста", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        StringBuilder message = new StringBuilder("История инвентаризаций:\n\n");
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

                        for (MaterialTransaction transaction : history) {
                            message.append(sdf.format(transaction.getTransactionDate()))
                                    .append(" - ").append(transaction.getMaterialName())
                                    .append(": ").append(transaction.getQuantity())
                                    .append(" ").append(transaction.getUnit())
                                    .append(" (").append(transaction.getUserName()).append(")\n");
                        }

                        new AlertDialog.Builder(DashboardStorekeeper.this)
                                .setTitle("ИСТОРИЯ ИНВЕНТАРИЗАЦИЙ")
                                .setMessage(message.toString())
                                .setPositiveButton("ОК", null)
                                .show();
                    }
                });
            }
        }).start();
    }

    // ОБНОВЛЕННЫЙ МЕТОД ДЛЯ ПРИЕМА МАТЕРИАЛОВ С ДОБАВЛЕНИЕМ НОВОГО
    private void showReceiveMaterialsDialog() {
        // Получаем все материалы
        List<Material> allMaterials = getAllMaterials();

        if (allMaterials.isEmpty()) {
            // Если материалов нет, сразу показываем диалог добавления
            Toast.makeText(this, "Нет материалов в базе. Добавьте новый материал.", Toast.LENGTH_SHORT).show();
            showAddNewMaterialDialog();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ПРИНЯТЬ МАТЕРИАЛЫ НА СКЛАД");

        // Создаем список материалов + кнопка добавления нового
        String[] materialNames = new String[allMaterials.size() + 1];
        for (int i = 0; i < allMaterials.size(); i++) {
            Material material = allMaterials.get(i);
            String status = "";
            // Добавляем пометку если низкий запас
            if (material.getCurrentStock() <= material.getMinStock() && material.getMinStock() > 0) {
                status = " ⚠️";
            }
            materialNames[i] = material.getName() +
                    " (" + material.getCurrentStock() + " " + material.getUnit() + ")" +
                    status;
        }

        // Последний элемент - кнопка добавления нового материала
        materialNames[allMaterials.size()] = "➕ ДОБАВИТЬ НОВЫЙ МАТЕРИАЛ";

        builder.setItems(materialNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == allMaterials.size()) {
                    // Нажата кнопка добавления нового материала
                    showAddNewMaterialDialog();
                } else {
                    Material selectedMaterial = allMaterials.get(which);
                    showReceiveMaterialQuantityDialog(selectedMaterial);
                }
            }
        });

        builder.setNegativeButton("ОТМЕНА", null);
        builder.show();
    }

    private void showReceiveMaterialQuantityDialog(Material material) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Прием материала: " + material.getName());

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Количество для приема");
        builder.setView(input);

        builder.setPositiveButton("ПРИНЯТЬ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                try {
                    double quantity = Double.parseDouble(inputText);
                    if (quantity > 0) {
                        double newStock = material.getCurrentStock() + quantity;
                        updateMaterialStock(material.getId(), newStock, "receive");

                        recordMaterialTransaction(material.getId(), quantity, "receive",
                                "Прием на склад от поставщика");

                        Toast.makeText(DashboardStorekeeper.this,
                                "Материал принят на склад!", Toast.LENGTH_SHORT).show();
                        loadStorekeeperData();
                    } else {
                        Toast.makeText(DashboardStorekeeper.this,
                                "Количество должно быть больше 0", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(DashboardStorekeeper.this,
                            "Введите корректное число", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("ОТМЕНА", null);
        builder.show();
    }

    // НОВЫЙ МЕТОД ДЛЯ ДОБАВЛЕНИЯ МАТЕРИАЛА
    private void showAddNewMaterialDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ДОБАВЛЕНИЕ НОВОГО МАТЕРИАЛА");

        // Используем существующий layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_material, null);
        builder.setView(dialogView);

        final EditText etMaterialName = dialogView.findViewById(R.id.etMaterialName);
        final EditText etMaterialUnit = dialogView.findViewById(R.id.etMaterialUnit);
        final EditText etInitialStock = dialogView.findViewById(R.id.etInitialStock);
        final EditText etMinStock = dialogView.findViewById(R.id.etMinStock);

        builder.setPositiveButton("ДОБАВИТЬ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etMaterialName.getText().toString().trim();
                String unit = etMaterialUnit.getText().toString().trim();
                String initialStockStr = etInitialStock.getText().toString().trim();
                String minStockStr = etMinStock.getText().toString().trim();

                if (name.isEmpty() || unit.isEmpty()) {
                    Toast.makeText(DashboardStorekeeper.this,
                            "Заполните название и единицу измерения", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double initialStock = initialStockStr.isEmpty() ? 0 : Double.parseDouble(initialStockStr);
                    double minStock = minStockStr.isEmpty() ? 0 : Double.parseDouble(minStockStr);

                    boolean success = addNewMaterial(name, unit, initialStock, minStock);

                    if (success) {
                        Toast.makeText(DashboardStorekeeper.this,
                                "Материал добавлен успешно!", Toast.LENGTH_SHORT).show();
                        loadStorekeeperData(); // Перезагружаем данные

                        // Предлагаем принять начальный запас
                        if (initialStock > 0) {
                            new AlertDialog.Builder(DashboardStorekeeper.this)
                                    .setTitle("Начальный запас")
                                    .setMessage("Принять " + initialStock + " " + unit + " материала '" + name + "' на склад?")
                                    .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Находим только что добавленный материал
                                            Material newMaterial = getMaterialByName(name);
                                            if (newMaterial != null) {
                                                showReceiveMaterialQuantityDialog(newMaterial);
                                            }
                                        }
                                    })
                                    .setNegativeButton("ПОЗЖЕ", null)
                                    .show();
                        }
                    } else {
                        Toast.makeText(DashboardStorekeeper.this,
                                "Ошибка добавления материала", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(DashboardStorekeeper.this,
                            "Введите корректные числовые значения", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("ОТМЕНА", null);
        builder.show();
    }

    private boolean addNewMaterial(String name, String unit, double initialStock, double minStock) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        try {
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Проверяем, нет ли уже материала с таким названием
            Cursor cursor = db.rawQuery(
                    "SELECT id FROM materials WHERE name = ?",
                    new String[]{name}
            );

            if (cursor != null && cursor.moveToFirst()) {
                Toast.makeText(this, "Материал с таким названием уже существует", Toast.LENGTH_SHORT).show();
                cursor.close();
                return false;
            }
            if (cursor != null) {
                cursor.close();
            }

            android.content.ContentValues values = new android.content.ContentValues();
            values.put("name", name);
            values.put("unit", unit);
            values.put("current_stock", initialStock);
            values.put("min_stock", minStock);
            values.put("created_at", getCurrentDateTime());

            long result = db.insert("materials", null, values);

            // Записываем в историю если есть начальный запас
            if (result != -1 && initialStock > 0) {
                // Получаем ID добавленного материала
                cursor = db.rawQuery("SELECT last_insert_rowid()", null);
                int materialId = -1;
                if (cursor != null && cursor.moveToFirst()) {
                    materialId = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }

                if (materialId != -1) {
                    // Записываем в material_usage
                    android.content.ContentValues usageValues = new android.content.ContentValues();
                    usageValues.put("order_id", 1);
                    usageValues.put("material_id", materialId);
                    usageValues.put("quantity_used", initialStock);
                    usageValues.put("usage_date", getCurrentDateTime());
                    usageValues.put("user_id", currentUser != null ? currentUser.getId() : 1);

                    db.insert("material_usage", null, usageValues);

                    // Записываем в историю транзакций
                    recordMaterialTransaction(materialId, initialStock, "receive",
                            "Первоначальное добавление материала на склад: " + name);
                }
            }

            return result != -1;
        } catch (Exception e) {
            Log.e("DashboardStorekeeper", "Ошибка добавления материала: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Ошибка добавления материала: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private Material getMaterialByName(String name) {
        List<Material> materials = getAllMaterials();
        for (Material material : materials) {
            if (material.getName().equals(name)) {
                return material;
            }
        }
        return null;
    }

    private void showIssueMaterialsDialog() {
        List<Material> allMaterials = getAllMaterials();

        if (allMaterials.isEmpty()) {
            Toast.makeText(this, "Нет материалов в базе данных", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] materialNames = new String[allMaterials.size()];
        for (int i = 0; i < allMaterials.size(); i++) {
            Material material = allMaterials.get(i);
            String status = "";
            // Добавляем пометку если низкий запас
            if (material.getCurrentStock() <= material.getMinStock() && material.getMinStock() > 0) {
                status = " ⚠️";
            }
            materialNames[i] = material.getName() +
                    " (" + material.getCurrentStock() + " " + material.getUnit() + ")" + status;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ВЫДАТЬ МАТЕРИАЛЫ СО СКЛАДА");
        builder.setItems(materialNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Material selectedMaterial = allMaterials.get(which);
                showIssueMaterialQuantityDialog(selectedMaterial);
            }
        });
        builder.setNegativeButton("ОТМЕНА", null);
        builder.show();
    }

    private void showIssueMaterialQuantityDialog(Material material) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выдача материала: " + material.getName());

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Количество для выдачи");
        builder.setView(input);

        builder.setPositiveButton("ВЫДАТЬ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                try {
                    double quantity = Double.parseDouble(inputText);
                    if (quantity > 0) {
                        if (quantity <= material.getCurrentStock()) {
                            double newStock = material.getCurrentStock() - quantity;
                            updateMaterialStock(material.getId(), newStock, "issue");

                            recordMaterialTransaction(material.getId(), quantity, "issue",
                                    "Выдача со склада в производство");

                            Toast.makeText(DashboardStorekeeper.this,
                                    "Материал выдан со склада!", Toast.LENGTH_SHORT).show();
                            loadStorekeeperData();
                        } else {
                            Toast.makeText(DashboardStorekeeper.this,
                                    "Недостаточно материала на складе!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DashboardStorekeeper.this,
                                "Количество должно быть больше 0", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(DashboardStorekeeper.this,
                            "Введите корректное число", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("ОТМЕНА", null);
        builder.show();
    }

    // === МЕТОДЫ РАБОТЫ С БАЗОЙ ДАННЫХ ===

    private List<Material> getAllMaterials() {
        List<Material> materials = new ArrayList<>();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT id, name, unit, current_stock, min_stock FROM materials ORDER BY name",
                    null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Material material = new Material();
                    material.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    material.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    material.setUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
                    material.setCurrentStock(cursor.getDouble(cursor.getColumnIndexOrThrow("current_stock")));
                    material.setMinStock(cursor.getDouble(cursor.getColumnIndexOrThrow("min_stock")));
                    materials.add(material);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return materials;
    }

    private boolean updateMaterialStock(int materialId, double newStock, String operationType) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        try {
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Получаем старое значение для записи в историю
            double oldStock = 0;
            Cursor cursor = db.rawQuery("SELECT current_stock FROM materials WHERE id = ?",
                    new String[]{String.valueOf(materialId)});
            if (cursor != null && cursor.moveToFirst()) {
                oldStock = cursor.getDouble(0);
            }
            if (cursor != null) {
                cursor.close();
            }

            // Обновляем текущий запас
            android.content.ContentValues values = new android.content.ContentValues();
            values.put("current_stock", newStock);

            int rowsAffected = db.update("materials", values, "id = ?",
                    new String[]{String.valueOf(materialId)});

            if (rowsAffected > 0) {
                // Записываем в material_usage
                android.content.ContentValues usageValues = new android.content.ContentValues();
                usageValues.put("order_id", 1);
                usageValues.put("material_id", materialId);
                usageValues.put("quantity_used", operationType.equals("receive") ? newStock : -newStock);
                usageValues.put("usage_date", getCurrentDateTime());
                usageValues.put("user_id", currentUser != null ? currentUser.getId() : 1);

                db.insert("material_usage", null, usageValues);

                // ЗАПИСЫВАЕМ В ИСТОРИЮ ИНВЕНТАРИЗАЦИИ
                if ("inventory".equals(operationType)) {
                    android.content.ContentValues transValues = new android.content.ContentValues();
                    transValues.put("material_id", materialId);
                    transValues.put("quantity", newStock);
                    transValues.put("transaction_type", "inventory");
                    transValues.put("notes", "Инвентаризация: было " + oldStock + ", стало " + newStock);
                    transValues.put("user_id", currentUser != null ? currentUser.getId() : 1);
                    transValues.put("transaction_date", getCurrentDateTime());

                    // Создаем таблицу если не существует
                    db.execSQL(
                            "CREATE TABLE IF NOT EXISTS material_transactions (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    "material_id INTEGER NOT NULL," +
                                    "quantity DECIMAL(10,2) NOT NULL," +
                                    "transaction_type VARCHAR(20) NOT NULL," +
                                    "notes TEXT," +
                                    "user_id INTEGER NOT NULL," +
                                    "transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                                    "FOREIGN KEY (material_id) REFERENCES materials(id)," +
                                    "FOREIGN KEY (user_id) REFERENCES users(id))"
                    );

                    db.insert("material_transactions", null, transValues);

                    Log.d("DashboardStorekeeper", "Записана история инвентаризации для материала ID: " + materialId);
                }
            }

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DashboardStorekeeper", "Ошибка обновления материала: " + e.getMessage());
            Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void recordMaterialTransaction(int materialId, double quantity, String type, String notes) {
        createMaterialTransactionsTableIfNotExists();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        try {
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            android.content.ContentValues values = new android.content.ContentValues();
            values.put("material_id", materialId);
            values.put("quantity", quantity);
            values.put("transaction_type", type);
            values.put("notes", notes);
            values.put("user_id", currentUser != null ? currentUser.getId() : 1);
            values.put("transaction_date", getCurrentDateTime());

            db.insert("material_transactions", null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMaterialTransactionsTableIfNotExists() {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        try {
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS material_transactions (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "material_id INTEGER NOT NULL," +
                            "quantity DECIMAL(10,2) NOT NULL," +
                            "transaction_type VARCHAR(20) NOT NULL," +
                            "notes TEXT," +
                            "user_id INTEGER NOT NULL," +
                            "transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "FOREIGN KEY (material_id) REFERENCES materials(id)," +
                            "FOREIGN KEY (user_id) REFERENCES users(id))"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<MaterialTransaction> getInventoryHistory() {
        List<MaterialTransaction> history = new ArrayList<>();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        Cursor cursor = null;
        try {
            // Сначала пробуем получить историю из материальных транзакций
            String tableExistsQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='material_transactions'";
            cursor = dbHelper.getReadableDatabase().rawQuery(tableExistsQuery, null);
            boolean tableExists = cursor != null && cursor.moveToFirst();
            if (cursor != null) {
                cursor.close();
            }

            if (tableExists) {
                // Таблица существует, ищем инвентаризации
                cursor = dbHelper.getReadableDatabase().rawQuery(
                        "SELECT mt.*, m.name as material_name, m.unit, u.name as user_name " +
                                "FROM material_transactions mt " +
                                "JOIN materials m ON mt.material_id = m.id " +
                                "JOIN users u ON mt.user_id = u.id " +
                                "WHERE mt.transaction_type = 'inventory' " +
                                "ORDER BY mt.transaction_date DESC LIMIT 10",
                        null
                );

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MaterialTransaction transaction = new MaterialTransaction();
                        transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                        transaction.setMaterialId(cursor.getInt(cursor.getColumnIndexOrThrow("material_id")));
                        transaction.setMaterialName(cursor.getString(cursor.getColumnIndexOrThrow("material_name")));
                        transaction.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")));
                        transaction.setUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
                        transaction.setTransactionType(cursor.getString(cursor.getColumnIndexOrThrow("transaction_type")));
                        transaction.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
                        transaction.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                        transaction.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));

                        String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("transaction_date"));
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            transaction.setTransactionDate(sdf.parse(dateStr));
                        } catch (Exception e) {
                            transaction.setTransactionDate(new Date());
                        }

                        history.add(transaction);
                    }
                }
            }

            // Если истории нет в material_transactions, покажем альтернативные данные
            if (history.isEmpty()) {
                // Можем показать последние изменения в материалах из material_usage
                cursor = dbHelper.getReadableDatabase().rawQuery(
                        "SELECT mu.id, mu.material_id, m.name as material_name, m.unit, " +
                                "mu.quantity_used as quantity, mu.usage_date as transaction_date, " +
                                "u.name as user_name, 'usage' as transaction_type, '' as notes " +
                                "FROM material_usage mu " +
                                "JOIN materials m ON mu.material_id = m.id " +
                                "JOIN users u ON mu.user_id = u.id " +
                                "ORDER BY mu.usage_date DESC LIMIT 10",
                        null
                );

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MaterialTransaction transaction = new MaterialTransaction();
                        transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                        transaction.setMaterialId(cursor.getInt(cursor.getColumnIndexOrThrow("material_id")));
                        transaction.setMaterialName(cursor.getString(cursor.getColumnIndexOrThrow("material_name")));
                        transaction.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")));
                        transaction.setUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
                        transaction.setTransactionType(cursor.getString(cursor.getColumnIndexOrThrow("transaction_type")));
                        transaction.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
                        transaction.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));

                        String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("transaction_date"));
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            transaction.setTransactionDate(sdf.parse(dateStr));
                        } catch (Exception e) {
                            transaction.setTransactionDate(new Date());
                        }

                        history.add(transaction);
                    }
                }
            }

        } catch (Exception e) {
            Log.e("DashboardStorekeeper", "Ошибка получения истории инвентаризации: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return history;
    }

    private DatabaseHelper.User getUserById(int id) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT * FROM users WHERE id = ?",
                    new String[]{String.valueOf(id)}
            );

            if (cursor != null && cursor.moveToFirst()) {
                DatabaseHelper.User user = new DatabaseHelper.User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")),
                        cursor.getString(cursor.getColumnIndexOrThrow("brigade")),
                        cursor.getString(cursor.getColumnIndexOrThrow("position")),
                        cursor.getString(cursor.getColumnIndexOrThrow("avatar_url"))
                );
                return user;
            }
        } catch (Exception e) {
            Log.e("DashboardStorekeeper", "Ошибка получения пользователя: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Выход из системы")
                .setMessage("Вы действительно хотите выйти?")
                .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(DashboardStorekeeper.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("ОТМЕНА", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DashboardStorekeeper", "onResume - перезагружаем данные");
        loadStorekeeperData();
    }

    @Override
    protected void onDestroy() {
        Log.d("DashboardStorekeeper", "onDestroy");
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    // === ВНУТРЕННИЕ КЛАССЫ ===

    public static class Material {
        private int id;
        private String name;
        private String unit;
        private double currentStock;
        private double minStock;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public double getCurrentStock() { return currentStock; }
        public void setCurrentStock(double currentStock) { this.currentStock = currentStock; }

        public double getMinStock() { return minStock; }
        public void setMinStock(double minStock) { this.minStock = minStock; }
    }

    public static class MaterialTransaction {
        private int id;
        private int materialId;
        private String materialName;
        private double quantity;
        private String unit;
        private String transactionType;
        private String notes;
        private int userId;
        private String userName;
        private Date transactionDate;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getMaterialId() { return materialId; }
        public void setMaterialId(int materialId) { this.materialId = materialId; }

        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }

        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public Date getTransactionDate() { return transactionDate; }
        public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }
    }
}