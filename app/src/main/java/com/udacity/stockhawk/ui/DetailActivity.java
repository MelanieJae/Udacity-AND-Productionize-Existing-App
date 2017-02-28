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
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import timber.log.Timber;

import static com.udacity.stockhawk.R.id.chart;

/**
 * Created by melanieh on 2/18/17.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] PRICE_TREND_COLUMNS = {Contract.Quote.COLUMN_HISTORY};
    private static final int CHART_DATA_LOADER_ID = 2;

    LineChart lineChart;
    String historyDataString;
    static String stockSymbol;
    Uri stockContentUri;
    ArrayList<String> xAxis = new ArrayList<>();
    ArrayList<Entry> yAxis = new ArrayList<>();
    final HashMap<Integer, String> dateLabelMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Timber.d("onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        lineChart = (LineChart) findViewById(chart);
        stockContentUri = getIntent().getData();
        stockSymbol = Contract.Quote.getStockFromUri(stockContentUri);
        Timber.d("stock symbol=" + stockSymbol);

        // the Utils class for MPAndroid chart must be initialized before attempting
        // to render the chart
        Utils.init(this);

        TextView chartHeading = (TextView) findViewById(R.id.chart_heading);
        chartHeading.setText(String.format(getString(R.string.chart_description), stockSymbol));
        lineChart.setContentDescription(String.format(getString(R.string.chart_cd), stockSymbol));

        // initialize loader
        getLoaderManager().initLoader(CHART_DATA_LOADER_ID, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(CHART_DATA_LOADER_ID, null, this);
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
        renderChart(historyDataString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getLoaderManager().restartLoader(CHART_DATA_LOADER_ID, null, this);
    }

    private void renderChart(String rawDataString) {
        // parse raw data string, read date (x) and adjusted close price (y) values from string
        String[] dataPoints = rawDataString.split("\\n");
        for (int i = dataPoints.length - 1; i > 0; i--) {
            String[] dataPointSubArray = dataPoints[i].split(",");
            long dateInMillis = Long.parseLong(dataPointSubArray[0]);

            float dateFloat = dateInMillis / 1.0e12f;
            float adjClosePrice = Float.parseFloat(dataPointSubArray[1]);
            dateLabelMap.put((dataPoints.length - i), dataPointSubArray[0]);

            // add new data point to array list of custom data objects(Entries)
            Entry entry = new Entry(dateFloat, adjClosePrice);
            Timber.d("entry= " + entry);
            yAxis.add(entry);
        }

        // create new data set object for chart and add to the array of datasets
        // (in this case just one for the stock we clicked on)
        LineDataSet set = new LineDataSet(yAxis, stockSymbol);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        // create a dataset and give it a type

        dataSets.add(set); // add the datasets

        // format xaxis labels

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                String dateMillisString = dateLabelMap.get((int) value);
                long dateInMillis = Long.parseLong(dateMillisString);
                Date date = new Date(dateInMillis);
                String dateString = DateFormat.getDateInstance().format(date);

                return dateString;
            }
        });

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
        lineChart.getDescription().setText("");
        lineChart.getDescription().setPosition(800f, 80f);
        lineChart.getDescription().setTextSize(28f);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.invalidate();
    }
}
