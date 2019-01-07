package com.spently.spently;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class MonthTracker {
    public static boolean checkMonth(Context context) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        String month = "" + calendar.get(Calendar.MONTH) + 1;
        String year = "" + calendar.get(Calendar.YEAR);
        try {
            String currentMonth = FileHelper.readFromFile(context, "current_month").get(0);
            String currentYear = FileHelper.readFromFile(context, "current_month").get(1);
            if (!currentMonth.equals(month)) {
                String totalVal = FileHelper.readFromFile(context, "total").get(0);
                FileHelper.writeToFile(context, "month_log", currentMonth + "_" + currentYear + "_" + totalVal + "\n", context.MODE_APPEND);
                FileHelper.writeToFile(context, "current_month", month, context.MODE_PRIVATE);
                FileHelper.writeToFile(context, "total", "0.00", context.MODE_PRIVATE);
                return true;
            }
        } catch (Exception e) {
            FileHelper.writeToFile(context, "current_month", month + "\n" + year, context.MODE_PRIVATE);
        }
        return false;
    }

    public static String getMonth() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        return "" + (calendar.get(Calendar.MONTH) + 1);
    }

    public static String getYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        return "" + calendar.get(Calendar.YEAR);
    }

    public static String getPrevMonth() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.add(Calendar.MONTH, -1);
        return "" + (calendar.get(Calendar.MONTH) + 1);
    }

    public static String getPrevYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.add(Calendar.MONTH, -1);
        return "" + calendar.get(Calendar.YEAR);
    }

    public static void updateMonthLog(Context context, String month, String year, String value) {
        try {
            ArrayList<String> monthLog = FileHelper.readFromFile(context, "month_log");
            for (int i = 0; i < monthLog.size(); i++) {
                String[] monthEntry = monthLog.get(i).split("_");
                if (monthEntry[0].equals(month) && monthEntry[1].equals(year)) {
                    String newValue = "" + (Double.parseDouble(monthEntry[2]) + Double.parseDouble(value));
                    FileHelper.updateItem(context, "month_log", monthLog.get(i),
                            month + "_" + year + "_" + newValue + "\n");
                    return;
                }
            }
            FileHelper.writeToFile(context, "month_log", month + "_" + year + "_" + value + "\n", context.MODE_APPEND);
        } catch (Exception e) {
            FileHelper.writeToFile(context, "month_log", month + "_" + year + "_" + value + "\n", context.MODE_APPEND);
        }
    }
}
