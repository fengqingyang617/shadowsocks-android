package com.github.shadowsocks.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class NetUtil {

    public static void post(Context context, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        //Log.e("test", "post url:" + url);
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, listener, errorListener);
        queue.add(stringRequest);
    }

    public static void get(Context context, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        //Log.e("test", "get url:" + url);
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, listener, errorListener);
        queue.add(stringRequest);
    }
}
