package com.example.clothes;

public class ManagerStats {
    public int totalOrders = 0;
    public int completedOrders = 0;
    public int inProgressOrders = 0;
    public String brigadePerformance = "";

    public int getCompletionPercent() {
        if (totalOrders == 0) return 0;
        return (completedOrders * 100) / totalOrders;
    }
}
