package com.dmitrij.behappy.widget;

import android.graphics.Color;
import com.dmitrij.behappy.data.StatsManager;
import java.util.List;

public class RamWidget extends BaseGraphWidget {
    @Override
    protected String getTitle() {
        return "RAM Usage";
    }

    @Override
    protected List<Float> getHistory() {
        return StatsManager.getInstance().getRamHistory();
    }

    @Override
    protected int getGraphColor() {
        return Color.parseColor("#66BB6A");
    }
}
