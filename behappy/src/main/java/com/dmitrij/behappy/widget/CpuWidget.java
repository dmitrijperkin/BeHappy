package com.dmitrij.behappy.widget;

import android.graphics.Color;
import com.dmitrij.behappy.data.StatsManager;
import java.util.List;

public class CpuWidget extends BaseGraphWidget {
    @Override
    protected String getTitle() {
        return "CPU Usage";
    }

    @Override
    protected List<Float> getHistory() {
        return StatsManager.getInstance().getProcessorUsageList();
    }

    @Override
    protected int getGraphColor() {
        return Color.parseColor("#42A5F5");
    }
}
