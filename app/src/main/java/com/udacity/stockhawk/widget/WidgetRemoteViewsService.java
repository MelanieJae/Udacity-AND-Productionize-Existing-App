package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by melanieh on 2/16/17.
 */

public class WidgetRemoteViewsService extends RemoteViewsService {

    List<String> mCollection = new ArrayList<>();
    Context mContext = null;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Timber.d("onGetViewFactory called");
        return new RemoteViewsFactory() {
            @Override
            public void onCreate() {
                initData();
                Timber.d("mCollection= " + mCollection.toString());
            }

            @Override
            public void onDataSetChanged() {
                initData();
            }

            @Override
            public void onDestroy() {
            }

            @Override
            public int getCount() {
                return mCollection.size();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                RemoteViews view = new RemoteViews(mContext.getPackageName(),
                        R.layout.widget_list_item);
                view.setTextViewText(R.id.stock_symbol,mCollection.get(position));
                return view;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            private void initData() {
                mCollection.clear();
                for (int i = 1; i <= 10; i++) {
                    mCollection.add("ListView item " + i);
                }
            }
        };
    }
}



