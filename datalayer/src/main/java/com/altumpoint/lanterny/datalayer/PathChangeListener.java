package com.altumpoint.lanterny.datalayer;


import com.google.android.gms.wearable.DataMap;

import java.util.Map;


/**
 * Listener for data change on Google API Data Layer.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 * @see DataLayerHelper
 */
public interface PathChangeListener {

    /**
     * Triggers when data on path are changed, or request for data retrieve was sent.
     *
     * @param data changed data.
     * @param variables variables of data.
     */
    void onDataChanged(final DataMap data, final Map<String, String> variables);

}
