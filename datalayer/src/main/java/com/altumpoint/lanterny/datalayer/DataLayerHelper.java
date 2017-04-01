package com.altumpoint.lanterny.datalayer;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.*;

import java.util.*;

import static com.altumpoint.lanterny.datalayer.Constants.*;


/**
 * Helper for interaction with Google API Data Layer.
 * All notifications about changing data in Data Layer goes throw {@link PathChangeListener}.
 * You need just register appropriate listener for path.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 * @see PathChangeListener
 * @see PutDataRequestBuilder
 */
public class DataLayerHelper extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int CNF_CM_CURRENT = 1;
    private static final int CNF_CM_CONNECTED = 2;
    private static final String LOG_TAG = "<[ DATA LAYER HELPER ]>";


    // ToDo add configuration possibility
    private int cnfCommunicateNode = CNF_CM_CONNECTED;

    private GoogleApiClient apiClient;
    private boolean connected;

    private List<Node> connectedNodes;
    private Node currentNode;
    private Node communicateNode;

    private DataLayerListener dataLayerListener = null;
    private MessageApi.MessageListener messageListener = null;
    private PathListenerHandler pathListenerHandler = new PathListenerHandler();


    public DataLayerHelper(Context context) {
        apiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        connected = false;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        connected = true;

        // Update active nodes list.
        Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                connectedNodes = nodes.getNodes();
                if (cnfCommunicateNode == CNF_CM_CONNECTED && !connectedNodes.isEmpty()) {
                    setCommunicateNode(connectedNodes.get(0));
                }
                if (dataLayerListener != null) {
                    dataLayerListener.onNodesListUpdated(connectedNodes);
                }
            }
        });
        Wearable.NodeApi.getLocalNode(apiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetLocalNodeResult getLocalNodeResult) {
                currentNode = getLocalNodeResult.getNode();
                if (cnfCommunicateNode == CNF_CM_CURRENT) {
                    setCommunicateNode(currentNode);
                }
                if (dataLayerListener != null) {
                    dataLayerListener.onCurrentNodeUpdated(currentNode);
                }
            }
        });

        if (dataLayerListener != null) {
            dataLayerListener.onConnected();
        }

        Wearable.DataApi.addListener(this.apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        connected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        this.connected = false;
        if (this.dataLayerListener != null) {
            this.dataLayerListener.onConnectionFailed(connectionResult);
        }
    }


    /**
     * Connect to Google API.
     */
    public void connect() {
        this.apiClient.connect();
    }

    /**
     * Disconnect from Google API.
     */
    public void disconnect() {
        this.apiClient.disconnect();
    }


    /**
     * Returns communicate node for current helper.
     *
     * @return node, to communicate with.
     */
    public Node getCommunicateNode() {
        return communicateNode;
    }

    /**
     * Sets communicate node for current helper.
     *
     * @param communicateNode node, to communicate with.
     */
    public void setCommunicateNode(Node communicateNode) {
        Log.i(LOG_TAG, "Setting communicated node: " + communicateNode.getId() + " : " + communicateNode.getDisplayName());
        this.communicateNode = communicateNode;
    }

    /**
     * Returns current node for current helper.
     *
     * @return current.
     */
    public Node getCurrentNode() {
        return currentNode;
    }

    /**
     * Returns connected nodes for current helper.
     *
     * @return connected nodes.
     */
    public List<Node> getConnectedNodes() {
        return connectedNodes;
    }

    /**
     * Returns {@code DataLayerListener} of current helper.
     *
     * @return data layer listener.
     */
    public DataLayerListener getDataLayerListener() {
        return dataLayerListener;
    }

    /**
     * Sets {@code DataLayerListener} for current helper.
     *
     * @param dataLayerListener new listener.
     */
    public void setDataLayerListener(DataLayerListener dataLayerListener) {
        this.dataLayerListener = dataLayerListener;
    }

    /**
     * Returns {@code DataLayerMessageListener} of current helper.
     *
     * @return data layer message listener.
     */
    public MessageApi.MessageListener getMessageListener() {
        return messageListener;
    }

    /**
     * Sets messages listener for current helper.
     *
     * @param messageListener new listener.
     */
    public void setMessageListener(MessageApi.MessageListener messageListener) {
        if (this.messageListener != null) {
            Wearable.MessageApi.removeListener(apiClient, this.messageListener);
        }
        this.messageListener = messageListener;
        if (this.messageListener != null) {
            Wearable.MessageApi.addListener(apiClient, this.messageListener);
        }
    }

    public void addPathListener(String path, PathChangeListener listener) {
        this.pathListenerHandler.addPathListener(path, listener);
    }

    public void removePathListener(PathChangeListener listener) {
        this.pathListenerHandler.removePathListener(listener);
    }

    private void notifyDataChanged(String path, DataMap dataMap) {
        Log.i(LOG_TAG, "Notify Data Changed!!! " + path);
        this.pathListenerHandler.invokeDataChanged(path, dataMap);
    }



    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        final ArrayList<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        Log.i(LOG_TAG, "Data Changed!!! Got events: " + events.size());
        for (DataEvent event : events) {
            if (event.getType() != DataEvent.TYPE_CHANGED) {
                continue;
            }

            PutDataMapRequest request = PutDataMapRequest.createFromDataMapItem(DataMapItem.fromDataItem(event.getDataItem()));
            notifyDataChanged(request.getUri().getPath(), request.getDataMap());
        }
    }


    /**
     * Puts data to data layer in separate thread.
     *
     * @param builder builder for data to put in data layer.
     */
    public void putData(PutDataRequestBuilder builder) {
        new Thread(new DataSendRunnable(builder)).run();
    }


    /**
     * Clears data in data layer by specified path.
     *
     * @param path path of data to be cleared.
     */
    public void clearData(String path) {
        if (this.currentNode == null) {
            Log.i(LOG_TAG, "Current node is null, can't delete path: " + path);
            return;
        }
        Wearable.DataApi.deleteDataItems(this.apiClient, buildUri(this.currentNode, path));
    }


    /**
     * Sends request to retrieve data by specified {@code path}.
     * If data not present in data layer, sends message to Node with request to put data in path.
     * When data will be available, {@code pathListener} will be called.
     *
     * @param path path to data.
     */
    public void retrieveData(String path) throws DataLayerException {
        Log.i(LOG_TAG, "Retrieving data: " + path);

        if (this.communicateNode == null) {
            Log.i(LOG_TAG, "Communicated node undefined, request wasn't send.");
            throw new DataLayerException("Communicated node not defined");
        }

        Wearable.DataApi.getDataItem(apiClient, buildUri(this.communicateNode, path))
                .setResultCallback(new GetDataCallback(path));
    }


    /**
     * Sends message to communicate node with request to update data in data layer.
     *
     * @param path path of data to update.
     */
    private void sendRequestForUpdate(final String path) {
        Log.i(LOG_TAG, "Sending request for updating path: " + path);
        Wearable.MessageApi.sendMessage(apiClient, communicateNode.getId(), MESSAGE_UPDATE_DATA, path.getBytes())
                .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            // Failed to send message
                            Log.i(LOG_TAG, "Request for updating path failed. Path: " + path);
                        } else {
                            Log.i(LOG_TAG, "Request for updating path was send with ID: " + sendMessageResult.getRequestId());
                        }
                    }
                });
    }


    private static Uri buildUri(Node node, String path) {
        return new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(path)
                .authority(node.getId())
                .build();
    }


    /**
     * Runnable for sending data to data layer.
     */
    private class DataSendRunnable implements Runnable {

        private PutDataRequestBuilder requestBuilder;

        private DataSendRunnable(PutDataRequestBuilder requestBuilder) {
            this.requestBuilder = requestBuilder;
        }

        @Override
        public void run() {
            if (!connected) {
                apiClient.blockingConnect();
            }
            if (apiClient.isConnected()) {
                Wearable.DataApi.putDataItem(apiClient, requestBuilder.build());
            }
        }
    }


    /**
     * Callback for getting data from data layer.
     */
    private class GetDataCallback implements ResultCallback<DataApi.DataItemResult> {
        private String path;

        GetDataCallback(String path) {
            this.path = path;
        }

        @Override
        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
            DataItem dataItem = dataItemResult.getDataItem();
            if (dataItemResult.getStatus().isSuccess() && dataItem != null) {
                Log.i(LOG_TAG, "Retrieved data in path: " + path);
                notifyDataChanged(path, DataMap.fromByteArray(dataItem.getData()));
            } else  {
                Log.i(LOG_TAG, "No data found in path: " + path);
                sendRequestForUpdate(path);
            }
        }
    }


    public interface DataLayerListener {
        void onConnected();
        void onConnectionFailed(@NonNull ConnectionResult connectionResult);
        void onNodesListUpdated(List<Node> nodes);
        void onCurrentNodeUpdated(Node node);
    }

}
