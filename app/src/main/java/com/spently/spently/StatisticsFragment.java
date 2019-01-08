package com.spently.spently;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class StatisticsFragment extends Fragment {
    View statsLayout;
    PieChart entriesChart;
    BarChart monthChart;
    ArrayList<String> xAxisLabel = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        statsLayout = inflater.inflate(R.layout.fragment_statistics, container, false);
        entriesChart = statsLayout.findViewById(R.id.chart_entries);
        monthChart = statsLayout.findViewById(R.id.chart_months);
        initiateGoal(inflater, container);
        initiateLastMonth();
        createPieChart();
        buildLabelList(inflater, container);
//        createBarChart();
        return statsLayout;
    }

    public void initiateLastMonth() {
        ProgressBar lastMonthProgress = statsLayout.findViewById(R.id.stats_last_month_progress);
        TextView lastMonth = statsLayout.findViewById(R.id.stats_last_month_data);
        try {
            ArrayList<String> monthLog = FileHelper.readFromFile(getContext(), "month_log");
            for (int i = 0; i < monthLog.size(); i++) {
                String[] lastLog = monthLog.get(i).split("_");
                if (lastLog[0].equals(MonthTracker.getPrevMonth()) && lastLog[1].equals(MonthTracker.getPrevYear())) {
                    lastMonth.setText("$" + lastLog[2]);
                    double total = Double.parseDouble(FileHelper.readFromFile(getContext(), "total").get(0));
                    double lastTotal = Double.parseDouble(lastLog[2]);
                    lastMonthProgress.setProgress((int) (total / lastTotal * 100));
                    return;
                }
            }
            lastMonthProgress.setVisibility(View.GONE);
        } catch (Exception e) {
            lastMonthProgress.setVisibility(View.GONE);
        }
    }

    public void initiateGoal(final LayoutInflater inflater, final ViewGroup container) {
        ProgressBar goalProgress = statsLayout.findViewById(R.id.stats_goal_progress);
        try {
            double total = Double.parseDouble(FileHelper.readFromFile(getContext(), "total").get(0));
            double goal = Double.parseDouble(FileHelper.readFromFile(getContext(), "goal").get(0));
            ((TextView) statsLayout.findViewById(R.id.stats_goal_data)).setText("$" + String.format("%.2f", goal));
            ((Button) statsLayout.findViewById(R.id.stats_set_goal)).setText("Edit Goal");
            goalProgress.setProgress((int) (total / goal * 100));
        } catch (Exception e) {
            goalProgress.setVisibility(View.GONE);
        }
        statsLayout.findViewById(R.id.stats_set_goal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGoal(inflater, container);
            }
        });
    }

    public void setGoal(LayoutInflater inflater, ViewGroup container) {
        final View field = inflater.inflate(R.layout.alert_set_goal, container, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set Goal")
                .setMessage("Enter Your Spending Goal.");
        builder.setView(field);
        builder.setIcon(R.mipmap.ic_goal);
        builder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String dollars = ((EditText) field.findViewById(R.id.set_goal_dollars_field)).getText().toString();
                        String cents = ((EditText) field.findViewById(R.id.set_goal_cents_field)).getText().toString();
                        FileHelper.writeToFile(getContext(), "goal", String.format("%.2f", Double.parseDouble(dollars + "." + cents)), MODE_PRIVATE);
                        getFragmentManager().beginTransaction().replace(R.id.content_frame, new StatisticsFragment()).addToBackStack(null).commit();
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public void createPieChart() {
        entriesChart.setUsePercentValues(false);
        entriesChart.animateY(1000, Easing.EaseInOutQuad);
        entriesChart.setDragDecelerationFrictionCoef(0.99f);
        entriesChart.setHoleRadius(98);
        entriesChart.setHoleColor(424242);
        entriesChart.getLegend().setEnabled(false);
        entriesChart.getDescription().setEnabled(false);
        entriesChart.setExtraOffsets(40, 40, 40, 40);
        entriesChart.setCenterText("Spendings Per Label");
        entriesChart.setCenterTextSize(16f);
        entriesChart.setCenterTextColor(Color.WHITE);
        ArrayList<PieEntry> chartValues = new ArrayList<>();
        Map<String, Float> labelMap = getLabelMap();
        if (labelMap.isEmpty()) {
            return;
        }
        for (String key : labelMap.keySet()) {
            chartValues.add(new PieEntry(labelMap.get(key), key));
        }
        PieDataSet chartData = new PieDataSet(chartValues, "");
        chartData.setColors(ColorTemplate.JOYFUL_COLORS);
        chartData.setSliceSpace(5f);
        chartData.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        chartData.setValueLinePart1OffsetPercentage(80.f);
        chartData.setValueLinePart1Length(0.2f);
        chartData.setValueLinePart2Length(0.4f);
        chartData.setValueLineColor(Color.WHITE);
        PieData data = new PieData(chartData);
        data.setValueTextSize(0f);
        entriesChart.setData(data);
    }

    public Map<String, Float> getLabelMap() {
        ArrayList<String> entries = FileHelper.readFromFile(getContext(), "entries");
        Map<String, Float> labelMap = new HashMap<>();
        for (int i = entries.size() - 1; i >= 0; i--) {
            String[] entryData = entries.get(i).split("_");
            if (labelMap.containsKey(entryData[4])) {
                labelMap.put(entryData[4], labelMap.get(entryData[4]) + Float.parseFloat(entryData[2]));
            } else {
                labelMap.put(entryData[4], Float.parseFloat(entryData[2]));
            }
        }
        return labelMap;
    }

    public void buildLabelList(LayoutInflater inflater, ViewGroup container) {
        LinearLayout labelList = statsLayout.findViewById(R.id.stats_labels_list);
        Map<String, Float> labelMap = getLabelMap();
        if (labelMap.isEmpty()) {
            labelList.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No Statistics Available.", Toast.LENGTH_LONG).show();
        }
        for (String key : labelMap.keySet()) {
            View labelView = inflater.inflate(R.layout.label_item, container, false);
            TextView labelName = labelView.findViewById(R.id.label_name);
            TextView labelPrice = labelView.findViewById(R.id.label_price);
            labelName.setText(key);
            labelPrice.setText("$" + String.format("%.2f", labelMap.get(key)));
            labelList.addView(labelView);
        }
    }

    public void createBarChart() {
        monthChart.animateY(1000, Easing.EaseInOutQuad);
        monthChart.setTouchEnabled(false);
        YAxis yAxis = monthChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setAxisMinimum(0);

        XAxis xAxis = monthChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        monthChart.getAxisRight().setEnabled(false);
        monthChart.getLegend().setEnabled(false);
        monthChart.getDescription().setEnabled(false);

        ArrayList<BarEntry> chartValues = new ArrayList<>();
        ArrayList<String> monthLog = FileHelper.readFromFile(getContext(), "month_log");
        int start = 0;
        float xPos = 0;
        if (monthLog.size() > 13) {
            start = monthLog.size() - 13;
        }
        for (int i = start; i < monthLog.size(); i++) {
            String[] monthData = monthLog.get(i).split("_");
            chartValues.add(new BarEntry(xPos, Float.parseFloat(monthData[2])));
            xAxisLabel.add(monthData[0] + "/" + monthData[1]);
            xPos++;
        }
        xAxisLabel.add(MonthTracker.getMonth() + "/" + MonthTracker.getYear());
        chartValues.add(new BarEntry(xPos, Float.parseFloat(FileHelper.readFromFile(getContext(), "total").get(0))));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getXAxisValues()));
        xAxis.setLabelCount(xAxisLabel.size());
        BarDataSet chartData = new BarDataSet(chartValues, "");
        chartData.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData data = new BarData(chartData);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.YELLOW);
        monthChart.setData(data);
    }

    private ArrayList<String> getXAxisValues() {
        return xAxisLabel;
    }
}
