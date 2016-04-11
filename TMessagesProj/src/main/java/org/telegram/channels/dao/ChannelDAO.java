package org.telegram.channels.dao;

import org.telegram.SQLite.DBHelper;
import org.telegram.infra.Constant;
import org.telegram.channels.model.Channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ChannelDAO {

	private static final String CHANNEL_TABLE_NAME = "channel";

	private static final String ID_COLUMN = "id";
	private static final String TITLE_COLUMN = "title";
	private static final String IMAGE_COLUMN = "image";
	private static final String IMAGE_URL_COLUMN = "image_url";
	private static final String CATEGORY_ID_COLUMN = "category_id";
	private static final String CREATE_DATE_COLUMN = "create_date";
	private static final String TYPE_COLUMN = "type";
	private static final String DESCRIPTION_COLUMN = "description";
	private static final String LINK_COLUMN = "link";
	private static final String SHOW_ORDER_COLUMN = "showOrder";
	private static final String IS_IMAGE_DIRTY_COLUMN = "is_image_dirty";

	private static final String[] ALL_COLUMN = new String[]{ID_COLUMN,
			TITLE_COLUMN,
			IMAGE_COLUMN,
			IMAGE_URL_COLUMN,
			CATEGORY_ID_COLUMN,
			CREATE_DATE_COLUMN,
			TYPE_COLUMN,
			DESCRIPTION_COLUMN,
			LINK_COLUMN,
			SHOW_ORDER_COLUMN,
			IS_IMAGE_DIRTY_COLUMN
	};

	public static final String CHANNEL_TABLE_CREATE = "create table  IF NOT EXISTS "
			+ CHANNEL_TABLE_NAME
			+ " (  "
			+ ID_COLUMN
			+ "  integer primary key,  "
			+ TITLE_COLUMN
			+ " text,"
			+ IMAGE_URL_COLUMN
			+ " text,"
			+ IMAGE_COLUMN
			+ " blob,"
			+ DESCRIPTION_COLUMN
			+ " text,"
			+ CATEGORY_ID_COLUMN
			+ " integer,"
			+ CREATE_DATE_COLUMN
			+ "  real,"
			+ SHOW_ORDER_COLUMN
			+ "  integer,"
			+ IS_IMAGE_DIRTY_COLUMN
			+ "  integer,"
			+ LINK_COLUMN
			+ "  text,"
			+ TYPE_COLUMN + "  text"
			+ ");";

	public static long insertChannel(Context context, Channel i) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return insertChannel(context, db, i);
	}

	public static long insertChannel(Context context, SQLiteDatabase db,
			Channel i) {
		i.setCreateDate(new Date(System.currentTimeMillis()));

		ContentValues values = channelToContentValues(i, true);
		return db.insert(CHANNEL_TABLE_NAME, null, values);
	}

	private static ContentValues channelToContentValues(Channel i,
			boolean updateImage) {
		ContentValues values = new ContentValues();
		values.put(ID_COLUMN, i.getId());
		values.put(TITLE_COLUMN, i.getTitle());
		if (updateImage) {
			values.put(IMAGE_COLUMN,(i.getImageBytes() == null) ? new byte[] {} : i.getImageBytes());
		}
		values.put(IMAGE_URL_COLUMN, i.getImageUrl());
		values.put(LINK_COLUMN, i.getLink());
		values.put(DESCRIPTION_COLUMN, i.getDescription());
		values.put(CATEGORY_ID_COLUMN, i.getCategoryId());
		values.put(CREATE_DATE_COLUMN, i.getCreateDate().getTime());
		values.put(SHOW_ORDER_COLUMN, i.getShowOrder());
		values.put(IS_IMAGE_DIRTY_COLUMN,i.isImageDirty() ? Constant.TRUE_VALUE : Constant.FALSE_VALUE);
		values.put(TYPE_COLUMN, i.getType().toString());
		return values;
	}

	public static Channel getChannel(Context context, long id, boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		Cursor c = db.query(CHANNEL_TABLE_NAME, ALL_COLUMN,
				ID_COLUMN + " =?", new String[]{String.valueOf(id)}, null,
				null, null);
		Channel g = null;
		while (c.moveToNext()) {
			g = getChannelFromCursor(c,loadImage);
		}
		c.close();
		return g;
	}

	public static Channel getChannel(SQLiteDatabase db, long id,
			boolean loadImage) {
		Cursor c = db.query(CHANNEL_TABLE_NAME, ALL_COLUMN,
				ID_COLUMN + " =?", new String[] { String.valueOf(id) }, null,
				null, null);
		Channel g = null;
		while (c.moveToNext()) {
			g = getChannelFromCursor(c, loadImage);
		}
		c.close();
		return g;

	}

	public static List<Channel> getAllChannels(Context context, boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return getAllChannels(db, loadImage);
	}

	public static List<Channel> getAllChannels(SQLiteDatabase db, boolean loadImage) {
		Cursor c = db.query(CHANNEL_TABLE_NAME, ALL_COLUMN, null, null, null,
				null, SHOW_ORDER_COLUMN + " desc ");
		List<Channel> gs = new ArrayList<Channel>();
		while (c.moveToNext()) {
			Channel g = getChannelFromCursor(c, loadImage);
			gs.add(g);
		}
		c.close();
		return gs;

	}

	public static List<Channel> getCategoryChannels(Context context,
			long catId, boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return getCategoryChannels(db, catId, loadImage);
	}

	public static List<Channel> getCategoryChannels(SQLiteDatabase db,
			long catId, boolean loadImage) {
		Cursor c = db.query(CHANNEL_TABLE_NAME, ALL_COLUMN,
				CATEGORY_ID_COLUMN + "=? ",
				new String[]{String.valueOf(catId)}, null, null, SHOW_ORDER_COLUMN
						+ " desc ");
		List<Channel> gs = new ArrayList<Channel>();
		while (c.moveToNext()) {
			Channel g = getChannelFromCursor(c,loadImage);
			gs.add(g);
		}
		c.close();
		return gs;

	}

	private static Channel getChannelFromCursor(Cursor c, boolean loadImage) {
		Channel i = new Channel();
		i.setId(c.getLong(c.getColumnIndex(ID_COLUMN)));
		i.setTitle(c.getString(c.getColumnIndex(TITLE_COLUMN)));
		if (loadImage) {
			i.setImageBytes(c.getBlob(c
					.getColumnIndex(IMAGE_COLUMN)));
		}
		i.setImageUrl(c.getString(c.getColumnIndex(IMAGE_URL_COLUMN)));
		i.setDescription(c.getString(c.getColumnIndex(DESCRIPTION_COLUMN)));
		i.setCategoryId(c.getLong(c.getColumnIndex(CATEGORY_ID_COLUMN)));
		i.setCreateDate(new Date(c.getLong(c.getColumnIndex(CREATE_DATE_COLUMN))));
		i.setLink(c.getString(c.getColumnIndex(LINK_COLUMN)));
		i.setShowOrder(c.getInt(c.getColumnIndex(SHOW_ORDER_COLUMN)));
		i.setType(Constant.CHANNEL_TYPE.getValueOf(c.getString(c.getColumnIndex(TYPE_COLUMN))));
		i.setImageDirty(c.getLong(c.getColumnIndex(IS_IMAGE_DIRTY_COLUMN)) == Constant.TRUE_VALUE);

		return i;
	}

	public static void updateChannel(Context context, Channel i,
			boolean updateImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		updateChannel(db, i, updateImage);
	}

	public static void updateChannel(SQLiteDatabase db, Channel i,
			boolean updateImage) {
		ContentValues values = channelToContentValues(i, updateImage);

		db.update(CHANNEL_TABLE_NAME, values, ID_COLUMN + " =?",
				new String[] { Long.toString(i.getId()) });
	}


	public static long insertOrUpdate(Context context, Channel item,
			boolean updateImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		ContentValues values = channelToContentValues(item, updateImage);
		return db.insertWithOnConflict(CHANNEL_TABLE_NAME, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	public static void delete(Context context, long id) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		
		Channel item = getChannel(context, id, false);
//		if(item!= null){
//			Utility.deleteFile(Utility.getFilePath(), item.getFileName());
//		}
		db.delete(CHANNEL_TABLE_NAME, ID_COLUMN + " = ?",
				new String[] { String.valueOf(id) });
	}


}
