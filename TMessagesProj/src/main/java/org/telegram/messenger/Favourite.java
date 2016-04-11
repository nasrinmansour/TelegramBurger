package org.telegram.messenger;

import android.content.Context;

import org.telegram.SQLite.DBHelper;
import org.telegram.ads.analytics.GoogleAnalyticsHelper;
import org.telegram.infra.Constant;

public class Favourite {
    long chat_id;
    long id;
    DBHelper d;
    public Favourite() {
    }

    public Favourite(long chatId) {
        this.chat_id = chatId;
    }

    public Favourite(long id, long chatId) {
        this.id = id;
        this.chat_id = chatId;
    }

    public static void addFavourite(Context c,Long chatId) {
        Favourite localFavourite = new Favourite(chatId.longValue());
        ApplicationLoader.databaseHandler.addFavourite(localFavourite);
        GoogleAnalyticsHelper.sendEventAction(c, Constant.Analytic.ADD_FAV_EVENT_NAME);
    }

    public static void deleteFavourite(Context c,Long chatId) {
        ApplicationLoader.databaseHandler.deleteFavourite(chatId);
        GoogleAnalyticsHelper.sendEventAction(c, Constant.Analytic.REMOVE_FAV_EVENT_NAME);
    }

    public static boolean isFavourite(Long chatId) {
        try {
            Favourite localFavourite = ApplicationLoader.databaseHandler.getFavouriteByChatId(chatId.longValue());
            return localFavourite != null;
        } catch (Exception localException) {
            FileLog.e("tmessages", localException);
        }
        return false;
    }

    public long getChatID() {
        return this.chat_id;
    }

    public long getID() {
        return this.id;
    }

    public void setChatID(long chatId) {
        this.chat_id = chatId;
    }

    public void setID(long id) {
        this.id = id;
    }
}
