package com.urbanlegend.instarecover.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static final String PREF_NAME = "com.urbanlegend.instarecover";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }


    public boolean shouldWeAskWritingPermission(String permission){
        return (pref.getBoolean(permission, true));
    }

    public void markAsAskedWritingPermission(String permission){
        pref.edit().putBoolean(permission, false).apply();
    }
}
