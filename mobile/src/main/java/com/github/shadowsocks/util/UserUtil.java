package com.github.shadowsocks.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.snail.antifake.deviceid.macaddress.MacAddressUtils;

import java.util.UUID;

public class UserUtil {
    public static String KEY_SP_UID = "uid";
    public static String URL_LOGIN = "http://www.tiantianling.vip/hpc/account.aspx?";
    public static String URL_REPORT_CONNECT_INFO = "http://www.tiantianling.vip/hpc/api/ss.ashx?";
    public static String URL_CONNECTED_PAGE = "http://www.tiantianling.vip/hpc/";
    public static String URL_PARAM_KEY_LINKSUCC = "linksucc";
    public static String URL_PARAM_KEY_LINKOUT = "linkout";


    private static String hmacKey;
    private static String userId;

    private static void initKeys() {
        hmacKey = "hj69Nk87cOj1ou";
    }

    public static void initUser(Context context) {
        initKeys();
        userId = SPUtil.get(context, KEY_SP_UID);
        if (StringUtil.isBlank(userId)) {
            String mac = MacAddressUtils.getMacAddress(context);
            if (StringUtil.isBlank(mac)) {
                mac = UUID.randomUUID().toString();
                //Log.i("test", "mac get fail, generate random UUID:" + mac);
            }
            //Log.i("test", "mac is:" + mac);
            userId = HMACUtil.getStringMD5(mac);
            SPUtil.put(context, KEY_SP_UID, userId);
            //Log.i("test", "userId is:" + userId);
        }
        login(context);
    }

    public static void login(Context context) {
        Response.Listener<String> listener = response -> {
            //Log.i("test", "login response:" + response);
            Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
        };
        Response.ErrorListener errorListener = error -> {
            //Log.e("test", "login response error:" + error.getMessage());
            error.printStackTrace();
            Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show();
        };
        String url = signedUrl(URL_LOGIN);
        NetUtil.post(context, url, listener, errorListener);
    }



    public static void connect(Context context, int serverId, NetCallback callback) {
        report(context, URL_REPORT_CONNECT_INFO + URL_PARAM_KEY_LINKSUCC, serverId, callback);
    }

    public static void disconnect(Context context, int serverId, NetCallback callback) {
        report(context, URL_REPORT_CONNECT_INFO + URL_PARAM_KEY_LINKOUT, serverId, callback);
    }

    public static void report(Context context, String url, int serverId, NetCallback callback) {
        Response.Listener<String> listener = response -> {
            //Log.i("test", "report response:" + response);
            callback.onResponse(true);
        };

        Response.ErrorListener errorListener = error -> {
            //Log.e("test", "report response error:" + error.getMessage());
            error.printStackTrace();
            callback.onResponse(false);
        };

        url += "=" + getRandom() + "&"  + "serverid=" + serverId   +"&";
        url = signedUrl(url);
        NetUtil.post(context, url, listener, errorListener);
    }

    /**
     *
     * @return 六位随机码 126524
     */
    private static int getRandom() {
        return (int) ((Math.random() * 9 + 1) * 100000);
    }

    public static String signedUrl(String url) {
        //Log.e("test", "signedUrl param url:" + url);
        int num = getRandom();
        String encryptUid = AESUtil.encrypt(userId, AESUtil.AES_KEY);
        encryptUid = encryptUid.replace("+", "||");
        String signed = HMACUtil.calculateHMAC(encryptUid, hmacKey);
        signed = signed.replace("+", "||");
        url += "userin=" + num + "&userid=" + encryptUid + "&signed=" + signed;

        //Log.e("test", "signedUrl url:" + url);
        return url;
    }
}