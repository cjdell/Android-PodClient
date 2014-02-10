package com.cjdell.podclient.models;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cjdell on 08/02/14.
 */
public class ModelLifecycleDispatcher {

    public interface Listener {

        public void onCreated(SugarRecord record);

        public void onSaved(SugarRecord record);

        public void onDeleted(SugarRecord record);

    }

    private static final List<Listener> mListeners = new ArrayList<Listener>();

    public static void addModelLifecycleListener(Listener listener) {
        mListeners.add(listener);
    }

    public static void removeModelLifecycleListener(Listener listener) {
        mListeners.remove(listener);
    }

    public static void dispatchOnCreated(SugarRecord record) {
        for (Listener listener : mListeners) {
            listener.onCreated(record);
        }
    }

    public static void dispatchOnSaved(SugarRecord record) {
        for (Listener listener : mListeners) {
            listener.onSaved(record);
        }
    }

    public static void dispatchOnDeleted(SugarRecord record) {
        for (Listener listener : mListeners) {
            listener.onDeleted(record);
        }
    }

}
