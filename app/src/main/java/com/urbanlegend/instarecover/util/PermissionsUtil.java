package com.urbanlegend.instarecover.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionsUtil {

    private boolean writeAccepted;

    public boolean haveWritePermissions(Context context, Activity activity) {
        boolean tmpResult = false;
        PrefManager pref = new PrefManager(context);

        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (hasPermission(permission, context)) {
            tmpResult = true;
        } else {
            if (pref.shouldWeAskWritingPermission(permission)) {
                requestPermissions(permission, activity);
                pref.markAsAskedWritingPermission(permission);
            }
        }

        return tmpResult;
    }

    private void requestPermissions(String permission, Activity activity) {
        String[] perms = {permission};

        int permsRequestCode = 200;

        ActivityCompat.requestPermissions(activity, perms, permsRequestCode);
    }

    private boolean hasPermission(String permission, Context context){
        if(isPreLollipop()){
            return(ContextCompat.checkSelfPermission(context, permission)== PackageManager.PERMISSION_GRANTED);
        }

        return true;
    }

    private boolean isPreLollipop(){
        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public boolean isWriteAccepted() {
        return writeAccepted;
    }

    public void setWriteAccepted(boolean writeAccepted) {
        this.writeAccepted = writeAccepted;
    }
}
