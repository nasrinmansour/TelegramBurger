package org.telegram.messenger;

import org.telegram.SQLite.DBHelper;

public class Lock {
    long chat_id;
    long id;
    DBHelper d;
    public Lock() {
    }

    public Lock(long chatId) {
        this.chat_id = chatId;
    }

    public Lock(long id, long chatId) {
        this.id = id;
        this.chat_id = chatId;
    }

    public static void addLock(Long chatId) {
        Lock localLock = new Lock(chatId.longValue());
        ApplicationLoader.databaseHandler.addLock(localLock);
    }

    public static void deleteLock(Long chatId) {
        Lock localLock = new Lock(chatId.longValue());
        ApplicationLoader.databaseHandler.deleteLock(localLock);
    }

    public static boolean isLock(Long chatId) {
        try {
            Lock localLock = ApplicationLoader.databaseHandler.getLockByChatId(chatId.longValue());
            return localLock != null;
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
