package com.altumpoint.lanterny.datalayer;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

import java.util.ArrayList;
import java.util.List;

import static com.altumpoint.lanterny.datalayer.Constants.*;


/**
 * Service for handling messages from Google API Data Layer.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 * @see DataLayerHelper
 */
public abstract class DataLayerService extends Service {

    private static final String LOG_TAG = "<[ DL SERVICE ]>";

    private ActionsBroadcastReceiver messageReceiver;

    protected DataLayerHelper dataLayerHelper;

    private List<PathAndListener> listeners = new ArrayList<>();


    protected abstract void initialize();
    protected abstract void requestForActivity(String activityId);
    protected abstract void handleMessage(MessageEvent messageEvent);


    @Override
    public void onCreate() {
        super.onCreate();
        dataLayerHelper = new DataLayerHelper(getApplicationContext());
        dataLayerHelper.setMessageListener(new DLMessageListener());
        Log.i(LOG_TAG, "Data layer initialized");
        initialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dataLayerHelper.connect();
        Log.i(LOG_TAG, "Data layer connected");

        this.messageReceiver = new ActionsBroadcastReceiver();
        registerReceiver(this.messageReceiver, new IntentFilter(BROADCAST_SERVICE));
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        dataLayerHelper.disconnect();
        unregisterReceiver(this.messageReceiver);
        super.onDestroy();
    }


    /**
     * Adds new listener for specified path.
     * <p/>
     * You can add the same listener for different path patterns. In this case it will be invoked
     * if any of paths patterns data request will be catched.
     * <p/>
     * You can add several listeners for the same path pattern. In this case they all will be
     * invoked if data change request will be catched.
     *
     * @param path path for listener.
     * @param listener listener.
     */
    protected void addDataRequestListener(String path, PathDataRequestListener listener) {
        listeners.add(new PathAndListener(path, listener));
    }


    private void clearPathData(String path) {
        this.dataLayerHelper.clearData(path);
    }


    private void updatePathData(String path) {
        for (PathAndListener pathAndListener : listeners) {
            if (pathAndListener.getPattern().matches(path)) {
                pathAndListener.getListener().dataUpdateRequest(pathAndListener.getPattern().parsePath(path));
            }
        }
    }


    private class DLMessageListener implements MessageApi.MessageListener {

        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            switch (messageEvent.getPath()) {
                case MESSAGE_UPDATE_DATA:
                    updatePathData(new String(messageEvent.getData()));
                    break;

                case MESSAGE_START_ACTIVITY:
                    requestForActivity(new String(messageEvent.getData()));
                    break;

                default:
                    handleMessage(messageEvent);
                    break;
            }

        }
    }


    /**
     * Class for receiving messages from application.
     * <p/>
     * Possible actions: {@code ACTION_CLEAR}.
     */
    private class ActionsBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra(VAR_ACTION, ACTION_UNDEF);
            switch (action) {
                case ACTION_CLEAR:
                    clearPathData(intent.getStringExtra(VAR_PATH));
                    break;

                default:
                    Log.i(LOG_TAG, "Got intent with undefined action: " + intent.toString());
                    break;
            }

        }
    }


    private class PathAndListener {
        private PathPattern pattern;
        private PathDataRequestListener listener;

        PathAndListener(String path, PathDataRequestListener listener) {
            this.pattern = new PathPattern(path);
            this.listener = listener;
        }

        PathPattern getPattern() {
            return pattern;
        }

        PathDataRequestListener getListener() {
            return listener;
        }
    }

}
