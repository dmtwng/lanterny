package com.altumpoint.lanterny.datalayer;


import java.util.Map;


/**
 * Listener for requests about updating data in Google API Data Layer.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 * @see DataLayerService
 */
public interface PathDataRequestListener {

    /**
     * Got request for update data with variables.
     *
     * @param variables map with variables of data update.
     */
    void dataUpdateRequest(Map<String, String> variables);

}
