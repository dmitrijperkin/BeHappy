package com.dmitrij.behappy.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.StatsManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

public class StatsDetailActivity extends BaseActivity {
    public static final String EXTRA_TYPE = "type";
    public static final String TYPE_CPU = "cpu";
    public static final String TYPE_RAM = "ram";

    private LineChart statsLineChart;
    private String statsCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_detail);

        statsCategory = getIntent().getStringExtra(EXTRA_TYPE);
        if (statsCategory == null) statsCategory = TYPE_CPU;

        Toolbar navigationBar = findViewById(R.id.toolbar);
        setSupportActionBar(navigationBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(statsCategory.equals(TYPE_CPU) ? R.string.status_cpu : R.string.status_ram);
        }

        statsLineChart = findViewById(R.id.chart);
        TextView statsDescription = findViewById(R.id.text_description);
        
        statsDescription.setText(statsCategory.equals(TYPE_CPU) ? R.string.desc_cpu_history : R.string.desc_ram_history);

        configureChart();
        updateChartDisplay();
    }

    private void configureChart() {
        statsLineChart.getDescription().setEnabled(false);
        statsLineChart.setTouchEnabled(true);
        statsLineChart.setDragEnabled(true);
        statsLineChart.setScaleEnabled(true);
        statsLineChart.setPinchZoom(true);
        statsLineChart.setDrawGridBackground(false);
        statsLineChart.getLegend().setEnabled(false);

        statsLineChart.getXAxis().setDrawGridLines(false);
        statsLineChart.getXAxis().setDrawLabels(false);
        statsLineChart.getAxisRight().setEnabled(false);
        
        statsLineChart.getAxisLeft().setAxisMinimum(0f);
        statsLineChart.getAxisLeft().setAxisMaximum(100f);
        statsLineChart.getAxisLeft().setTextColor(Color.GRAY);
        statsLineChart.getAxisLeft().setDrawGridLines(true);
    }

    private void updateChartDisplay() {
        List<Float> usageDataList = statsCategory.equals(TYPE_CPU) 
            ? StatsManager.getInstance().getProcessorUsageList() 
            : StatsManager.getInstance().getMemoryUsageList();

        List<Entry> chartEntries = new ArrayList<>();
        for (int index = 0; index < usageDataList.size(); index++) {
            chartEntries.add(new Entry(index, usageDataList.get(index)));
        }

        int accentColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, Color.CYAN);

        LineDataSet usageDataSet = new LineDataSet(chartEntries, statsCategory.toUpperCase());
        usageDataSet.setColor(accentColor);
        usageDataSet.setLineWidth(3f);
        usageDataSet.setDrawCircles(false);
        usageDataSet.setDrawValues(false);
        usageDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        usageDataSet.setDrawFilled(true);
        usageDataSet.setFillColor(accentColor);
        usageDataSet.setFillAlpha(50);

        LineData chartData = new LineData(usageDataSet);
        statsLineChart.setData(chartData);
        statsLineChart.getAxisLeft().setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY));
        statsLineChart.invalidate();
    }
}
