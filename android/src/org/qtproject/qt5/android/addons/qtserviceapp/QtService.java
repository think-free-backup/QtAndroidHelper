package org.qtproject.qt5.android.addons.qtserviceapp;

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

@SuppressLint("NewApi")
public class QtService extends Service {

    /* Private variables */

    public Boolean started=false;
    private String[] m_qtLibs = null;
    private DexClassLoader m_classLoader = null;

    private Class m_class = QtServiceActivity.class;

    private static ServiceInfo m_serviceInfo = null;
    protected static NotificationManager m_notificationManager;
    protected static Notification.Builder m_builder;
    protected static PendingIntent pi;
    private static QtService m_instance;
    private static String m_lib_name;

    private static final String ERROR_CODE_KEY = "error.code";
    private static final String DEX_PATH_KEY = "dex.path";
    private static final String STATIC_INIT_CLASSES_KEY = "static.init.classes";
    private static final String NATIVE_LIBRARIES_KEY = "native.libraries";
    private static final String MAIN_LIBRARY_KEY = "main.library";
    private static final String BUNDLED_LIBRARIES_KEY = "bundled.libraries";
    private static final String LOADER_CLASS_NAME_KEY = "loader.class.name";
    private static final String LIB_PATH_KEY = "lib.path";
    private static final String ENVIRONMENT_VARIABLES_KEY = "env.variable";

    /* Service methods */

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

    public IBinder onBind(Intent intent) {

        return null;
    }

    @SuppressWarnings("deprecation")
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        /* Notification */

        createNotification();

        /* Start the app */

        startApp();

        /* Return */

        return(START_STICKY);
    }

    public void onDestroy(){

        Log.w(getClass().getName(), "Service destroyed ...");

        Process.killProcess(Process.myPid());
    }

    /* Internals methods */

    private void startApp(){

        if (!started){

            started = true;
            Toast.makeText(getBaseContext(), m_lib_name + " starting", Toast.LENGTH_LONG).show();

            try{

                if (m_serviceInfo.metaData.containsKey("android.app.qt_libs_resource_id")) {
                    int resourceId = m_serviceInfo.metaData.getInt("android.app.qt_libs_resource_id");
                    m_qtLibs = getResources().getStringArray(resourceId);
                }

                if (m_serviceInfo.metaData.containsKey("android.app.use_local_qt_libs")
                        && m_serviceInfo.metaData.getInt("android.app.use_local_qt_libs") == 1) {
                    ArrayList<String> libraryList = new ArrayList<String>();


                    String localPrefix = "/data/local/tmp/qt/";
                    if (m_serviceInfo.metaData.containsKey("android.app.libs_prefix"))
                        localPrefix = m_serviceInfo.metaData.getString("android.app.libs_prefix");

                    boolean bundlingQtLibs = false;
                    if (m_serviceInfo.metaData.containsKey("android.app.bundle_local_qt_libs")
                        && m_serviceInfo.metaData.getInt("android.app.bundle_local_qt_libs") == 1) {
                        localPrefix = getApplicationInfo().dataDir + "/";
                        bundlingQtLibs = true;
                    }

                    if (m_qtLibs != null) {
                        for (int i=0;i<m_qtLibs.length;i++) {
                            libraryList.add(localPrefix
                                            + "lib/lib"
                                            + m_qtLibs[i]
                                            + ".so");
                        }
                    }

                    libraryList.add(localPrefix + "lib/libQtAndroidService.so");

                    String dexPaths = new String();
                    String pathSeparator = System.getProperty("path.separator", ":");
                    if (!bundlingQtLibs && m_serviceInfo.metaData.containsKey("android.app.load_local_jars")) {
                        String[] jarFiles = m_serviceInfo.metaData.getString("android.app.load_local_jars").split(":");
                        for (String jar:jarFiles) {
                            if (jar.length() > 0) {
                                if (dexPaths.length() > 0)
                                    dexPaths += pathSeparator;
                                dexPaths += localPrefix + jar;
                            }
                        }
                    }

                    Bundle loaderParams = new Bundle();
                    loaderParams.putInt(ERROR_CODE_KEY, 0);
                    loaderParams.putString(DEX_PATH_KEY, dexPaths);
                    loaderParams.putString(LOADER_CLASS_NAME_KEY, "org.qtproject.qt5.android.QtServiceDelegate");
                    loaderParams.putString(ENVIRONMENT_VARIABLES_KEY, "");
                    if (m_serviceInfo.metaData.containsKey("android.app.static_init_classes")) {
                        loaderParams.putStringArray(STATIC_INIT_CLASSES_KEY,
                                                    m_serviceInfo.metaData.getString("android.app.static_init_classes").split(":"));
                    }
                    loaderParams.putStringArrayList(NATIVE_LIBRARIES_KEY, libraryList);

                    loadApplication(loaderParams);
                    return;
                }
            }
            catch (Exception e) {
                started = false;
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    public void loadApplication(Bundle loaderParams){

        try{

            ArrayList<String> libs = new ArrayList<String>();
            if ( m_serviceInfo.metaData.containsKey("android.app.bundled_libs_resource_id") )
                libs.addAll(Arrays.asList(getResources().getStringArray(m_serviceInfo.metaData.getInt("android.app.bundled_libs_resource_id"))));

            String libName = null;
            if ( m_serviceInfo.metaData.containsKey("android.app.lib_name") ) {
                libName = m_serviceInfo.metaData.getString("android.app.lib_name");
                loaderParams.putString(MAIN_LIBRARY_KEY, libName); //main library contains main() function
            }

            loaderParams.putStringArrayList(BUNDLED_LIBRARIES_KEY, libs);
            loaderParams.putString(ENVIRONMENT_VARIABLES_KEY, "");

            m_classLoader = new DexClassLoader(loaderParams.getString(DEX_PATH_KEY), // .jar/.apk files
                    getDir("outdex", Context.MODE_PRIVATE).getAbsolutePath(), // directory where optimized DEX files should be written.
                    loaderParams.containsKey(LIB_PATH_KEY) ? loaderParams.getString(LIB_PATH_KEY) : null, // libs folder (if exists)
                    getClassLoader()); // parent loader

            String loaderClassName = loaderParams.getString(LOADER_CLASS_NAME_KEY);

            Log.w(getClass().getName(), "Loader : " + loaderClassName);

            Class<?> loaderClass = m_classLoader.loadClass(loaderClassName); // load QtLoader class
            Object qtLoader = loaderClass.newInstance(); // create an instance

            Method perpareAppMethod = qtLoader.getClass().getMethod("loadApplication",
                                                                    Service.class,
                                                                    ClassLoader.class,
                                                                    Bundle.class);

            if (!(Boolean)perpareAppMethod.invoke(qtLoader, this, m_classLoader, loaderParams))
                throw new Exception("");

            // now load the application library so it's accessible from this class loader
            if (libName != null)
                System.loadLibrary(libName);

            Method startAppMethod=qtLoader.getClass().getMethod("startApplication");
            if (!(Boolean)startAppMethod.invoke(qtLoader))
                throw new Exception("");


        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Activity class */

    public void setActivityClass(Class cl){

        m_class = cl;
    }


    /* Notification methods */

    protected void createNotification(){

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
    }

    public static void notify(String s)
    {
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

    /* Helpers */

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
