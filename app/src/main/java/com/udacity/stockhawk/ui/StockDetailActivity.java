package com.udacity.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.udacity.stockhawk.data.Contract.Quote;

import java.util.ArrayList;

/**
 * Created by melanieh on 2/15/17.
 */

public class StockDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    final String[] QUOTE_COLUMNS = {Quote._ID, Quote.COLUMN_SYMBOL, Quote.COLUMN_PRICE};
    Cursor cursor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarChart chart = new BarChart(this);
        // TODO: custom UI view for chart + text below, not layout
        setContentView(chart);
//        setContentView(R.layout.activity_detail);

//        // read history data from cursor for chart
//        cursor = getContentResolver().query(Quote.URI, QUOTE_COLUMNS, null, null, " ASC");

        // initialize ArrayList to populate with the values from the cursor for the line chart DataSetObject
        ArrayList<Entry> dataPoints = new ArrayList<>();

        // create MPAndroidChart DataSetObject
        LineDataSet dataset = new LineDataSet(dataPoints, "# of Calls");

        // chart x-axis labels
        ArrayList<String> xlabels = new ArrayList<String>();

        // placeholder dummy graph
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(4f, 0));
        entries.add(new BarEntry(8f, 1));
        entries.add(new BarEntry(6f, 2));
        entries.add(new BarEntry(12f, 3));
        entries.add(new BarEntry(18f, 4));
        entries.add(new BarEntry(9f, 5));
        IBarDataSet barDataSet = new BarDataSet(entries, "# of Calls");
        BarData data = new BarData(barDataSet);
        chart.setData(data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
