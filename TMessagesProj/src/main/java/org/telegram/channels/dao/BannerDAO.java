package org.telegram.channels.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.telegram.SQLite.DBHelper;
import org.telegram.infra.Constant;
import org.telegram.channels.model.Banner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BannerDAO {

	private static final String BANNER_TABLE_NAME = "banner";

	private static final String ID_COLUMN = "id";
	private static final String TITLE_COLUMN = "title";
	private static final String IMAGE_COLUMN = "image";
	private static final String IMAGE_URL_COLUMN = "image_url";
	private static final String CREATE_DATE_COLUMN = "create_date";
	private static final String TYPE_COLUMN = "type";
	private static final String LINK_COLUMN = "link";
	private static final String SHOW_ORDER_COLUMN = "showOrder";
	private static final String IS_IMAGE_DIRTY_COLUMN = "is_image_dirty";

	private static final String[] ALL_COLUMN = new String[]{ID_COLUMN,
			TITLE_COLUMN,
			IMAGE_COLUMN,
			IMAGE_URL_COLUMN,
			CREATE_DATE_COLUMN,
			TYPE_COLUMN,
			LINK_COLUMN,
			SHOW_ORDER_COLUMN,
			IS_IMAGE_DIRTY_COLUMN
	};

	public static final String BANNER_TABLE_CREATE = "create table  IF NOT EXISTS "
			+ BANNER_TABLE_NAME
			+ " (  "
			+ ID_COLUMN
			+ "  integer primary key,  "
			+ TITLE_COLUMN
			+ " text,"
			+ IMAGE_URL_COLUMN
			+ " text,"
			+ IMAGE_COLUMN
			+ " blob,"
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

	public static long insertBanner(Context context, Banner i) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return insertBanner(context, db, i);
	}

	public static long insertBanner(Context context, SQLiteDatabase db,
			Banner i) {
		i.setCreateDate(new Date(System.currentTimeMillis()));

		ContentValues values = bannerToContentValues(i, true);
		return db.insert(BANNER_TABLE_NAME, null, values);
	}

	private static ContentValues bannerToContentValues(Banner i,
			boolean updateImage) {
		ContentValues values = new ContentValues();
		values.put(ID_COLUMN, i.getId());
		values.put(TITLE_COLUMN, i.getTitle());
		if (updateImage) {
			values.put(IMAGE_COLUMN,(i.getImageBytes() == null) ? new byte[] {} : i.getImageBytes());
		}
		values.put(IMAGE_URL_COLUMN, i.getImageUrl());
		values.put(LINK_COLUMN, i.getLink());
		values.put(CREATE_DATE_COLUMN, i.getCreateDate().getTime());
		values.put(SHOW_ORDER_COLUMN, i.getShowOrder());
		values.put(IS_IMAGE_DIRTY_COLUMN,i.isImageDirty() ? Constant.TRUE_VALUE : Constant.FALSE_VALUE);
		values.put(TYPE_COLUMN, i.getType().toString());
		return values;
	}

	public static Banner getBanner(Context context, long id, boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		Cursor c = db.query(BANNER_TABLE_NAME, ALL_COLUMN,
				ID_COLUMN + " =?", new String[]{String.valueOf(id)}, null,
				null, null);
		Banner g = null;
		while (c.moveToNext()) {
			g = getBannerFromCursor(c, loadImage);
		}
		c.close();
		return g;
	}

	public static Banner getBanner(SQLiteDatabase db, long id,
			boolean loadImage) {
		Cursor c = db.query(BANNER_TABLE_NAME, ALL_COLUMN,
				ID_COLUMN + " =?", new String[] { String.valueOf(id) }, null,
				null, null);
		Banner g = null;
		while (c.moveToNext()) {
			g = getBannerFromCursor(c, loadImage);
		}
		c.close();
		return g;

	}

	public static List<Banner> getAllBanners(Context context, boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return getAllBanners(db, loadImage);
	}

	public static List<Banner> getAllBanners(SQLiteDatabase db, boolean loadImage) {
		Cursor c = db.query(BANNER_TABLE_NAME, ALL_COLUMN, null, null, null,
				null, SHOW_ORDER_COLUMN + " desc ");
		List<Banner> gs = new ArrayList<Banner>();
		while (c.moveToNext()) {
			Banner g = getBannerFromCursor(c, loadImage);
			gs.add(g);
		}
		c.close();
		return gs;

	}

	private static Banner getBannerFromCursor(Cursor c, boolean loadImage) {
		Banner i = new Banner();
		i.setId(c.getLong(c.getColumnIndex(ID_COLUMN)));
		i.setTitle(c.getString(c.getColumnIndex(TITLE_COLUMN)));
		if (loadImage) {
			i.setImageBytes(c.getBlob(c
					.getColumnIndex(IMAGE_COLUMN)));
		}
		i.setImageUrl(c.getString(c.getColumnIndex(IMAGE_URL_COLUMN)));
		i.setCreateDate(new Date(c.getLong(c.getColumnIndex(CREATE_DATE_COLUMN))));
		i.setLink(c.getString(c.getColumnIndex(LINK_COLUMN)));
		i.setShowOrder(c.getInt(c.getColumnIndex(SHOW_ORDER_COLUMN)));
		i.setType(Constant.BANNER_LINK_TYPE.getValueOf(c.getString(c.getColumnIndex(TYPE_COLUMN))));
		i.setImageDirty(c.getLong(c.getColumnIndex(IS_IMAGE_DIRTY_COLUMN)) == Constant.TRUE_VALUE);

		return i;
	}

	public static void updateBanner(Context context, Banner i,
			boolean updateImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		updateBanner(db, i, updateImage);
	}

	public static void updateBanner(SQLiteDatabase db, Banner i,
			boolean updateImage) {
		ContentValues values = bannerToContentValues(i, updateImage);

		db.update(BANNER_TABLE_NAME, values, ID_COLUMN + " =?",
				new String[] { Long.toString(i.getId()) });
	}


	public static long insertOrUpdate(Context context, Banner item,
			boolean updateImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		ContentValues values = bannerToContentValues(item, updateImage);
		return db.insertWithOnConflict(BANNER_TABLE_NAME, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	public static void delete(Context context, long id) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		
		Banner item = getBanner(context, id, false);
//		if(item!= null){
//			Utility.deleteFile(Utility.getFilePath(), item.getFileName());
//		}
		db.delete(BANNER_TABLE_NAME, ID_COLUMN + " = ?",
				new String[] { String.valueOf(id) });
	}


}
