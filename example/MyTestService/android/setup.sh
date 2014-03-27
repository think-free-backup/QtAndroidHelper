#!/bin/bash

# This script will setup the android folder for an activity or a service based qt application

# Functions

function setup(){

    clear

    echo ""
    echo "Please enter your package name (ex : org.qt-project.myqtapplication) : "
    read package

    app=`ls ../*.pro | cut -d '/' -f 2 | cut -d '.' -f 1`

    if [ "$app" == "" ];
    then
        echo "Can't find .pro in ../ exiting"
        exit 1
    fi

    echo ""
    echo "Do you want to create an activity or a service based application ? [a/s]"
    read tp

    if [ "$tp" == "a" ];
    then

        activity $package $app

    elif [ "$tp" == "s" ];
    then

        service $package $app

    else

        clear
        echo "Bad selection"
        sleep 2
        setup
    fi
}

function activity(){

    echo "You've selected an activity based application"

    cp Activity-AndroidManifest.xml AndroidManifest.xml

    echo "Cleaning un-necesary files"
    #################################

    rm -rf src/org/qtproject/qt5/android/addons/qtserviceapp
    rm src/org/qtproject/qt5/android/QtServiceDelegate.java

    echo "Configuring manifest"
    ###########################

    sed -i "s|org.qtproject.qt5.android.addons|$1|" AndroidManifest.xml
    sed -i "s|qtactivityapp.QtServiceActivity|$2Activity|g" AndroidManifest.xml
    sed -i "s|qtactivityapp.QtService|$2Service|g" AndroidManifest.xml

    echo "Creating package folders"
    ###############################

    fld=src/$(sed 's|\.|/|g' <<< $1)
    mkdir -p $fld


    echo "Configuring activity"
    ###########################


    echo ""
    echo "Do you want to use a sticky notification with service ? [y/n]"
    read sticky


    echo "package $1;" >> $fld/$2Activity.java

    echo "import org.qtproject.qt5.android.addons.qtactivityapp.QtServiceActivity;" >> $fld/$2Activity.java
    echo "import org.qtproject.qt5.android.addons.qtactivityapp.QtService;" >> $fld/$2Activity.java
    echo "import android.os.Bundle;" >> $fld/$2Activity.java
    echo "import android.content.Intent;" >> $fld/$2Activity.java
    echo "import android.content.ComponentName;" >> $fld/$2Activity.java
    echo "import android.util.Log;" >> $fld/$2Activity.java

    echo "public class $2Activity extends QtServiceActivity" >> $fld/$2Activity.java
    echo "{" >> $fld/$2Activity.java
    echo "    public void onCreate(Bundle savedInstanceState)" >> $fld/$2Activity.java
    echo "    {" >> $fld/$2Activity.java
    echo "        Log.w(getClass().getName(), "Starting config editor");" >> $fld/$2Activity.java

    if [ "$sticky" == "y" ];
    then
        echo "        super.setServiceClass($2Service.java);" >> $fld/$2Activity.java
    else
        echo "        super.setServiceClass(null);" >> $fld/$2Activity.java
    fi

    echo "        super.onCreate(savedInstanceState);" >> $fld/$2Activity.java
    echo "    }" >> $fld/$2Activity.java
    echo "}" >> $fld/$2Activity.java


    echo "Configuring service"
    ##########################

    echo "package $1;" >> $fld/$2Service.java

    echo "import org.qtproject.qt5.android.addons.qtactivityapp.QtService;" >> $fld/$2Service.java
    echo "import android.util.Log;" >> $fld/$2Service.java

    echo "public class $2Service extends QtService" >> $fld/$2Service.java
    echo "{" >> $fld/$2Service.java
    echo "    public void onCreate()" >> $fld/$2Service.java
    echo "    {" >> $fld/$2Service.java
    echo "        Log.w(getClass().getName(), "Starting config editor service");" >> $fld/$2Service.java
    echo "        super.setActivityClass($2Activity.class);" >> $fld/$2Service.java
    echo "        super.onCreate();" >> $fld/$2Service.java
    echo "    }" >> $fld/$2Service.java
    echo "}" >> $fld/$2Service.java

}

function service(){

    echo "You've selected a service based application"
    ##################################################

    cp Service-AndroidManifest.xml AndroidManifest.xml

    echo "Compiling helper librairy"
    ################################

    cd jni
    ndk-build
    cd ..

    echo "Cleaning un-necesary files"
    #################################

    rm -rf src/org/qtproject/qt5/android/addons/qtactivityapp

    echo "Configuring manifest"
    ###########################

    sed -i "s|org.qtproject.qt5.android.addons|$1|" AndroidManifest.xml
    sed -i "s|qtserviceapp.QtService|$2|g" AndroidManifest.xml
    sed -i "s|QtServiceActivity|$2|g" AndroidManifest.xml

    sed -i "s|import R;|import $1.R;|"  src/org/qtproject/qt5/android/addons/qtserviceapp/QtServiceActivity.java # FIXME !!!!


    echo "Creating package folders"
    ###############################

    fld=src/$(sed 's|\.|/|g' <<< $1)
    mkdir -p $fld


    echo "Configuring Service"
    ##########################

    echo "package $1;" >> $fld/$2.java

    echo "import org.qtproject.qt5.android.addons.qtserviceapp.QtService;" >> $fld/$2.java
    echo "import android.util.Log;" >> $fld/$2.java

    echo "public class $2 extends QtService {" >> $fld/$2.java

    echo "    public void onCreate()" >> $fld/$2.java
    echo "    {" >> $fld/$2.java
    echo "        Log.w(getClass().getName(), \"Starting datasync service\");" >> $fld/$2.java
    echo "        super.setActivityClass($2Activity.class);" >> $fld/$2.java
    echo "        super.onCreate();" >> $fld/$2.java
    echo "    }" >> $fld/$2.java
    echo "}" >> $fld/$2.java
    echo "" >> $fld/$2.java


    echo "Configuring service activity"
    ###################################

    echo "package $1;" >> $fld/$2Activity.java

    echo "import $1.R;" >> $fld/$2Activity.java

    echo "import org.qtproject.qt5.android.addons.qtserviceapp.QtServiceActivity;" >> $fld/$2Activity.java

    echo "import android.os.Bundle;" >> $fld/$2Activity.java
    echo "import android.util.Log;" >> $fld/$2Activity.java

    echo "public class $2Activity extends QtServiceActivity {" >> $fld/$2Activity.java

    echo "    @Override" >> $fld/$2Activity.java
    echo "    public void onCreate(Bundle savedInstanceState)" >> $fld/$2Activity.java
    echo "    {" >> $fld/$2Activity.java
    echo "        Log.w(getClass().getName(), \"Starting datasync service activity\");" >> $fld/$2Activity.java
    echo "        super.setActivityClass($2.class);" >> $fld/$2Activity.java
    echo "        super.onCreate(savedInstanceState);" >> $fld/$2Activity.java
    echo "    }" >> $fld/$2Activity.java
    echo "}" >> $fld/$2Activity.java
    echo "" >> $fld/$2Activity.java


    echo "Configuring broadcast receiver (to be run at startup)"
    ############################################################

    echo "package $1;" >> $fld/$2BroadcastReceiver.java

    echo "import org.qtproject.qt5.android.addons.qtserviceapp.QtServiceBroadcastReceiver;" >> $fld/$2BroadcastReceiver.java

    echo "import android.content.Context;" >> $fld/$2BroadcastReceiver.java
    echo "import android.content.Intent;" >> $fld/$2BroadcastReceiver.java
    echo "import android.util.Log;" >> $fld/$2BroadcastReceiver.java

    echo "public class $2BroadcastReceiver extends QtServiceBroadcastReceiver {" >> $fld/$2BroadcastReceiver.java

    echo "    @Override" >> $fld/$2BroadcastReceiver.java
    echo "    public void onReceive(Context context, Intent intent) {" >> $fld/$2BroadcastReceiver.java

    echo "        Log.w(getClass().getName(), \"Broadcast received for Comservice\");" >> $fld/$2BroadcastReceiver.java

    echo "        super.setActivityClass($2.class);" >> $fld/$2BroadcastReceiver.java
    echo "        super.onReceive(context, intent);" >> $fld/$2BroadcastReceiver.java
    echo "    }" >> $fld/$2BroadcastReceiver.java
    echo "}" >> $fld/$2BroadcastReceiver.java
    echo "" >> $fld/$2BroadcastReceiver.java    
}

# Main

clear
echo ""
echo "This script will allow you to setup a qt activity based application with a service that run a sticky notification or a service based application (without grafical interface)"
echo "First create a qt project in qtcreator "
echo "Please ensure that ndk-build (android-ndk) is in your path before continuing"
echo ""
echo "Press any key to continue"
read

setup

clear
echo "Almost done, please create an icon.png and a notificon.png for every resolution you want to support in the res folder"
echo "You can use : http://android-ui-utils.googlecode.com/hg/asset-studio/dist/index.html to create it"
