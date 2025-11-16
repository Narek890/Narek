package com.example.clothes;

import java.util.ArrayList;
import java.util.List;

public class StorekeeperStats {
    public List<Material> lowStockMaterials = new ArrayList<>();
    public String recentUsage = "";

    public StorekeeperStats() {
        // Тестовые данные по умолчанию
        lowStockMaterials.add(new Material("Нитки #40", 2.5, 5.0, "катушка"));
        lowStockMaterials.add(new Material("Пуговицы", 45.0, 100.0, "шт"));
        lowStockMaterials.add(new Material("Молния", 8.0, 15.0, "шт"));
        recentUsage = "Ткань х/б - 45.2 м\nНитки #40 - 3 кат.\nПуговицы - 120 шт";
    }
}

class Material {
    public String name;
    public double currentStock;
    public double minStock;
    public String unit;

    public Material() {}

    public Material(String name, double currentStock, double minStock, String unit) {
        this.name = name;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.unit = unit;
    }
}