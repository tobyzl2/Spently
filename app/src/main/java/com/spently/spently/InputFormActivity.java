package com.spently.spently;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class InputFormActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_form);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final String total = String.format("%.2f", getIntent().getDoubleExtra("total_spent", 0.0));
        final String name = getIntent().getStringExtra("name");

        //Entry Name
        final EditText nameField = findViewById(R.id.input_form_entry_name);
        nameField.setText(name);

        //Entry Label
        final AutoCompleteTextView labelField = findViewById(R.id.input_form_entry_label);
        String[] autoCompleteLabels;
        try {
            ArrayList<String> labels = FileHelper.readFromFile(InputFormActivity.this, "labels");
            ArrayList<String> labelsNoDuplicates = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                if (!labelsNoDuplicates.contains(labels.get(i))) {
                    labelsNoDuplicates.add(labels.get(i));
                }
            }
            autoCompleteLabels = labelsNoDuplicates.toArray(new String[labelsNoDuplicates.size()]);
        } catch (Exception e) {
            autoCompleteLabels = new String[0];
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, autoCompleteLabels);
        labelField.setAdapter(arrayAdapter);

        //Set Total Fields
        final EditText totalDollarsField = findViewById(R.id.input_form_total_dollars_field);
        totalDollarsField.setText(total.substring(0, total.length() - 3));
        final EditText totalCentsField = findViewById(R.id.input_form_total_cents_field);
        totalCentsField.setText(total.substring(total.length() - 2, total.length()));

        //Set Month and Year
        final EditText monthField = findViewById(R.id.input_form_month_field);
        monthField.setText("" + MonthTracker.getMonth());
        final EditText yearField = findViewById(R.id.input_form_year_field);
        yearField.setText("" + MonthTracker.getYear());

        Button submitButton = findViewById(R.id.input_form_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                String nameVal = nameField.getText().toString().trim();
                String labelVal = labelField.getText().toString().trim();
                String totalVal = getMoneyVal(totalDollarsField, totalCentsField);
                String monthYearVal = getMonthYearVal(monthField, yearField);
                if (labelVal.equals("")) {
                    Toast.makeText(getApplicationContext(), "Invalid Label Value.", Toast.LENGTH_LONG).show();
                } else if (totalVal == null) {
                    Toast.makeText(getApplicationContext(), "Invalid Total Value.", Toast.LENGTH_LONG).show();
                } else if (monthYearVal == null) {
                    Toast.makeText(getApplicationContext(), "Invalid Month/Year Value.", Toast.LENGTH_LONG).show();
                } else {
                    if (nameVal.equals("")) {
                        nameVal = "Unnamed Entry";
                    }
                    FileHelper.writeToFile(getApplicationContext(), "entries",
                            ts + "_" + nameVal + "_" + totalVal + "_" + monthYearVal + "_" + labelVal + "\n", MODE_APPEND);
                    FileHelper.writeToFile(getApplicationContext(), "labels", labelVal + "\n", MODE_APPEND);
                    String currentMonthVal = MonthTracker.getMonth() + "/" + MonthTracker.getYear();
                    if (monthYearVal.equals(currentMonthVal)) {
                        try {
                            String oldTotal = FileHelper.readFromFile(getApplicationContext(), "total").get(0);
                            FileHelper.writeToFile(getApplicationContext(), "total",
                                    "" + String.format("%.2f", (Double.parseDouble(oldTotal) + Double.parseDouble(totalVal))), MODE_PRIVATE);
                        } catch (Exception e) {
                            FileHelper.writeToFile(getApplicationContext(), "total",
                                    "" + String.format("%.2f", Double.parseDouble(totalVal)), MODE_PRIVATE);
                        }
                    } else {
                        String[] monthYear = monthYearVal.split("/");
                        MonthTracker.updateMonthLog(getApplicationContext(), monthYear[0], monthYear[1], totalVal);
                    }
                    Intent toMain = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(toMain);
                }
            }
        });
    }

    public String getMoneyVal(EditText dollarsField, EditText centsField) {
        String totalDollarsVal = dollarsField.getText().toString();
        String totalCentsVal = centsField.getText().toString();
        if (totalDollarsVal.length() == 0 && totalCentsVal.length() == 0) {
            return null;
        } else if (totalDollarsVal.length() == 0) {
            totalDollarsVal = "0";
        } else if (totalCentsVal.length() == 0) {
            totalCentsVal = "00";
        } else if (totalCentsVal.length() == 1) {
            totalCentsVal = totalCentsVal + "0";
        }
        return totalDollarsVal + "." + totalCentsVal;
    }

    public String getMonthYearVal(EditText monthField, EditText yearField) {
        String monthVal = monthField.getText().toString();
        String yearVal = yearField.getText().toString();
        if (monthVal.length() == 0 || yearVal.length() == 0) {
            return null;
        } else if (Integer.parseInt(monthVal) < 1 || Integer.parseInt(monthVal) > 12) {
            return null;
        } else if (Integer.parseInt(yearVal) > Integer.parseInt(MonthTracker.getYear()) || Integer.parseInt(yearVal) < Integer.parseInt(MonthTracker.getYear()) - 1) {
            return null;
        } else if (Integer.parseInt(yearVal) == Integer.parseInt(MonthTracker.getYear()) && Integer.parseInt(monthVal) > Integer.parseInt(MonthTracker.getMonth())) {
            return null;
        }
        return monthVal + "/" + yearVal;
    }
}
