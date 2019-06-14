package com.reactlibrary;

import java.util.concurrent.TimeUnit;

import kin.sdk.WhitelistableTransaction;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

class WhitelistService {

    private static final String URL_WHITELISTING_SERVICE = "http://34.239.111.38:3000/whitelist";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;

    public WhitelistService() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void whitelistTransaction(WhitelistableTransaction whitelistableTransaction, Callback cb) throws JSONException {
        RequestBody requestBody = RequestBody.create(JSON, toJson(whitelistableTransaction));
        Request request = new Request.Builder()
                .url(URL_WHITELISTING_SERVICE)
                .post(requestBody)
                .build();
        okHttpClient.newCall(request)
                .enqueue(cb);
    }

    private String toJson(WhitelistableTransaction whitelistableTransaction) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("envelope", whitelistableTransaction.getTransactionPayload());
        jo.put("network_id", whitelistableTransaction.getNetworkPassphrase());
        return jo.toString();
    }
}



