package com.spently.spently;

import java.util.ArrayList;

public class TotalFinder {
    public static double totalFinder(String inputText) {
        String[] texts = inputText.trim().split("\\s+");
        ArrayList<Double> prices = new ArrayList<>();
        for (String text : texts) {
            String textFragment = text;
            if (text.startsWith("$")) {
                textFragment = text.substring(1);
            }
            if (textFragment.length() >= 4) {
                if (textFragment.substring(textFragment.length() - 3, textFragment.length() - 2).equals(".")) {
                    try {
                        prices.add(Double.parseDouble(textFragment));
                    } catch (NumberFormatException e) {
                        //Not a price
                    }
                }
            }
        }
        if (prices.size() == 0) {
            return -1.0;
        }
        double total = prices.get(0);
        for (double price : prices) {
            if (price > total) {
                total = price;
            }
        }
        return total;
    }
}
