package com.github.shadowsocks;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceDataStore;

import com.github.shadowsocks.aidl.IShadowsocksService;
import com.github.shadowsocks.aidl.ShadowsocksConnection;
import com.github.shadowsocks.aidl.TrafficStats;
import com.github.shadowsocks.bean.NodeBean;
import com.github.shadowsocks.bean.NodeBeanInstanceCreator;
import com.github.shadowsocks.bg.BaseService;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.database.ProfileManager;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener;
import com.github.shadowsocks.util.AESUtil;
import com.github.shadowsocks.util.SPUtil;
import com.github.shadowsocks.util.StringUtil;
import com.github.shadowsocks.util.UserUtil;
import com.github.shadowsocks.utils.DirectBoot;
import com.github.shadowsocks.utils.Key;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import kotlin.Pair;

public class MainActivityWeb extends AppCompatActivity implements ShadowsocksConnection.Callback, OnPreferenceDataStoreChangeListener {

    private NodeBean mNodeBean = null;
    WebView mWebView;
    private int REQUEST_CONNECT = 1;

    private BaseService.State state = BaseService.State.Idle;
    private Handler handler = new Handler();
    private ShadowsocksConnection connection = new ShadowsocksConnection(handler, true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_web);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connection.setBandwidthTimeout(500);
    }

    @Override
    protected void onStop() {
        super.onStop();
        connection.setBandwidthTimeout(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataStore.INSTANCE.getPublicStore().unregisterChangeListener(this);
        connection.disconnect(this);
        new BackupManager(this).dataChanged();
        handler.removeCallbacksAndMessages(null);
    }

    private void init() {
        mWebView = findViewById(R.id.web_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JSBridge(), "JS2A");
        mWebView.setWebViewClient(new Client());

//        Button btn1 = findViewById(R.id.btn1);
//        btn1.setOnClickListener(v -> new JSBridge().callLink(NodeBean.example()));

        connection.connect(this, this);
        DataStore.INSTANCE.getPublicStore().registerChangeListener(this);
        //Log.e("test", "DataStore.profileId:" + DataStore.INSTANCE.getProfileId());
        loadStartPage();
    }

    private void loadStartPage() {
        String startPage = UserUtil.signedUrl(UserUtil.URL_LOGIN);
//        Log.e("test", "startPage url:" + startPage);
        mWebView.loadUrl(startPage);
    }

    /**
     * ---------------CORE 回调-------------
     **/
    @Override
    public void stateChanged(@NotNull BaseService.State state, @Nullable String profileName, @Nullable String msg) {
//        Log.e("test", "stateChanged state:" + state.name() + " profileName:" + profileName + " msg:" + msg);
        this.state = state;

        // 上报数据
        if (state == BaseService.State.Connected) {
//            Log.e("test", "上报连接");
            UserUtil.connect(this, mNodeBean.getSERVERID(), success -> {
//                Log.e("test", "上报连接结果 result:" + success);
            });
            SPUtil.put(this, "serviceId", String.valueOf(mNodeBean.getSERVERID()));
            loadStartPage();
        } else if (state == BaseService.State.Stopped) {
            String serviceId = SPUtil.get(this, "serviceId");
//            Log.e("test", "上报断开" + serviceId);
            if (StringUtil.isBlank(serviceId)) {
                serviceId = "1";
            }
            UserUtil.disconnect(this, Integer.parseInt(serviceId), success -> {
//                Log.e("test", "上报断开结果 result:" + success);
            });
//            loadStartPage();
        }
    }


    @Override
    public void trafficUpdated(long profileId, @NotNull TrafficStats stats) {
        //Log.e("test", "trafficUpdated profileId:" + profileId);
    }

    @Override
    public void trafficPersisted(long profileId) {
        //Log.e("test", "trafficPersisted profileId:" + profileId);
    }

    @Override
    public void onServiceConnected(@NotNull IShadowsocksService service) {
        //Log.e("test", "onServiceConnected");
        try {
            state = BaseService.State.values()[service.getState()];
            //Log.e("test", "state:" + state.name());
            if (state != BaseService.State.Connected && state != BaseService.State.Connecting) {
                try {
                    ProfileManager.INSTANCE.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            state = BaseService.State.Idle;
        }
    }

    @Override
    public void onServiceDisconnected() {
        //Log.e("test", "onServiceConnected");
        state = BaseService.State.Idle;
    }

    @Override
    public void onBinderDied() {
        //Log.e("test", "onServiceConnected");
        connection.disconnect(this);
        connection.connect(this, this);
    }

    @Override
    public void onPreferenceDataStoreChanged(@NotNull PreferenceDataStore store, @NotNull String key) {
        //Log.e("test", "onPreferenceDataStoreChanged key:" + key);
        if (key.equals(Key.serviceMode)) {
            handler.post(() -> {
                connection.disconnect(MainActivityWeb.this);
                connection.connect(MainActivityWeb.this, MainActivityWeb.this);
            });
        }
    }
    /**---------------CORE 回调END-------------**/

    /**
     * ---------------页面js回调-------------
     **/
    public class JSBridge {
        @JavascriptInterface
        public void callLink(String encryptStr) {
//            Log.i("test", "callLink encryptStr:" + encryptStr);

            if (StringUtil.isBlank(encryptStr)) {
                //Log.e("test", "callLink encryptStr is blank");
                return;
            }

            String json = AESUtil.decrypt(encryptStr, AESUtil.AES_KEY);
//            Log.e("test", "decrypt json:" + json);
            if (StringUtil.isBlank(json)) {
//                Log.e("test", "节点解密失败");
                return;
            }

            // 1.录入vpn信息
            Profile profile = new Profile();

            if (Core.INSTANCE.getCurrentProfile() != null && Core.INSTANCE.getCurrentProfile().getFirst() != null) {
                Core.INSTANCE.getCurrentProfile().getFirst().copyFeatureSettingsTo(profile);
            }
            profile.serialize();

            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(
                        NodeBean.class,
                        new NodeBeanInstanceCreator(getApplicationContext())
                );
                Gson customGson = gsonBuilder.create();

                mNodeBean = customGson.fromJson(json, NodeBean.class);
                if (mNodeBean.isInvalid()) {
                    Toast.makeText(MainActivityWeb.this, "返回节点信息错误2", Toast.LENGTH_LONG).show();
                    return;
                }
                mNodeBean.format();
                profile.setId(mNodeBean.getSERVERID());
                profile.setHost(mNodeBean.getIP());
                profile.setRemotePort(Integer.parseInt(mNodeBean.getPORT()));
                profile.setPassword(mNodeBean.getPWD());
                profile.setMethod(mNodeBean.getPROTOCOL());
//                profile.setRoute("bypass-lan-china");
                profile.setRoute("gfwlist");
            } catch (Exception e) {
                Toast.makeText(MainActivityWeb.this, "返回节点信息错误1", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return;
            }

            connect(profile);
        }

        private void createProfile(Profile profile) {
            try {
                //Log.e("test", "callLink profild id:" + profile.getId());
                if (Core.INSTANCE.getActiveProfileIds().size() > 0
                        && Core.INSTANCE.getActiveProfileIds().contains(profile.getId())
                        && DataStore.INSTANCE.getDirectBootAware()) {
                    DirectBoot.INSTANCE.update(null);
                }

                profile = ProfileManager.INSTANCE.createProfile(profile);
                //Log.e("test", "callLink profileId:" + profile.getId());
                DataStore.INSTANCE.setProfileId(profile.getId());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivityWeb.this, "创建节点失败", Toast.LENGTH_LONG).show();
                return;
            }
        }

        public void connect(Profile profile) {
            //Log.e("test", "connect profile id:" + DataStore.INSTANCE.getProfileId());
            // 2 连接
            if (state.getCanStop()) {
//                showDisconnectDialog();
                Core.INSTANCE.stopService();
                try {
                    ProfileManager.INSTANCE.clear();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivityWeb.this, "重新连接中", Toast.LENGTH_SHORT).show();
                handler.postDelayed(() -> {
                    connect(profile);
                }, 1500);
            } else if (DataStore.INSTANCE.getServiceMode().equals(Key.modeVpn)) {
                createProfile(profile);
                Intent intent = VpnService.prepare(MainActivityWeb.this);
                if (intent != null) {
                    startActivityForResult(intent, REQUEST_CONNECT);
                    //Log.e("test", "startActivityForResult");
                } else {
                    onActivityResult(REQUEST_CONNECT, Activity.RESULT_OK, null);
                    //Log.e("test", "onActivityResult");
                }
            } else {
                Toast.makeText(MainActivityWeb.this, "连接中", Toast.LENGTH_SHORT).show();
                createProfile(profile);
                Core.INSTANCE.startService();
                //Log.e("test", "startService");
            }
        }


        @JavascriptInterface
        public void outLink(String serverId) {
//            Log.e("test", "outLink");
            if (state.getCanStop()) {
//                Log.e("test", "outLink in");
                Core.INSTANCE.stopService();
                try {
                    ProfileManager.INSTANCE.clear();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        if (requestCode != REQUEST_CONNECT) {
            super.onActivityResult(requestCode, resultCode, data);
            //Log.e("test", "activityweb onActivityResult");
        } else if (resultCode == Activity.RESULT_OK) {
            Core.INSTANCE.startService();
            //Log.e("test", "activityweb startService");
        } else {
            //Log.e("test", "activityweb makeText");
            Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_LONG).show();
        }
    }

    public static class Client extends WebViewClient {
        @Override
        public void onPageFinished(WebView webView, String url) {

            //Log.e("test", "onPageFinished url:" + url);
        }
    }
}
