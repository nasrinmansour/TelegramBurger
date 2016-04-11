package org.telegram.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.telegram.infra.SharedPrefrenHelper;
import org.telegram.channels.dao.BannerDAO;
import org.telegram.channels.dao.CategoryDAO;
import org.telegram.channels.dao.ChannelDAO;
import org.telegram.groups.dao.GroupCategoryDAO;
import org.telegram.groups.dao.GroupDAO;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Favourite;
import org.telegram.messenger.Lock;

public class DBHelper
        extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favourites";
    private static final int DATABASE_VERSION = 2;

    private static final String FAV_KEY_CHAT_ID = "chat_id";
    private static final String FAV_KEY_ID = "id";

    private static final String LOCK_KEY_CHAT_ID = "chat_id";
    private static final String LOCK_KEY_ID = "id";

    private static final String SETTING_KEY_KEY = "key";
    private static final String SETTINGS_KEY_VAL = "val";

    private static final String TABLE_FAVS = "tbl_favs";
    private static final String TABLE_SETTINGS = "tbl_settings";
    private static final String TABLE_LOCKS = "tbl_locked";

    public DBHelper(Context paramContext) {
        super(paramContext, DATABASE_NAME, null, DATABASE_VERSION);
    }
//---------------------
    public void addLock(Lock lock) {
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(LOCK_KEY_CHAT_ID, Long.valueOf(lock.getChatID()));
        localSQLiteDatabase.insert(TABLE_LOCKS, null, localContentValues);
        localSQLiteDatabase.close();
    }

    public void deleteLock(Lock lock) {
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        String[] arrayOfString = new String[1];
        arrayOfString[0] = String.valueOf(lock.getChatID());
        localSQLiteDatabase.delete(TABLE_LOCKS, LOCK_KEY_CHAT_ID + " = ?", arrayOfString);
        localSQLiteDatabase.close();
    }

    public Lock getLockByChatId(long chatId) {
        SQLiteDatabase localSQLiteDatabase = getReadableDatabase();
        Cursor localCursor = null;
        try {
            String[] arrayOfString1 = {LOCK_KEY_ID, LOCK_KEY_CHAT_ID};
            String[] arrayOfString2 = new String[1];
            arrayOfString2[0] = String.valueOf(chatId);
            localCursor = localSQLiteDatabase.query(TABLE_LOCKS, arrayOfString1, LOCK_KEY_CHAT_ID +"=?", arrayOfString2, null, null, null);
            if ((localCursor != null) && (localCursor.moveToFirst())) {
                Lock lock = new Lock(localCursor.getLong(1));
                return lock;
            }
            return null;
        } catch (Exception localException) {
            if (localCursor != null) {
                localCursor.close();
            }
            FileLog.e("tmessages", localException);
            return null;
        } finally {
            if (localCursor != null) {
                localCursor.close();
            }
        }
    }
    //---------------------
    public void addFavourite(Favourite paramFavourite) {
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(FAV_KEY_CHAT_ID, Long.valueOf(paramFavourite.getChatID()));
        localSQLiteDatabase.insert(TABLE_FAVS, null, localContentValues);
        localSQLiteDatabase.close();
    }

    public void deleteFavourite(Long paramLong) {
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        String[] arrayOfString = new String[1];
        arrayOfString[0] = String.valueOf(paramLong);
        localSQLiteDatabase.delete(TABLE_FAVS, FAV_KEY_CHAT_ID + " = ?", arrayOfString);
        localSQLiteDatabase.close();
    }

    public Favourite getFavouriteByChatId(long paramLong) {
        SQLiteDatabase localSQLiteDatabase = getReadableDatabase();
        Cursor localCursor = null;
        try {
            String[] arrayOfString1 = {FAV_KEY_ID, FAV_KEY_CHAT_ID};
            String[] arrayOfString2 = new String[1];
            arrayOfString2[0] = String.valueOf(paramLong);
            localCursor = localSQLiteDatabase.query(TABLE_FAVS, arrayOfString1, FAV_KEY_CHAT_ID +"=?", arrayOfString2, null, null, null);
            if ((localCursor != null) && (localCursor.moveToFirst())) {
                Favourite localFavourite = new Favourite(localCursor.getLong(1));
                return localFavourite;
            }
            return null;
        } catch (Exception localException) {
            if (localCursor != null) {
                localCursor.close();
            }
            FileLog.e("tmessages", localException);
            return null;
        } finally {
            if (localCursor != null) {
                localCursor.close();
            }
        }
    }

//---------------------
    public void saveSetting(int key,String val){
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(SETTING_KEY_KEY, key);
        localContentValues.put(SETTINGS_KEY_VAL, val);
        localSQLiteDatabase.insertWithOnConflict(TABLE_SETTINGS, null, localContentValues, SQLiteDatabase.CONFLICT_REPLACE);
        localSQLiteDatabase.close();
    }

    public String getSetting(long key) {
        SQLiteDatabase localSQLiteDatabase = getReadableDatabase();
        Cursor localCursor = null;
        try {
            String[] arrayOfString1 = {SETTING_KEY_KEY, SETTINGS_KEY_VAL};
            String[] arrayOfString2 = new String[1];
            arrayOfString2[0] = String.valueOf(key);
            localCursor = localSQLiteDatabase.query(TABLE_SETTINGS, arrayOfString1, SETTING_KEY_KEY +"=?", arrayOfString2, null, null, null);
            if ((localCursor != null) && (localCursor.moveToFirst())) {
                return localCursor.getString(1);
            }
            return null;
        } catch (Exception localException) {
            if (localCursor != null) {
                localCursor.close();
            }
            FileLog.e("tmessages", localException);
            return null;
        } finally {
            if (localCursor != null) {
                localCursor.close();
            }
        }
    }

    public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
        paramSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_FAVS+"("+ FAV_KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+ FAV_KEY_CHAT_ID +" INTEGER)");
        paramSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_LOCKS+"("+ LOCK_KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+ LOCK_KEY_CHAT_ID +" INTEGER)");
        paramSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS  "+TABLE_SETTINGS+"("+ SETTING_KEY_KEY +" INTEGER PRIMARY KEY,"+ SETTINGS_KEY_VAL +" STRING)");
        paramSQLiteDatabase.execSQL(ChannelDAO.CHANNEL_TABLE_CREATE);
        paramSQLiteDatabase.execSQL(CategoryDAO.CATEGORY_TABLE_CREATE);
        paramSQLiteDatabase.execSQL(BannerDAO.BANNER_TABLE_CREATE);
        paramSQLiteDatabase.execSQL(GroupDAO.GROUP_TABLE_CREATE);
        paramSQLiteDatabase.execSQL(GroupCategoryDAO.CATEGORY_TABLE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
//        paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_FAVS);
//        paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS channel");
//        paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS category");
        paramSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS  "+TABLE_SETTINGS+"("+ SETTING_KEY_KEY +" INTEGER PRIMARY KEY,"+ SETTINGS_KEY_VAL +" STRING)");
        paramSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_LOCKS+"("+ LOCK_KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+ LOCK_KEY_CHAT_ID +" INTEGER)");
        paramSQLiteDatabase.execSQL(ChannelDAO.CHANNEL_TABLE_CREATE);
        paramSQLiteDatabase.execSQL(CategoryDAO.CATEGORY_TABLE_CREATE);
        paramSQLiteDatabase.execSQL(BannerDAO.BANNER_TABLE_CREATE);
        paramSQLiteDatabase.execSQL(GroupDAO.GROUP_TABLE_CREATE);
        paramSQLiteDatabase.execSQL(GroupCategoryDAO.CATEGORY_TABLE_CREATE);
        onCreate(paramSQLiteDatabase);
    }


}
