package org.telegram.messenger;

import android.content.Context;

import org.telegram.infra.Constant;
import org.telegram.infra.SharedPrefrenHelper;
import org.telegram.infra.Utility;
import org.telegram.ui.LaunchActivity;

import java.util.Date;

public class SepehrSettingsHelper {

    private class SettingsKeys{
        public static final int GHOST_MODE =1;
        public static final int IS_FREE_ACTIVATED =2;
        public static final int IS_PREMIUM = 3;
        public static final int CHATROOM_USAGE_DURATION = 4;
    }

    public static void saveGhostModeActivationState(boolean state) {
        ApplicationLoader.databaseHandler.saveSetting(SettingsKeys.GHOST_MODE, String.valueOf(state));
    }
    public static boolean getGhostModeActivationState() {
        String state = ApplicationLoader.databaseHandler.getSetting(SettingsKeys.GHOST_MODE);
        return state!=null?Boolean.parseBoolean(state):false;
    }


    public static void saveIsFreeVersionActivatedSetting(boolean state) {
        ApplicationLoader.databaseHandler.saveSetting(SettingsKeys.IS_FREE_ACTIVATED, String.valueOf(state));
    }
    public static boolean getIsFreeVersionActivatedSetting() {
        String state = ApplicationLoader.databaseHandler.getSetting(SettingsKeys.IS_FREE_ACTIVATED);
        return state!=null?Boolean.parseBoolean(state):false;
    }

    public static void saveIsPremiumSetting(boolean state) {
        ApplicationLoader.databaseHandler.saveSetting(SettingsKeys.IS_PREMIUM, String.valueOf(state));
    }

    public static boolean getIsPremiumSetting() {
        String state = ApplicationLoader.databaseHandler.getSetting(SettingsKeys.IS_PREMIUM);
        return state!=null?Boolean.parseBoolean(state):false;
    }


    public static void saveChatroomValidUseDuration(long durationMill) {
        ApplicationLoader.databaseHandler.saveSetting(SettingsKeys.CHATROOM_USAGE_DURATION, String.valueOf(durationMill));
    }

    public static long getChatroomValidUseDuration() {
        String val = ApplicationLoader.databaseHandler.getSetting(SettingsKeys.CHATROOM_USAGE_DURATION);
        try {
            return Long.parseLong(val);
        }catch (Exception e){
            return 0;
        }
    }

    public static Date getChatroomExpireDate(Context context) {
        long installDate = SharedPrefrenHelper.getAppInstallationDateMill(context);
        return new Date(installDate+getChatroomValidUseDuration()+ Constant.CHATROOM_FREE_USAGE_DURATION_MILL);

    }

}
