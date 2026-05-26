package com.dmitrij.behappy.data.network;

import java.io.IOException;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DemoCall<T> implements Call<T> {
    private final T data;

    public DemoCall(T data) {
        this.data = data;
    }

    @Override
    public Response<T> execute() throws IOException {
        return Response.success(data);
    }

    @Override
    public void enqueue(Callback<T> callback) {
        callback.onResponse(this, Response.success(data));
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return this;
    }

    @Override
    public Request request() {
        return null;
    }

    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }
}
