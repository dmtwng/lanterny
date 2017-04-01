package com.altumpoint.lanterny.datalayer;


import android.content.Context;
import android.content.Intent;

import static com.altumpoint.lanterny.datalayer.Constants.*;


/**
 * Messenger for communicating with data layer service, which is running
 * on the same device.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 * @see DataLayerService
 */
public class DataLayerServiceMessenger {

    private Context context;


    public DataLayerServiceMessenger(Context context) {
        this.context = context;
    }


    /**
     * Send intent to service with message to clear data under specified path.
     *
     * @param path path to clear.
     */
    public void sendClearMessage(String path) {
        Intent intent = new Intent(BROADCAST_SERVICE);
        intent.putExtra(VAR_ACTION, ACTION_CLEAR);
        intent.putExtra(VAR_PATH, path);
        this.context.sendBroadcast(intent);
    }

}
