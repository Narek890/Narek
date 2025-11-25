package com.example.clothes;

import java.util.ArrayList;
import java.util.List;

public class StorekeeperStats {
    public List<Material> lowStockMaterials = new ArrayList<>();
    public String recentUsage = "";

    // Внутренний статический класс Material
    public static class Material {
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
}