package com.udacity.stockhawk.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;

import java.util.HashSet;
import java.util.Set;

public final class PrefUtils {

    static Uri stocksUri = Contract.Quote.URI;

    private PrefUtils() {
    }

    public static Set<String> getStocks(Context context) {
        String[] DEFAULT_STOCKS_COLUMNS = new String[]{Contract.Quote.COLUMN_SYMBOL};
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        Set<String> defaultStocks = new HashSet<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            Cursor stocksCursor = context.getContentResolver().query(stocksUri,
                    DEFAULT_STOCKS_COLUMNS, null, null, null);
                while (stocksCursor.moveToNext()) {
                    String symbol = stocksCursor.getString(Contract.Quote.POSITION_SYMBOL);
                    defaultStocks.add(symbol);
                }
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(initializedKey, true);
                editor.putStringSet(stocksKey, defaultStocks);
                editor.apply();
        }
        return defaultStocks;
    }

    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        // editing the default stocks will be done via the database rather than directly
        // to the stringset
        Set<String> stocks;
        ContentValues values;
        values = new ContentValues();
        values.put(Contract.Quote.COLUMN_SYMBOL, symbol);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        if (add) {
            context.getContentResolver().insert(stocksUri, values);
            stocks = getStocks(context);
            editor.putStringSet(key, stocks);
        } else {
            context.getContentResolver().
                    delete(Contract.Quote.makeUriForStock(symbol), null, null);
            stocks = getStocks(context);
            editor.putStringSet(key, stocks);
        }
            editor.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
        Uri stockUri = Contract.Quote.makeUriForStock(symbol);
        context.getContentResolver().delete(stockUri, null, null);
    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

    public static int[] convertTimeInMillisToTime(long timeInMillis) {
        int[] time = new int[3];
        time[0] = (int)Math.ceil(timeInMillis/(1000*3600)); // hours
        double hourFract = time[0] - Math.floor(time[0]);
        time[1] = (int)Math.ceil(hourFract * 60);
        double minutesFract = time[1] - Math.floor(time[1]);
        time[2] = (int)Math.ceil(minutesFract * 60);
        return time;
    }

}
