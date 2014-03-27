#-------------------------------------------------
#
# Project created by QtCreator 2014-03-26T12:42:37
#
#-------------------------------------------------

QT       += core

QT       -= gui

TARGET = MyTestService
CONFIG   += console
CONFIG   -= app_bundle

TEMPLATE = app


SOURCES += main.cpp

ANDROID_PACKAGE_SOURCE_DIR = $$PWD/android

OTHER_FILES += \
  android/AndroidManifest.xml
