LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := QtAndroidService
LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES := QtAndroidService.cpp
LOCAL_LDLIBS    := -llog 

include $(BUILD_SHARED_LIBRARY)