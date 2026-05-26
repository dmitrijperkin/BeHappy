package com.dmitrij.behappy.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
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

public class StatsDetailActivity extends AppCompatActivity {
    public static final String EXTRA_TYPE = "type";
    public static final String TYPE_CPU = "cpu";
    public static final String TYPE_RAM = "ram";

    private LineChart chart;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_detail);

        type = getIntent().getStringExtra(EXTRA_TYPE);
        if (type == null) type = TYPE_CPU;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(type.equals(TYPE_CPU) ? R.string.status_cpu : R.string.status_ram);
        }

        chart = findViewById(R.id.chart);
        TextView description = findViewById(R.id.text_description);
        
        description.setText(type.equals(TYPE_CPU) ? R.string.desc_cpu_history : R.string.desc_ram_history);

        setupChart();
        updateData();
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);

        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawLabels(false);
        chart.getAxisRight().setEnabled(false);
        
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.getAxisLeft().setTextColor(Color.GRAY);
        chart.getAxisLeft().setDrawGridLines(true);
    }

    private void updateData() {
        List<Float> history = type.equals(TYPE_CPU) 
            ? StatsManager.getInstance().getCpuHistory() 
            : StatsManager.getInstance().getRamHistory();

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            entries.add(new Entry(i, history.get(i)));
        }

        int primaryColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, Color.CYAN);
        int surfaceColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.WHITE);

        LineDataSet dataSet = new LineDataSet(entries, type.toUpperCase());
        dataSet.setColor(primaryColor);
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(primaryColor);
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getAxisLeft().setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY));
        chart.invalidate();
    }
}
