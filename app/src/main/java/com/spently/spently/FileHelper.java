package com.spently.spently;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileHelper {

    public static void writeToFile(Context context, String filename, String data, int mode) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, mode));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> readFromFile(Context context, String filename) {
        ArrayList<String> result = new ArrayList<>();
        try {
            InputStream inputStream = context.openFileInput(filename);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                while ((receiveString = bufferedReader.readLine()) != null) {
                    result.add(receiveString);
                }
                inputStream.close();
            }
        }
        catch (Exception e) {
            //File does not exist
        }
        return result;
    }

    public static void logFile (Context context, String filename) {
        StringBuilder result = new StringBuilder();
        try {
            InputStream inputStream = context.openFileInput(filename);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                while ((receiveString = bufferedReader.readLine()) != null) {
                    result.append(receiveString + "\n");
                }
                inputStream.close();
            }
        }
        catch (Exception e) {
        }
        Log.d(filename, "\n" + result);
    }

    public static void removeItem (Context context, String filename, String item) {
        ArrayList<String> file = readFromFile(context, filename);
        writeToFile(context, filename, "", context.MODE_PRIVATE);
        for (String entry : file) {
            if (!entry.equals(item)) {
                writeToFile(context, filename, entry + "\n", context.MODE_APPEND);
            }
        }
    }

    public static boolean containsItem(Context context, String filename, String item) {
        ArrayList<String> file = readFromFile(context, filename);
        for (String entry : file) {
            if (entry.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public static void updateItem (Context context, String filename, String item, String newItem) {
        ArrayList<String> file = readFromFile(context, filename);
        writeToFile(context, filename, "", context.MODE_PRIVATE);
        for (String entry : file) {
            if (!entry.equals(item)) {
                writeToFile(context, filename, entry + "\n", context.MODE_APPEND);
            }
            else {
                writeToFile(context, filename, newItem + "\n", context.MODE_APPEND);
            }
        }
    }
}
