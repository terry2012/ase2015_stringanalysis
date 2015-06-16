LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional 

LOCAL_STATIC_JAVA_LIBRARIES := libsimplexml

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := PhoneLabHeartbeat

LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libsimplexml:simple-xml-2.6.6.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
