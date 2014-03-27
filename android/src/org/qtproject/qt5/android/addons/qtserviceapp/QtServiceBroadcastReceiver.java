package org.qtproject.qt5.android.addons.qtserviceapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class QtServiceBroadcastReceiver extends BroadcastReceiver {

    private Class m_class = QtService.class;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.w("Qt", "Starting service");

        try
        {
            ActivityInfo activityInfo = context.getPackageManager().getReceiverInfo(new ComponentName(context, this.getClass()), PackageManager.GET_META_DATA);
            Bundle meta = activityInfo.metaData;

            SharedPreferences settings = context.getSharedPreferences(meta.getString("android.app.lib_name"), 0);
            boolean startup = settings.getBoolean("runStartup", true);

            if (startup){

                Intent i = new Intent(context, m_class);
                context.startService(i);
            }
        }
        catch (Exception e)
        {
            Log.e("Qt", "Can't create service" + e);
        }

    }

    /* Activity class */

    public void setActivityClass(Class cl){

        m_class = cl;
    }
}
