package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by melanieh on 2/16/17.
 */

public class WidgetRemoteViewsService extends RemoteViewsService {

    List<String> mCollection = new ArrayList<>();
    private Cursor data = null;

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        Timber.d("onGetViewFactory called");
        return new RemoteViewsFactory() {
            @Override
            public void onCreate() {
            //
            }

            @Override
            public void onDataSetChanged() {
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null, null, Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
            }

            @Override
            public int getCount() {
                return data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Timber.d("getViewAt called");

                data.moveToPosition(position);
                RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(),
                        R.layout.widget_list_item);
                // Create an Intent to launch MainActivity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                views.setOnClickPendingIntent(R.id.symbol, pendingIntent);

                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix(getApplicationContext().getString(R.string.dollar_pos_prefix));
                DecimalFormat euroFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
                DecimalFormat euroFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
                euroFormatWithPlus.setPositivePrefix(getApplicationContext().getString(R.string.euro_pos_prefix));
                DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                Timber.d("symbol= " + symbol);
                views.setTextViewText(R.id.symbol, data.getString(Contract.Quote.POSITION_SYMBOL));

                if (Locale.getDefault() == Locale.US || Locale.getDefault() == Locale.CANADA) {
                    views.setTextViewText(R.id.price,
                            dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));
                } else {
                    views.setTextViewText(R.id.price,
                            euroFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));
                }

                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                if (PrefUtils.getDisplayMode(getApplicationContext())
                        .equals(getApplicationContext().getString(R.string.pref_display_mode_absolute_key))) {
                    views.setTextViewText(R.id.change, change);
                } else {
                    views.setTextViewText(R.id.change, percentage);
                }
            return views;
        }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

        };
    }

}



