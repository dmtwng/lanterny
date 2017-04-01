package com.altumpoint.lanterny.datalayer;

/**
 * Constants for helper messages.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 */
class Constants {
    static final String MESSAGE_UPDATE_DATA = "/com.altumpoint.lanterny/helper/update_data";
    static final String MESSAGE_START_ACTIVITY = "/com.altumpoint.lanterny/helper/start_activity";

    static final String BROADCAST_SERVICE = "com.altumpoint.lanterny.datalayer.CLEAR_PATH";
    static final String VAR_PATH = "path";
    static final String VAR_ACTION = "action";
    static final int ACTION_UNDEF = 0;
    static final int ACTION_CLEAR = 1;
}
