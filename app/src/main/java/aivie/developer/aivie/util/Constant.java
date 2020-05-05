package aivie.developer.aivie.util;

import aivie.developer.aivie.BuildConfig;

public interface Constant {

    // Debuggin Log
    boolean DEBUG = BuildConfig.DEBUG;
    String TAG = "richc";

    // Internet Url
    String FIREBASE_STORAGE_INST = "gs://clinical-trials-772d5.appspot.com";
    String AUTO_AVATAR_URL = "https://ui-avatars.com/api/?size=40&rounded=true&background=0D8ABC&color=fff&name=";

    // Notification
    String NOTIFICATION_ID = "notification-id";
    String NOTIFICATION = "notification";
    String GROUP_VISIT_REMINDER = "Visit Reminder";

    // Shared Preference
    String SP_NAME = "AIVIE";
    String SP_KEY_INIT_REMINDER = "init-visit-reminder";

    // Adverse Events
    Integer sedation = 0;
    Integer rash = 1;
    Integer yawning = 2;
    Integer sweating = 3;
    Integer amenorrhea = 4;
    Integer postural_hypertension = 5;
    Integer dizziness = 6;
    Integer vomiting_nausea = 7;
    Integer urinary_incontinence = 8;
    Integer headache = 9;
    Integer sexual_dysfunction = 10;
    Integer tremor = 11;
    Integer insomnia = 12;
    Integer constipation = 13;
    Integer drowsiness = 14;
    Integer dry_month = 15;
    Integer loss_appetite = 16;
    Integer gain_weight = 17;

    Integer adverse_events_count = 18;
}
