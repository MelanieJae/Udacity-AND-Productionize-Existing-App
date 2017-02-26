package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by melanieh on 2/16/17.
 */

public class WidgetRemoteViewsService extends RemoteViewsService {

    List<String> mCollection = new ArrayList<>();
    Context mContext = null;
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
                RemoteViews view = new RemoteViews(mContext.getPackageName(),
                        R.layout.widget_list_item);
                view.setTextViewText(R.id.stock_symbol,data.getString(Contract.Quote.POSITION_SYMBOL));
                return view;
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



