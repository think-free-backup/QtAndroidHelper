package com.mycompany.test1;
import com.mycompany.test1.R;
import org.qtproject.qt5.android.addons.qtserviceapp.QtServiceActivity;
import android.os.Bundle;
import android.util.Log;
public class MyTestServiceActivity extends QtServiceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.w(getClass().getName(), "Starting datasync service activity");
        super.setActivityClass(MyTestService.class);
        super.onCreate(savedInstanceState);
    }
}

