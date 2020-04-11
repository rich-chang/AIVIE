package aivie.developer.aivie.util;

import aivie.developer.aivie.BuildConfig;

public interface Constant {

    // Debuggin Log
    boolean DEBUG = BuildConfig.DEBUG;
    String TAG = "richc";

    // Internet Url
    String FIREBASE_STORAGE_INST = "gs://clinical-trials-772d5.appspot.com";
    String AUTO_AVATRA_URL = "https://ui-avatars.com/api/?size=80&rounded=true&background=0D8ABC&color=fff&name=";

    // Notification
    String NOTIFICATION_ID = "notification-id";
    String NOTIFICATION = "notification";
}
