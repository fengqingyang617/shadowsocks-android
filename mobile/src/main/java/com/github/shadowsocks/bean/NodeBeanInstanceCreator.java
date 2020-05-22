package com.github.shadowsocks.bean;

import android.content.Context;

import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;

public class NodeBeanInstanceCreator implements InstanceCreator<NodeBean> {
    private Context context;

    public NodeBeanInstanceCreator(Context context) {
        this.context = context;
    }

    @Override
    public NodeBean createInstance(Type type) {
        // create new object with our additional property
        NodeBean userContext = new NodeBean(context);

        // return it to gson for further usage
        return userContext;
    }
}
