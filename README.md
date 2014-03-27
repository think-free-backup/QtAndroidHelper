Qt Android service and activity helper classes
==============================================

- First create a project in QtCreator QtQuick or QtWidget for activity based application or "Qt Console Application" for a service based one

- In project - Android - Run - Advanced actions, create an AndroidManifest
  or put the following lines to your .pro

  ANDROID_PACKAGE_SOURCE_DIR = $$PWD/android

  OTHER_FILES += \
      android/AndroidManifest.xml


- ! Only bundled Qt librairies have been tested ...

- Replace the "android" folder that qtcreator have created in your project by this one or if your edited the .pro just put it in your project.

- Run ./setup.sh in the android folder
- Put your icon.png and notificon.png
