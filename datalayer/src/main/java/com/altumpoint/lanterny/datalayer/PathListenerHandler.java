package com.altumpoint.lanterny.datalayer;


import com.google.android.gms.wearable.DataMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Handler for listeners.
 *
 * @author Dmytro Patserkovskyi
 * @since 0.1.0
 */
class PathListenerHandler {

    private Map<String, PathData> listenersStorage;


    PathListenerHandler() {
        this.listenersStorage = new HashMap<>();
    }


    void addPathListener(String path, PathChangeListener listener) {
        if (!this.listenersStorage.containsKey(path)) {
            this.listenersStorage.put(path, new PathData(path));
        }
        this.listenersStorage.get(path).listeners.add(listener);
    }

    void removePathListener(PathChangeListener listener) {
        for (Map.Entry<String, PathData> entry : this.listenersStorage.entrySet()) {
            entry.getValue().listeners.remove(listener);
        }
    }

    void invokeDataChanged(String path, DataMap dataMap) {
        for (Map.Entry<String, PathData> entry : this.listenersStorage.entrySet()) {
            if (entry.getValue().pattern.matches(path)) {
                entry.getValue().invoke(path, dataMap);
            }
        }
    }


    private class PathData {
        PathPattern pattern;
        Set<PathChangeListener> listeners;

        PathData(String pathPattern) {
            this.pattern = new PathPattern(pathPattern);
            this.listeners = new HashSet<>();
        }

        void invoke(String path, DataMap dataMap) {
            Map<String, String> variables = pattern.parsePath(path);
            for (PathChangeListener listener : this.listeners) {
                listener.onDataChanged(dataMap, variables);
            }
        }
    }
}
