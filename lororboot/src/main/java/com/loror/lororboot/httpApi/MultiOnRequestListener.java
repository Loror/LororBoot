package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.HttpClient;

import java.util.LinkedList;
import java.util.List;

public class MultiOnRequestListener implements OnRequestListener {

    private List<OnRequestListener> onRequestListeners = new LinkedList<>();

    public MultiOnRequestListener addOnRequestListener(OnRequestListener onRequestListener) {
        if (onRequestListener != null) {
            onRequestListeners.add(onRequestListener);
        }
        return this;
    }

    public MultiOnRequestListener removeOnRequestListener(OnRequestListener onRequestListener) {
        if (onRequestListener != null) {
            onRequestListeners.remove(onRequestListener);
        }
        return this;
    }

    public MultiOnRequestListener clearOnRequestListener() {
        onRequestListeners.clear();
        return this;
    }

    @Override
    public void onRequestBegin(HttpClient client, ApiRequest request) {
        for (OnRequestListener listener : onRequestListeners) {
            listener.onRequestBegin(client, request);
        }
    }

    @Override
    public void onRequestEnd(HttpClient client, ApiResult result) {
        for (OnRequestListener listener : onRequestListeners) {
            listener.onRequestEnd(client, result);
        }
    }
}
