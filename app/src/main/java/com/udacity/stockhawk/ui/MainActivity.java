package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;
    @BindView(R.id.clock)
    TextView clock;
    private StockAdapter adapter;
    Calendar rightNow;
    Handler mHandler;
    int hours;
    int minutes;
    int seconds;
    long millisInFuture = 0;

    @Override
    public void onClick(String symbol) {
        Timber.d("Symbol clicked: %s", symbol);
        Uri stockUri = Contract.Quote.makeUriForStock(symbol);

        // launch intent to send user to stock detail page
        Intent launchDetail = new Intent(this, DetailActivity.class);
        launchDetail.setData(stockUri);
        startActivity(launchDetail);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("defaultLocale= " + Locale.getDefault());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // display stocks
        adapter = new StockAdapter(this, this);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
            }
        }).attachToRecyclerView(stockRecyclerView);

        // display dynamic market closing bell countdown timer
        setCountdownClockText();
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this);

        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);
        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }
        // populate stock list
        adapter.setCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCountdownClockText() {
        rightNow = Calendar.getInstance();
        switch (rightNow.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SATURDAY:
                clock.setText(getString(R.string.us_markets_closed));
                clock.setContentDescription(getString(R.string.us_markets_closed));
            case Calendar.SUNDAY:
                clock.setText(getString(R.string.us_markets_closed));
                clock.setContentDescription(getString(R.string.us_markets_closed));
            default:
                displayCountdownTimer();
        }
    }

    private void displayCountdownTimer() {
        final int currTimeHours = rightNow.get(Calendar.HOUR);
        final int currTimeMinutes = rightNow.get(Calendar.MINUTE);
        final int currTimeSeconds = rightNow.get(Calendar.SECOND);
        final long currentTimeInMillis = ((currTimeHours * 60 * 60) + (currTimeMinutes * 60)
                + currTimeSeconds) * 1000;
        int GMTOffset = rightNow.get(Calendar.ZONE_OFFSET);
        int DSTOffset = rightNow.get(Calendar.DST_OFFSET);
        // adjusted US market close based on 4PM EST(US) (9PM/21:00 GMT) converted to the user's
        // time zone obtained from the system settings via the calendar 'rightnow' instance
        long adjUSMarketClose = (21 * 3600 * 1000 + GMTOffset + DSTOffset);

        millisInFuture = adjUSMarketClose - currentTimeInMillis;
        new CountDownTimer(millisInFuture, 1000) {
            public void onTick(long millisUntilFinished) {
                // convert millisInFuture to standard hhh:mm:ss format
                int[] time = PrefUtils.convertTimeInMillisToTime(millisUntilFinished);
                hours = time[0];
                minutes = time[1];
                seconds = time[2];
                clock.setText(getString(R.string.closing_bell_countdown)
                        + String.format(getString(R.string.format_hours), hours)
                        + ":" + String.format(getString(R.string.format_minutes), minutes)
                        + ":" + String.format(getString(R.string.format_seconds), seconds));
                clock.setContentDescription(getString(R.string.closing_bell_countdown)
                        + String.format(getString(R.string.format_hours), hours)
                        + ":" + String.format(getString(R.string.format_minutes), minutes)
                        + ":" + String.format(getString(R.string.format_seconds), seconds));
            }

            public void onFinish() {
                clock.setText(getString(R.string.us_markets_closed));
                clock.setContentDescription(getString(R.string.us_markets_closed));

            }

        }.start();

    }

}