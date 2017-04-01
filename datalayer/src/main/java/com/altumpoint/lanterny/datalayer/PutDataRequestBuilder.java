package com.altumpoint.lanterny.datalayer;


import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;


/**
 * Builder for data that should be send to Google API data layer.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 * @see DataLayerHelper
 */
public abstract class PutDataRequestBuilder {

    private String path;

    public PutDataRequestBuilder(String path) {
        this.path = path;
    }

    public PutDataRequest build() {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        fillDataMap(putDataMapRequest.getDataMap());
        return putDataMapRequest.asPutDataRequest();
    }

    protected abstract void fillDataMap(DataMap dataMap);
}
