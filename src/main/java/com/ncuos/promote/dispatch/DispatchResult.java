package com.ncuos.promote.dispatch;

import io.netty.util.internal.ObjectUtil;


public class DispatchResult<T> {
    private final String uri;

    private final T target;

    DispatchResult(String uri, T target) {
        this.uri = ObjectUtil.checkNotNull(uri, "uri");
        this.target = ObjectUtil.checkNotNull(target, "target");
    }

    public String uri() {
        return uri;
    }

    public T target() {
        return target;
    }
}
