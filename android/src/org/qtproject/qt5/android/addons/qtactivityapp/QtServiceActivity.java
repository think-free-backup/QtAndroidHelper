package org.qtproject.qt5.android.addons.qtactivityapp;

import org.qtproject.qt5.android.bindings.QtActivity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.ComponentName;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;

public class QtServiceActivity extends QtActivity
{
    private ActivityInfo m_activityInfo = null;
    private Class m_class = QtService.class;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        try {
            m_activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
        } 
        catch (NameNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }
        
        loadService();
    }

    public void setServiceClass(Class cl){

        m_class = cl;
    }

    private int loadService()
    {
        int retVal = -1;
        Log.w("Qt", "loadService");
        try
        {
            if (m_class != null){
                Intent i = new Intent(this, m_class);
                i.putExtra(QtService.EXTRA_MESSAGE, "Loading " + m_activityInfo.metaData.getString("android.app.lib_name"));
                ComponentName cn = startService(i);
                retVal = 0;
            }
        }
        catch (Exception e)
        {
            Log.e("Qt", "Can't create service " + e);
        }

        return retVal;
    }
}
