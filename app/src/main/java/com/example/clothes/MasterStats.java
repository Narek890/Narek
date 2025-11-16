package com.example.clothes;

import java.util.ArrayList;
import java.util.List;

public class MasterStats {
    public int workersCount = 0;
    public int totalCompleted = 0;
    public int totalDefects = 0;
    public List<Worker> workers = new ArrayList<>();

    public double getDefectsPercent() {
        if (totalCompleted == 0) return 0;
        return (totalDefects * 100.0) / totalCompleted;
    }
}

class Worker {
    public String name;
    public String position;
    public int completed;

    public Worker() {}

    public Worker(String name, String position, int completed) {
        this.name = name;
        this.position = position;
        this.completed = completed;
    }
}
