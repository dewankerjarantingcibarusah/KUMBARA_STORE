package com.dkr.kumbarastore.pembeli;
import java.text.NumberFormat;
import java.util.Locale;


public class Utils {
    public static String formatRupiah(double harga) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(harga);
    }
    public static double cleanAndParsePrice(String price) {
        try {
            // Clean the price string and parse it to double
            String cleanedPrice = price.replaceAll("[^\\d.]", ""); // Remove non-numeric characters
            return Double.parseDouble(cleanedPrice);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0.0; // Default value in case of parsing error
        }
    }

    public static String formatRupiah1(String harga) {
        try {
            double hargaDouble = Double.parseDouble(harga.replaceAll("[^\\d.]", ""));
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            formatter.setMaximumFractionDigits(0); // No decimal points
            return formatter.format(hargaDouble);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return harga; // Return original string if format fails
        }
    }
}
