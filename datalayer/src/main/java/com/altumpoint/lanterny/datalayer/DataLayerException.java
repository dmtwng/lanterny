package com.altumpoint.lanterny.datalayer;

/**
 * Exception for data layer.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 */
public class DataLayerException extends Exception {

    public DataLayerException() {
    }

    public DataLayerException(String detailMessage) {
        super(detailMessage);
    }

    public DataLayerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DataLayerException(Throwable throwable) {
        super(throwable);
    }
}
