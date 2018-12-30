package com.spently.spently;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

public class InputFormActivity extends AppCompatActivity {
    Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_form);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final String total = String.format( "%.2f", getIntent().getDoubleExtra("total_spent", 0.0));

        //Set Total Fields
        final EditText totalDollarsField = findViewById(R.id.input_form_total_dollars_field);
        totalDollarsField.setText(total.substring(0,total.length() - 3));
        final EditText totalCentsField = findViewById(R.id.input_form_total_cents_field);
        totalCentsField.setText(total.substring(total.length() - 2, total.length()));

        //Set Month and Year
        calendar = Calendar.getInstance(TimeZone.getDefault());
        final EditText monthField = findViewById(R.id.input_form_month_field);
        monthField.setText("" + (calendar.get(Calendar.MONTH) + 1));
        final EditText yearField = findViewById(R.id.input_form_year_field);
        yearField.setText("" + calendar.get(Calendar.YEAR));

        //Tax Layout
        final LinearLayout taxLayout = findViewById(R.id.input_form_tax_layout);
        final RadioGroup taxRadio = findViewById(R.id.input_form_tax_radio);
        final EditText taxDollarsField = findViewById(R.id.input_form_tax_dollars_field);
        final EditText taxCentsField = findViewById(R.id.input_form_tax_cents_field);
        final Switch taxSwitch = findViewById(R.id.input_form_tax_switch);
        taxSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    taxLayout.setVisibility(View.VISIBLE);
                    taxRadio.setVisibility(View.VISIBLE);
                } else {
                    taxLayout.setVisibility(View.GONE);
                    taxRadio.setVisibility(View.GONE);
                }
            }
        });

        //Tip Layout
        final LinearLayout tipLayout = findViewById(R.id.input_form_tip_layout);
        final RadioGroup tipRadio = findViewById(R.id.input_form_tip_radio);
        final EditText tipDollarsField = findViewById(R.id.input_form_tip_dollars_field);
        final EditText tipCentsField = findViewById(R.id.input_form_tip_cents_field);
        final Switch tipSwitch = findViewById(R.id.input_form_tip_switch);
        tipSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tipLayout.setVisibility(View.VISIBLE);
                    tipRadio.setVisibility(View.VISIBLE);
                } else {
                    tipLayout.setVisibility(View.GONE);
                    tipRadio.setVisibility(View.GONE);
                }
            }
        });
        Button submitButton = findViewById(R.id.input_form_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String totalVal = getMoneyVal(totalDollarsField, totalCentsField);
                String monthYearVal = getMonthYearVal(monthField, yearField);
                String taxVal = getMoneyVal(taxDollarsField, taxCentsField);
                String tipVal = getMoneyVal(tipDollarsField, tipCentsField);
                if (totalVal == null) {
                    Toast.makeText(getApplicationContext(), "Invalid Total Value.", Toast.LENGTH_LONG).show();
                } else if (monthYearVal == null) {
                    Toast.makeText(getApplicationContext(), "Invalid Month/Year Value.", Toast.LENGTH_LONG).show();
                } else if (taxSwitch.isChecked() && taxVal == null || taxVal != null && Double.parseDouble(taxVal) > Double.parseDouble(totalVal)) {
                    Toast.makeText(getApplicationContext(), "Invalid Tax Value.", Toast.LENGTH_LONG).show();
                } else if (tipSwitch.isChecked() && tipVal == null || tipVal != null && Double.parseDouble(tipVal) > Double.parseDouble(totalVal)) {
                    Toast.makeText(getApplicationContext(), "Invalid Tip Value.", Toast.LENGTH_LONG).show();
                } else if (taxSwitch.isChecked() && taxRadio.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getApplicationContext(), "Check a Radio Button.", Toast.LENGTH_LONG).show();
                } else if (tipSwitch.isChecked() && tipRadio.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getApplicationContext(), "Check a Radio Button.", Toast.LENGTH_LONG).show();
                } else {
                    String taxIncEx = null;
                    String tipIncEx = null;
                    if (taxSwitch.isChecked()) {
                        taxIncEx = ((RadioButton) taxRadio.findViewById(taxRadio.getCheckedRadioButtonId())).getText().toString();
                    }
                    if (tipSwitch.isChecked()) {
                        tipIncEx = ((RadioButton) tipRadio.findViewById(tipRadio.getCheckedRadioButtonId())).getText().toString();
                    }
                    FileHelper.writeToFile(getApplicationContext(), "entries",
                            totalVal + "_" + monthYearVal + "_" + taxVal + "_" + taxIncEx + "_" + tipVal + "_" + tipIncEx + "\n", MODE_APPEND);
                    try {
                        String oldTotal = FileHelper.readFromFile(getApplicationContext(), "total").get(0);
                        FileHelper.writeToFile(getApplicationContext(), "total",
                                "" + String.format( "%.2f", (Double.parseDouble(oldTotal) + Double.parseDouble(totalVal))), MODE_PRIVATE);
                    } catch (Exception e) {
                        FileHelper.writeToFile(getApplicationContext(), "total",
                                "" + String.format( "%.2f", Double.parseDouble(totalVal)), MODE_PRIVATE);
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
        } else if (Integer.parseInt(yearVal) > calendar.get(Calendar.YEAR) || Integer.parseInt(yearVal) < calendar.get(Calendar.YEAR) - 1) {
            return null;
        }
        return monthVal + "/" + yearVal;
    }
}
