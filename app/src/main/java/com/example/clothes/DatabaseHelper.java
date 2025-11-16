package com.example.clothes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "narek.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        copyDatabaseFromAssets();
        debugDatabaseStructure();
    }

    private void copyDatabaseFromAssets() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);

        if (!dbFile.exists()) {
            try {
                InputStream inputStream = context.getAssets().open("databases/" + DATABASE_NAME);
                File parentDir = dbFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }

                OutputStream outputStream = new FileOutputStream(dbFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

                Log.d("DatabaseHelper", "✅ База данных скопирована из assets");
            } catch (IOException e) {
                Log.e("DatabaseHelper", "❌ Ошибка копирования БД: " + e.getMessage());
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Используем существующую БД
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Не обновляем БД
    }

    // Отладка структуры БД
    public void debugDatabaseStructure() {
        SQLiteDatabase db = getReadableDatabase();

        try {
            Log.d("DatabaseDebug", "=== СТРУКТУРА БАЗЫ ДАННЫХ ===");

            // Покажем все таблицы
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            Log.d("DatabaseDebug", "📊 ТАБЛИЦЫ В БАЗЕ:");
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                Log.d("DatabaseDebug", "   - " + tableName);
            }
            cursor.close();

            // Покажем данные из таблицы users
            Log.d("DatabaseDebug", "👥 ДАННЫЕ ИЗ USERS:");
            cursor = db.rawQuery("SELECT * FROM users", null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                String brigade = cursor.getString(cursor.getColumnIndexOrThrow("brigade"));
                String position = cursor.getString(cursor.getColumnIndexOrThrow("position"));
                Log.d("DatabaseDebug", "   ID: " + id + ", Email: " + email + ", Name: " + name +
                        ", Role: " + role + ", Brigade: " + brigade + ", Position: " + position);
            }
            cursor.close();

        } catch (Exception e) {
            Log.e("DatabaseDebug", "Ошибка отладки БД: " + e.getMessage());
        }
    }

    // === МЕТОДЫ ДЛЯ АУТЕНТИФИКАЦИИ И РЕГИСТРАЦИИ ===

    // Аутентификация пользователя
    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        User user = null;

        try {
            String query = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
            Cursor cursor = db.rawQuery(query, new String[]{email, password});

            if (cursor.moveToFirst()) {
                user = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")),
                        cursor.getString(cursor.getColumnIndexOrThrow("brigade")),
                        cursor.getString(cursor.getColumnIndexOrThrow("position")),
                        cursor.getString(cursor.getColumnIndexOrThrow("avatar_url"))
                );
                Log.d("DatabaseHelper", "✅ Пользователь найден: " + user.getName());
            } else {
                Log.d("DatabaseHelper", "❌ Пользователь не найден: " + email);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка аутентификации: " + e.getMessage());
        }
        return user;
    }

    // Проверка существования email
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();

        try {
            String query = "SELECT COUNT(*) FROM users WHERE email = ?";
            Cursor cursor = db.rawQuery(query, new String[]{email});

            boolean exists = false;
            if (cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0;
            }

            cursor.close();
            Log.d("DatabaseHelper", "🔍 Проверка email " + email + ": " + (exists ? "существует" : "не существует"));
            return exists;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка проверки email: " + e.getMessage());
            return false;
        }
    }

    // Обновление пароля
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            String updateQuery = "UPDATE users SET password_hash = ?, updated_at = datetime('now') WHERE email = ?";
            db.execSQL(updateQuery, new Object[]{newPassword, email});

            Log.d("DatabaseHelper", "✅ Пароль обновлен для: " + email);
            return true;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка обновления пароля: " + e.getMessage());
            return false;
        }
    }

    // Получение пользователя по email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        User user = null;

        try {
            String query = "SELECT * FROM users WHERE email = ?";
            Cursor cursor = db.rawQuery(query, new String[]{email});

            if (cursor.moveToFirst()) {
                user = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")),
                        cursor.getString(cursor.getColumnIndexOrThrow("brigade")),
                        cursor.getString(cursor.getColumnIndexOrThrow("position")),
                        cursor.getString(cursor.getColumnIndexOrThrow("avatar_url"))
                );
            }

            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка получения пользователя: " + e.getMessage());
        }

        return user;
    }

    // Регистрация пользователя
    public boolean registerUser(String email, String password, String name, String brigade, String position) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            // Сначала проверяем, нет ли уже пользователя с таким email
            if (isEmailExists(email)) {
                Log.e("DatabaseHelper", "❌ Пользователь с email " + email + " уже существует");
                return false;
            }

            String insertQuery = "INSERT INTO users (email, password_hash, name, brigade, position, role) " +
                    "VALUES (?, ?, ?, ?, ?, 'worker')";
            db.execSQL(insertQuery, new Object[]{email, password, name, brigade, position});
            Log.d("DatabaseHelper", "✅ Пользователь зарегистрирован: " + email);
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    // === ДАННЫЕ ДЛЯ WORKER ===
    public WorkerStats getWorkerStats(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        WorkerStats stats = new WorkerStats();

        try {
            // Статистика за сегодня
            String query = "SELECT " +
                    "COALESCE(SUM(a.actual_quantity), 0) as completed, " +
                    "COALESCE(SUM(a.defects), 0) as defects " +
                    "FROM assignments a " +
                    "WHERE a.user_id = ? AND date(a.start_time) = date('now')";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                stats.completed = cursor.getInt(0);
                stats.defects = cursor.getInt(1);
                Log.d("DatabaseHelper", "📊 Статистика worker: completed=" + stats.completed + ", defects=" + stats.defects);
            }
            cursor.close();

            // Если нет данных за сегодня, берем общую статистику
            if (stats.completed == 0) {
                query = "SELECT " +
                        "COALESCE(SUM(a.actual_quantity), 0) as completed, " +
                        "COALESCE(SUM(a.defects), 0) as defects " +
                        "FROM assignments a " +
                        "WHERE a.user_id = ?";

                cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
                if (cursor.moveToFirst()) {
                    stats.completed = cursor.getInt(0);
                    stats.defects = cursor.getInt(1);
                }
                cursor.close();
            }

            // Задания на сегодня
            stats.todayAssignments = getTodayAssignments(userId);

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка получения статистики worker: " + e.getMessage());
        }
        return stats;
    }

    private String getTodayAssignments(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder assignments = new StringBuilder();

        try {
            String query = "SELECT o.name as operation_name, " +
                    "a.planned_quantity, a.actual_quantity " +
                    "FROM assignments a " +
                    "LEFT JOIN operations o ON a.operation_id = o.id " +
                    "WHERE a.user_id = ? AND (date(a.start_time) = date('now') OR a.status = 'assigned') " +
                    "ORDER BY a.created_at LIMIT 3";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
            int count = 1;
            boolean hasData = false;

            while (cursor.moveToNext()) {
                String opName = cursor.getString(cursor.getColumnIndexOrThrow("operation_name"));
                int planned = cursor.getInt(cursor.getColumnIndexOrThrow("planned_quantity"));
                int actual = cursor.getInt(cursor.getColumnIndexOrThrow("actual_quantity"));

                if (opName == null) opName = "Задание " + count;

                assignments.append(opName).append("    ")
                        .append(actual).append("/").append(planned).append(" шт\n");
                count++;
                hasData = true;
            }
            cursor.close();

            if (!hasData) {
                assignments.append("Раскрой деталей    0/50 шт\n")
                        .append("Стачать швы    0/30 шт\n")
                        .append("Обработка    0/25 шт");
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка получения заданий: " + e.getMessage());
            assignments.append("Раскрой деталей    45/50 шт\n")
                    .append("Стачать швы    30/50 шт\n")
                    .append("Обработка    52/50 шт");
        }
        return assignments.toString();
    }

    // === ДАННЫЕ ДЛЯ MASTER ===
    public MasterStats getMasterStats(int userId, String brigade) {
        SQLiteDatabase db = getReadableDatabase();
        MasterStats stats = new MasterStats();

        try {
            // Если бригада не указана, находим её по пользователю
            if (brigade == null || brigade.isEmpty()) {
                Cursor userCursor = db.rawQuery("SELECT brigade FROM users WHERE id = ?",
                        new String[]{String.valueOf(userId)});
                if (userCursor.moveToFirst()) {
                    brigade = userCursor.getString(0);
                }
                userCursor.close();
            }

            if (brigade != null && !brigade.isEmpty()) {
                // Статистика по бригаде
                String query = "SELECT " +
                        "COUNT(DISTINCT a.user_id) as workers_count, " +
                        "COALESCE(SUM(a.actual_quantity), 0) as total_completed, " +
                        "COALESCE(SUM(a.defects), 0) as total_defects " +
                        "FROM assignments a " +
                        "JOIN users u ON a.user_id = u.id " +
                        "WHERE u.brigade = ? AND date(a.start_time) >= date('now', '-7 days')";

                Cursor cursor = db.rawQuery(query, new String[]{brigade});
                if (cursor.moveToFirst()) {
                    stats.workersCount = cursor.getInt(0);
                    stats.totalCompleted = cursor.getInt(1);
                    stats.totalDefects = cursor.getInt(2);
                    Log.d("DatabaseHelper", "📊 Статистика master: workers=" + stats.workersCount +
                            ", completed=" + stats.totalCompleted + ", defects=" + stats.totalDefects);
                }
                cursor.close();

                // Работники бригады
                stats.workers = getBrigadeWorkers(brigade);
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка получения статистики master: " + e.getMessage());
        }
        return stats;
    }

    private List<Worker> getBrigadeWorkers(String brigade) {
        SQLiteDatabase db = getReadableDatabase();
        List<Worker> workers = new ArrayList<>();

        try {
            String query = "SELECT u.name, u.position, " +
                    "(SELECT COALESCE(SUM(actual_quantity), 0) FROM assignments WHERE user_id = u.id AND date(start_time) >= date('now', '-7 days')) as completed " +
                    "FROM users u WHERE u.brigade = ? AND u.role = 'worker' ORDER BY completed DESC LIMIT 5";

            Cursor cursor = db.rawQuery(query, new String[]{brigade});
            while (cursor.moveToNext()) {
                Worker worker = new Worker();
                worker.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                worker.position = cursor.getString(cursor.getColumnIndexOrThrow("position"));
                worker.completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed"));
                workers.add(worker);
                Log.d("DatabaseHelper", "👤 Работник: " + worker.name + " - " + worker.completed + " шт");
            }
            cursor.close();

            if (workers.isEmpty()) {
                workers.add(new Worker("Анна Петрова", "Швея", 127));
                workers.add(new Worker("Иван Сидоров", "Швец", 98));
                workers.add(new Worker("Мария Козлова", "Упаковщик", 156));
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка получения работников: " + e.getMessage());
            workers.add(new Worker("Анна Петрова", "Швея", 127));
            workers.add(new Worker("Иван Сидоров", "Швец", 98));
            workers.add(new Worker("Мария Козлова", "Упаковщик", 156));
        }
        return workers;
    }

    // === ДАННЫЕ ДЛЯ STOREKEEPER ===
    public StorekeeperStats getStorekeeperStats() {
        SQLiteDatabase db = getReadableDatabase();
        StorekeeperStats stats = new StorekeeperStats();
        stats.lowStockMaterials.clear();

        try {
            // Материалы с низким запасом
            String query = "SELECT name, current_stock, min_stock, unit " +
                    "FROM materials WHERE current_stock <= min_stock * 1.5 ORDER BY current_stock ASC LIMIT 5";

            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                Material material = new Material();
                material.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                material.currentStock = cursor.getDouble(cursor.getColumnIndexOrThrow("current_stock"));
                material.minStock = cursor.getDouble(cursor.getColumnIndexOrThrow("min_stock"));
                material.unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
                stats.lowStockMaterials.add(material);
                Log.d("DatabaseHelper", "📦 Материал: " + material.name + " - " + material.currentStock + " " + material.unit);
            }
            cursor.close();

            // Последние списания
            stats.recentUsage = getRecentMaterialUsage(db);

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка получения статистики storekeeper: " + e.getMessage());
        }
        return stats;
    }

    private String getRecentMaterialUsage(SQLiteDatabase db) {
        StringBuilder usage = new StringBuilder();

        try {
            String query = "SELECT m.name, mu.quantity_used, m.unit " +
                    "FROM material_usage mu " +
                    "JOIN materials m ON mu.material_id = m.id " +
                    "ORDER BY mu.usage_date DESC LIMIT 4";

            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity_used"));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));

                usage.append(name).append(" - ").append(quantity).append(" ").append(unit).append("\n");
            }
            cursor.close();

            if (usage.length() == 0) {
                usage.append("Ткань х/б - 45.2 м\nНитки #40 - 3 кат.\nПуговицы - 120 шт\nМолния - 8 шт");
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка получения списаний: " + e.getMessage());
            usage.append("Ткань х/б - 45.2 м\nНитки #40 - 3 кат.\nПуговицы - 120 шт\nМолния - 8 шт");
        }
        return usage.toString();
    }

    // === ДАННЫЕ ДЛЯ MANAGER ===
    public ManagerStats getManagerStats() {
        SQLiteDatabase db = getReadableDatabase();
        ManagerStats stats = new ManagerStats();

        try {
            // Статистика заказов
            String query = "SELECT " +
                    "COUNT(*) as total_orders, " +
                    "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed_orders, " +
                    "SUM(CASE WHEN status = 'in_progress' THEN 1 ELSE 0 END) as in_progress_orders " +
                    "FROM orders WHERE date(created_at) >= date('now', '-30 days')";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                stats.totalOrders = cursor.getInt(0);
                stats.completedOrders = cursor.getInt(1);
                stats.inProgressOrders = cursor.getInt(2);
                Log.d("DatabaseHelper", "📊 Статистика manager: total=" + stats.totalOrders +
                        ", completed=" + stats.completedOrders + ", inProgress=" + stats.inProgressOrders);
            }
            cursor.close();

            // Производительность по бригадам
            stats.brigadePerformance = getBrigadePerformance(db);

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка получения статистики manager: " + e.getMessage());
        }
        return stats;
    }

    private String getBrigadePerformance(SQLiteDatabase db) {
        StringBuilder performance = new StringBuilder();

        try {
            String query = "SELECT u.brigade, " +
                    "COALESCE(SUM(a.actual_quantity), 0) as completed, " +
                    "COALESCE(SUM(a.planned_quantity), 1) as planned " +
                    "FROM users u " +
                    "LEFT JOIN assignments a ON u.id = a.user_id AND date(a.start_time) >= date('now', '-7 days') " +
                    "WHERE u.brigade IS NOT NULL AND u.brigade != '' " +
                    "GROUP BY u.brigade " +
                    "ORDER BY completed DESC";

            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                String brigade = cursor.getString(cursor.getColumnIndexOrThrow("brigade"));
                int completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed"));
                int planned = cursor.getInt(cursor.getColumnIndexOrThrow("planned"));

                int percent = (planned > 0) ? (completed * 100) / planned : 0;
                performance.append(brigade).append(": ").append(percent).append("%\n");
                Log.d("DatabaseHelper", "🏭 Бригада: " + brigade + " - " + percent + "%");
            }
            cursor.close();

            if (performance.length() == 0) {
                performance.append("Бригада №1: 89%\nБригада №2: 76%\nБригада №3: 92%\nБригада №4: 81%");
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Ошибка получения производительности: " + e.getMessage());
            performance.append("Бригада №1: 89%\nБригада №2: 76%\nБригада №3: 92%\nБригада №4: 81%");
        }
        return performance.toString();
    }
}
