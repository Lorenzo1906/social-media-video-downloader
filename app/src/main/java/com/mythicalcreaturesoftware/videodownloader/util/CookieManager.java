package com.mythicalcreaturesoftware.videodownloader.util;

import java.util.HashMap;
import java.util.Map;

public class CookieManager {

    private static volatile CookieManager instance;
    private static final Object mutex = new Object();
    private static Map<String, String> cookies;

    private CookieManager() {
    }

    public static CookieManager getInstance() {
        CookieManager result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new CookieManager();
            }
        }
        return result;
    }

    public Map<String, String> getCookies () {
        if (cookies == null) {
            cookies = new HashMap<>();
            cookies.put("tt_webid", String.valueOf(999999998));
            cookies.put("tt_webid_v2", String.valueOf(999999998));
            cookies.put("tt_csrf_token", "sHfEtGPNSevPvHZ8");
            cookies.put("s_v_web_id", "verify_khr3jabg_V7ucdslq_Vrw9_4KPb_AJ1b_Ks706M8zIJTq");
        }

        return cookies;
    }


}