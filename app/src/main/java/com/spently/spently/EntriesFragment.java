package com.spently.spently;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class EntriesFragment extends Fragment {
    View entriesLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        entriesLayout = inflater.inflate(R.layout.fragment_entries, container, false);
        buildEntriesList(inflater, container);
        return entriesLayout;
    }

    public void buildEntriesList(LayoutInflater inflater, ViewGroup container) {
        LinearLayout entryList = entriesLayout.findViewById(R.id.entries_entry_list);
        try {
            ArrayList<String> entries = FileHelper.readFromFile(getContext(), "entries");
            if (entries.size() == 0) {
                Toast.makeText(getContext(), "No Entries Avaliable.", Toast.LENGTH_LONG).show();
            }
            for (int i = entries.size() - 1; i >= 0; i--) {
                View entryView = inflater.inflate(R.layout.entry_item, container, false);
                TextView entryName = entryView.findViewById(R.id.entry_name);
                TextView entryPrice = entryView.findViewById(R.id.entry_price);
                TextView entryMonthYear = entryView.findViewById(R.id.entry_month_year);
                TextView entryLabel = entryView.findViewById(R.id.entry_label);
                ImageButton delete = entryView.findViewById(R.id.entry_delete);
                final String entry = entries.get(i);
                final String[] entryVal = entry.split("_");
                entryName.setText(entryVal[1]);
                entryPrice.setText("$" + entryVal[2]);
                entryMonthYear.setText("" + entryVal[3]);
                entryLabel.setText(entryVal[4]);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Delete?")
                                .setMessage("Would you like to delete this entry?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        FileHelper.removeItem(getContext(), "entries", entry);
                                        if (entryVal[3].equals(MonthTracker.getMonth() + "/" + MonthTracker.getYear())) {
                                            double total = Double.parseDouble(FileHelper.readFromFile(getContext(), "total").get(0));
                                            double newTotal = total - Double.parseDouble(entryVal[2]);
                                            FileHelper.writeToFile(getContext(), "total", String.format("%.2f", newTotal), MODE_PRIVATE);
                                        } else {
                                            String[] monthYear = entryVal[3].split("/");
                                            ArrayList<String> monthLogs = FileHelper.readFromFile(getContext(), "month_log");
                                            for (int i = 0; i < monthLogs.size(); i++) {
                                                String[] logItems = monthLogs.get(i).split("_");
                                                if (logItems[0].equals(monthYear[0]) && logItems[1].equals(monthYear[1])) {
                                                    double total = Double.parseDouble(FileHelper.readFromFile(getContext(), "month_log").get(i).split("_")[2]);
                                                    double newTotal = total - Double.parseDouble(entryVal[2]);
                                                    if (newTotal == 0) {
                                                        FileHelper.removeItem(getContext(), "month_log", monthLogs.get(i));
                                                    } else {
                                                        FileHelper.updateItem(getContext(), "month_log", monthLogs.get(i),
                                                                monthYear[0] + "_" + monthYear[1] + "_" + String.format("%.2f", newTotal) + "\n");
                                                    }
                                                }
                                            }
                                        }
                                        FileHelper.removeItem(getContext(), "labels", entryVal[4]);
                                        getFragmentManager().beginTransaction().replace(R.id.content_frame, new EntriesFragment()).addToBackStack(null).commit();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                    }
                });
                entryList.addView(entryView);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "No Entries Avaliable.", Toast.LENGTH_LONG).show();
        }
    }
}
