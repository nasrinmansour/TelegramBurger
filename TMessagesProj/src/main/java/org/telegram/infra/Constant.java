package org.telegram.infra;

import android.app.AlarmManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Morteza on 2016/03/17.
 */
public class Constant {
    public static final int TRUE_VALUE = 1;
    public static final int FALSE_VALUE = 0;

    //----server config
   
    public static final int defColor = 0xff2f8cc9;//0xff43C3DB;//0xff2f8cc9;58BCD5//0xff55abd2
    public static final long CHATROOM_FREE_USAGE_DURATION_MILL = 0;//3* AlarmManager.INTERVAL_DAY;


    public static final String PLUS_PREFS_KEY = "plusconfig";
    public static final String MAIN_CONFIG_PREFS = "mainconfig";

    public static final String THEME_PREFS = "theme";
    public static final String HIDE_TABS_PREFS = "hideTabs";
    public static final String SORT_ALL_PREFS = "sortAll";
    public static final String SORT_USERS_PREFS = "sortUsers";
    public static final String SORT_GROUPS_PREFS = "sortGroups";
    public static final String SORT_CHANNELS_PREFS = "sortChannels";
    public static final String SORT_BOTS_PREFS = "sortBots";
    public static final String SORT_MEGAGROUPS_PREFS = "sortSGroups";
    public static final String SORT_FAVS_PREFS = "sortFavs";
    public static final String TAB_HEIGHT_PREFS_KEY = "tabsHeight";

    public static final int DIALOG_TYPE_DIALOG = 0;
    public static final int DIALOG_TYPE_SERVER_ONLY = 1;
    public static final int DIALOG_TYPE_GROUP_ONLY = 2;
    public static final int DIALOG_TYPE_USERS = 3;
    public static final int DIALOG_TYPE_GROUPS = 4;
    public static final int DIALOG_TYPE_CHANNELS = 5;
    public static final int DIALOG_TYPE_BOTS = 6;
    public static final int DIALOG_TYPE_MEGA_GROUPS = 7;
    public static final int DIALOG_TYPE_FAVS = 8;
    public static final int DIALOG_TYPE_GROUPS_ALL = 9;

    public static final String SELECTED_TAB = "selTab";

    public static final int USER_STATUS_COLOR_OFFLINE = 0xffb6b6b6;
    public static final int USER_STATUS_COLOR_LATELY = 0xffffa200;
    public static final int USER_STATUS_COLOR_ONLINE = 0xff00ff00;
    public static final int USER_STATUS_COLOR_ALONG_TIME_AGO = 0xffb6b6b6;
    public static final String CATEGORY_ID_KEY = "cat_id";


    public static final String[] SEPEHR_CHANNEL_ID = new String[]{"bergram","sepehr_group","burger_telegram"};
    public static final String ACTIVATION_CODE = "gr@=m7";

    public class Analytic {
        public static final String NOTIFICATION_SCREEN_VIEW_NAME = "notification";
        public static final String LAUNCH_SCREEN_VIEW_NAME = "launchActivity";
        public static final String CAFE_CHANNEL_SCREEN_VIEW_NAME = "cafe_channel";
        public static final String CATEGORY_SCREEN_VIEW_NAME = "category_";

        public static final String CHANNEL_CLICK_EVENT_NAME = "channel_";
        public static final String TAB_CHANGE_EVENT_NAME = "tab_change_";
        public static final String LOCK_EVENT_NAME = "lock_dialog_";
        public static final String REMOVE_FAV_EVENT_NAME = "remove_favourites";
        public static final String ADD_FAV_EVENT_NAME = "add_favourites";
        public static final String ONLY_ONLINE_USERS_EVENT_NAME = "only_online";
        public static final String GHOST_MODE_CLICK_EVENT_NAME = "ghost_";
        public static final String BANNER_CLICK_EVENT_NAME = "banner_click_";
        public static final String GROUP_CLICK_EVENT_NAME = "group_click";
    }

    public static void log(String str) {
        Log.d("aaaaaaaaaaaaaaaa", str);
    }

    public static final int IMAGE_QUALITY = 60;


    public class JSON_PARAM {
        public static final String VERSION = "version" ;
        public static final String GROUP_ID= "id";
        public static final String GROUP_CHANGES_LIST = "changes";
        public static final String GROUP_DEL_LIST = "dels";
        public static final String GROUP_TITLE = "title";
        public static final String GROUP_ORDER = "order";
        public static final String GROUP_TYPE = "type";
        public static final String GROUP_LINK = "link";
        public static final String GROUP_IMAGE = "thumb";
        public static final String GROUP_CAT_ID = "cat";

        public static final String CHANNEL_ID= "id";
        public static final String CHANNEL_CHANGES_LIST = "changes";
        public static final String CHANNEL_DEL_LIST = "dels";
        public static final String CHANNEL_TITLE = "title";
        public static final String CHANNEL_ORDER = "order";
        public static final String CHANNEL_TYPE = "type";
        public static final String CHANNEL_LINK = "link";
        public static final String CHANNEL_IMAGE = "thumb";
        public static final String CHANNEL_CAT_ID = "cat";

        public static final String CATEGORY_CHANGES_LIST = "changes";
        public static final String CATEGORY_ID= "id";
        public static final String CATEGORY_IMAGE_URL = "image";
        public static final String CATEGORY_NAME = "name";
        public static final String CATEGORY_DEL_LIST = "dels";


        public static final String BANNER_ID= "id";
        public static final String BANNER_CHANGES_LIST = "changes";
        public static final String BANNER_DEL_LIST = "dels";
        public static final String BANNER_TITLE = "title";
        public static final String BANNER_ORDER = "order";
        public static final String BANNER_TYPE = "type";
        public static final String BANNER_LINK = "link";
        public static final String BANNER_IMAGE = "image";

        public static final String TIME = "time";

    }

    public static final String FOLDER_NAME = "sepehr_stickers";
    public static final String SAVED_IMAGE_DIR = Environment
            .getDataDirectory()
            + File.separator
            + Constant.FOLDER_NAME;

    public static enum DOWNLOAD_STATE {
        DOWNLOADED(1), DOWNLODING(2), NOT_DOWNLOADED(3);
        private Integer type;

        DOWNLOAD_STATE(Integer type) {
            this.type = type;
        }

        public Integer toInteger() {
            return type;
        }

        @Override
        public String toString() {
            return String.valueOf(type);
        }

        public static DOWNLOAD_STATE getValueOf(Integer type) {
            switch (type) {
                case 1:
                    return DOWNLOADED;
                case 2:
                    return DOWNLODING;
                case 3:
                    return NOT_DOWNLOADED;
                default:
                    return null;
            }
        }

    }
    public static enum GROUP_TYPE {
        PUBLICGROUP("1"), PRIVATEGROUP("0");
        private String type;

        GROUP_TYPE(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static GROUP_TYPE getValueOf(String type) {
            switch (type.toCharArray()[0]) {
                case '1':
                    return PUBLICGROUP;
                case '0':
                    return PRIVATEGROUP;
                default:
                    return null;
            }
        }
    }

    public static enum CHANNEL_TYPE {
        PUBLICCHANNEL("1"), PRIVATECHANNEL("0");
        private String type;

        CHANNEL_TYPE(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static CHANNEL_TYPE getValueOf(String type) {
            switch (type.toCharArray()[0]) {
                case '1':
                    return PUBLICCHANNEL;
                case '0':
                    return PRIVATECHANNEL;
                default:
                    return null;
            }
        }
    }
    public static enum BANNER_LINK_TYPE {
        PUBLICCHANNEL("0"), PRIVATECHANNEL("1"), URL("2");
        private String type;

        BANNER_LINK_TYPE(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static BANNER_LINK_TYPE getValueOf(String type) {
            switch (type.toCharArray()[0]) {
                case '0':
                    return PUBLICCHANNEL;
                case '1':
                    return PRIVATECHANNEL;
                case '2':
                    return URL;
                default:
                    return null;
            }
        }
    }
}
