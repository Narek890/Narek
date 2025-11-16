package com.example.clothes;


public class WorkerStats {
    public int completed = 0;
    public int defects = 0;
    public String todayAssignments = "";

    public double getDefectsPercent() {
        if (completed == 0) return 0;
        return (defects * 100.0) / completed;
    }
}
