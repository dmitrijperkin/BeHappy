package com.dmitrij.behappy.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StatsManager {
    private static StatsManager instance;
    private static final int MAX = 60;

    private final LinkedList<Float> cpuHistory = new LinkedList<>();
    private final LinkedList<Float> ramHistory = new LinkedList<>();

    private StatsManager() {}

    public static synchronized StatsManager getInstance() {
        if (instance == null) {
            instance = new StatsManager();
        }
        return instance;
    }

    public synchronized void addCpuSample(float val) {
        cpuHistory.add(val);
        if (cpuHistory.size() > MAX) cpuHistory.removeFirst();
    }

    public synchronized void addRamSample(float val) {
        ramHistory.add(val);
        if (ramHistory.size() > MAX) ramHistory.removeFirst();
    }

    public synchronized List<Float> getCpuHistory() {
        return new ArrayList<>(cpuHistory);
    }

    public synchronized List<Float> getRamHistory() {
        return new ArrayList<>(ramHistory);
    }

    public synchronized List<Float> getProcessorUsageList() {
        return getCpuHistory();
    }

    public synchronized List<Float> getMemoryUsageList() {
        return getRamHistory();
    }
}
