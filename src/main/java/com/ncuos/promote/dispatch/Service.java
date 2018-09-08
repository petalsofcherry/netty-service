package com.ncuos.promote.dispatch;

import com.google.gson.Gson;

public interface Service {
    default Gson get() {
        return new Gson();
    }
    default Gson post() {
        return new Gson();
    }
    default Gson delete() {
        return new Gson();
    }
    default Gson ws() {
        return new Gson();
    }
}
