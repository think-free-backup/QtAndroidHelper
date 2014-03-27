package org.qtproject.qt5.android.addons.qtactivityapp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import dalvik.system.DexClassLoader;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.os.Process;
import android.widget.Toast;

public class QtService extends Service
{
    public static final String EXTRA_MESSAGE="EXTRA_MESSAGE";

    private boolean isRunning=false;
    private ComponentName myService;
    private Class m_class = QtServiceActivity.class;

    private static ServiceInfo m_serviceInfo = null;
    private static NotificationManager m_notificationManager;
    private static Notification.Builder m_builder;
    private static QtService m_instance;
    private static String m_lib_name;
    private static PendingIntent pi;

    /* Event handling */

    public void onCreate (){

        Log.w(getClass().getName(), "Service created ...");

        m_instance = this;

        try{
            ComponentName myService = new ComponentName(this, this.getClass());
            m_serviceInfo = getPackageManager().getServiceInfo(myService, PackageManager.GET_META_DATA);
            m_lib_name = splitCamelCase (m_serviceInfo.metaData.getString("android.app.lib_name"));
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public IBinder onBind(Intent intent){

        return(null);
    }

    public int onStartCommand(Intent intent, int flags, int startId){

        /* Notifiication */

        Notification note=new Notification(m_serviceInfo.metaData.getInt("android.app.notificon"),"",System.currentTimeMillis());

        Intent i=new Intent(m_instance, m_class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        pi=PendingIntent.getActivity(this, 0,
                                i, 0);

        note.setLatestEventInfo(this, m_lib_name ,
            "Running",
            pi);
        note.flags|=Notification.FLAG_NO_CLEAR;

        startForeground(1337, note);

        /* Return */

        return(START_STICKY);
    }

    public void onDestroy(){

        Log.w(getClass().getName(), "Service destroyed ...");

        Process.killProcess(Process.myPid());
    }

    /* Activity class */

    public void setActivityClass(Class cl){

        m_class = cl;
    }

    /* Notification methods */

    public static void notify(String s){

        if (m_notificationManager == null) {

            m_notificationManager = (NotificationManager)m_instance.getSystemService(Context.NOTIFICATION_SERVICE);
            m_builder = new Notification.Builder(m_instance);
            m_builder.setSmallIcon(m_serviceInfo.metaData.getInt("android.app.notificon"));
            m_builder.setContentTitle(m_lib_name);
            m_builder.setContentIntent(pi);
        }

        m_builder.setContentText(s);
        Notification note = m_builder.build();
        note.flags |= Notification.FLAG_NO_CLEAR;

        m_notificationManager.notify(1337, note);
    }

    /* Helper classes */

    static String splitCamelCase(String sp) {

        String s = Character.toUpperCase(sp.charAt(0)) + sp.substring(1);

        return s.replaceAll(
          String.format("%s|%s|%s",
             "(?<=[A-Z])(?=[A-Z][a-z])",
             "(?<=[^A-Z])(?=[A-Z])",
             "(?<=[A-Za-z])(?=[^A-Za-z])"
          ),
          " "
       );
    }
}
