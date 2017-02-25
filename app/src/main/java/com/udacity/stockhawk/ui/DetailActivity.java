package com.udacity.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by melanieh on 2/18/17.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String[] PRICE_TREND_COLUMNS = {Contract.Quote.COLUMN_HISTORY};
    private static final int CHART_DATA_LOADER_ID = 2;

    LineChart lineChart;
    String historyDataString;
    static String stockSymbol;
    Uri stockContentUri;
    ArrayList<String> xAxis = new ArrayList<>();
    ArrayList<Entry> yAxis = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Timber.d("onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        lineChart = (LineChart) findViewById(R.id.chart);
        stockContentUri = getIntent().getData();
        stockSymbol = Contract.Quote.getStockFromUri(stockContentUri);
        Timber.d("stock symbol=" + stockSymbol);
        // the Utils class for MPAndroid chart must be initialized before attempting
        // to render the chart
        Utils.init(this);

        // initialize loader
        getLoaderManager().initLoader(CHART_DATA_LOADER_ID, null, this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Timber.d("onCreateLoader called");
        Timber.d("stockContentUri" + stockContentUri);
        return new CursorLoader(this, stockContentUri,
                PRICE_TREND_COLUMNS, null, null, Contract.Quote.COLUMN_HISTORY + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Timber.d("onLoadFinished called");
        while (cursor.moveToNext()) {
            historyDataString = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Quote.COLUMN_HISTORY));
        }
        parseHistoryString(historyDataString, stockSymbol);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getLoaderManager().restartLoader(CHART_DATA_LOADER_ID, null, this);
    }

    private void parseHistoryString(String rawDataString, String stockSymbol) {

        Timber.d("renderChart called");
        Timber.d("rawDataString= " + rawDataString);
        Timber.d("symbol= " + stockSymbol);

        // parse raw data string, read date (x) and adjusted close price (y) values from string
        String[] dataPoints = rawDataString.split("\\n");
        for (int i = 0; i < dataPoints.length; i++) {
            String[] dataPointSubArray = dataPoints[i].split(",");
            Timber.d("dataPointSubArray size= " + dataPointSubArray.length);

            xAxis.add(dataPointSubArray[0]);
            float dateFloat = Float.parseFloat(dataPointSubArray[0]);
            Timber.d("dateFloatString= " + dateFloat);

            float date = dateFloat / 1.0e12f;
            Timber.d("float date=" + date);

            float adjClosePrice = Float.parseFloat(dataPointSubArray[1]);
            Timber.d("float adjClosePrice=" + adjClosePrice);

            // add new data point to array list of custom data objects(Entries)
            Entry entry = new Entry(date, adjClosePrice);
            Timber.d("entry= " + entry);
            yAxis.add(entry);
        }

        renderChart(yAxis);
    }

    private void renderChart(ArrayList<Entry> yAxis) {
        LineDataSet set;

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        // create a dataset and give it a type
        set = new LineDataSet(yAxis, stockSymbol);

        dataSets.add(set); // add the datasets

        // chart settings
        set.setColor(Color.BLACK);
        set.setCircleColor(Color.BLACK);
        set.setLineWidth(1f);
        set.setCircleRadius(3f);
        set.setDrawCircles(true);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);

        // bind data to chart
        lineChart.setData(new LineData(dataSets));
        lineChart.getDescription().setPosition(700f, 40f);
        lineChart.getDescription().setTextSize(32f);
        lineChart.getDescription().setText("Price History for: " + stockSymbol);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.invalidate();
    }

}
