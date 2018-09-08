package com.ncuos.promote.dispatch;

import java.util.HashMap;
import java.util.Map;

class BaseDispatcher<T> {
    private final Map<String, T> routes;

    BaseDispatcher() {
        this.routes = new HashMap<>();
    }

    void addRoute(String path, T target) {
        if (routes.containsKey(path)) {
            return;
        }
        routes.put(path, target);
    }

    DispatchResult<T> dispatch(String uri) {
        for (Map.Entry<String, T> entry : routes.entrySet()) {
            String path = entry.getKey();
            if (path.equals(uri)) {
                T target = entry.getValue();
                return new DispatchResult<>(uri, target);
            }
        }
        return null;
    }
}
