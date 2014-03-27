package org.qtproject.qt5.android.addons.qtserviceapp;

import com.mycompany.test1.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.os.Bundle;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.CompoundButton;
import android.widget.Switch;

public class QtServiceActivity extends Activity {

    private Intent i;
    private Class m_class = QtService.class;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.serviceconfigactivity);

        i = new Intent(this, m_class);

        final Switch sRun = (Switch) findViewById(R.id.switch_run);
        sRun.setChecked(isMyServiceRunning());
        sRun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked)
                    startService(i);
                else
                    stopService(i);
            }
        });

        ComponentName myActivity = new ComponentName(this, this.getClass());
        ActivityInfo activityInfo = null;
        try {
            activityInfo = getPackageManager().getActivityInfo(myActivity, PackageManager.GET_META_DATA);
        }
        catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences settings = getBaseContext().getSharedPreferences(activityInfo.metaData.getString("android.app.lib_name"), 0);
        final Editor editor = settings.edit();

        boolean startup = settings.getBoolean("runStartup", true);

        final Switch sRunStartup = (Switch) findViewById(R.id.switch_runstartup);
        sRunStartup.setChecked(startup);
        sRunStartup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                editor.putBoolean("runStartup", isChecked);
                editor.commit();
            }
        });
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (m_class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /* Activity class */

    public void setActivityClass(Class cl){

        m_class = cl;
    }

}
