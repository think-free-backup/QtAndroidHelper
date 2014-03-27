package com.mycompany.test1;
import org.qtproject.qt5.android.addons.qtserviceapp.QtService;
import android.util.Log;
public class MyTestService extends QtService {
    public void onCreate()
    {
        Log.w(getClass().getName(), "Starting datasync service");
        super.setActivityClass(MyTestServiceActivity.class);
        super.onCreate();
    }
}

