package org.telegram.channels.dao;

import org.telegram.SQLite.DBHelper;
import org.telegram.infra.Constant;
import org.telegram.channels.model.Category;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CategoryDAO {

	private static final String CATEGORY_TABLE_NAME = "category";

	private static final String ID_COLUMN = "id";
	private static final String TITLE_COLUMN = "title";
	private static final String PARENT_CAT_ID_COLUMN = "parent_cat_id";
	private static final String TYPE_COLUMN = "type";
	private static final String IMAGE_COLUMN = "image";
	private static final String IMAGE_URL_COLUMN = "image_url";
	private static final String IS_IMAGE_DIRTY_COLUMN = "is_image_dirty";

	private static final String[] ALL_COLUMN = new String[] { ID_COLUMN,
			TITLE_COLUMN, PARENT_CAT_ID_COLUMN, IMAGE_COLUMN, IMAGE_URL_COLUMN,
			TYPE_COLUMN ,IS_IMAGE_DIRTY_COLUMN};

	public static final String CATEGORY_TABLE_CREATE = "create table  IF NOT EXISTS "
			+ CATEGORY_TABLE_NAME
			+ " (  "
			+ ID_COLUMN
			+ "  integer primary key autoincrement,  "
			+ TITLE_COLUMN
			+ " text,"
			+ IMAGE_COLUMN
			+ " blob,"
			+ IMAGE_URL_COLUMN
			+ " text,"
			+ TYPE_COLUMN
			+ " text,"
			+ IS_IMAGE_DIRTY_COLUMN
			+ " integer ,"
			+ PARENT_CAT_ID_COLUMN
			+ " integer "
			+ ");";

	public static long insert(SQLiteDatabase db, Category i) {
		ContentValues values = categoryToContentValues(i,true);
		return db.insert(CATEGORY_TABLE_NAME, null, values);
	}

	public static long insert(Context context, Category i) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return insert(db, i);
	}

	private static ContentValues categoryToContentValues(Category i,boolean updateImage) {
		ContentValues values = new ContentValues();
		values.put(ID_COLUMN, i.getId());
		values.put(TITLE_COLUMN, i.getTitle());
		values.put(PARENT_CAT_ID_COLUMN, i.getParentCatId());
		if(updateImage){
			values.put(IMAGE_COLUMN,
				(i.getImage() == null) ? new byte[] {} : i.getImage());
		}
		values.put(IMAGE_URL_COLUMN, i.getImageUrl());
		values.put(IS_IMAGE_DIRTY_COLUMN,i.isImageDirty() ? Constant.TRUE_VALUE : Constant.FALSE_VALUE);
		return values;
	}

	public static Category get(Context context, long id, boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		Cursor c = db.query(CATEGORY_TABLE_NAME, ALL_COLUMN, ID_COLUMN + " =?",
				new String[] { String.valueOf(id) }, null, null, null);
		Category g = null;
		while (c.moveToNext()) {
			g = getCategoryFromCursor(c, loadImage);
		}
		c.close();
		return g;
	}

	public static Category get(SQLiteDatabase db, long id, boolean loadImage) {
		Cursor c = db.query(CATEGORY_TABLE_NAME, ALL_COLUMN, ID_COLUMN + " =?",
				new String[] { String.valueOf(id) }, null, null, null);
		Category g = null;
		while (c.moveToNext()) {
			g = getCategoryFromCursor(c, loadImage);
		}
		c.close();
		return g;

	}

	public static List<Category> getAllCategory(Context context,
			boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return getAllCategory(db, loadImage);
	}

	public static List<Category> getAllCategory(SQLiteDatabase db,
			boolean loadImage) {
		Cursor c = db.query(CATEGORY_TABLE_NAME, ALL_COLUMN, null, null, null,
				null, ID_COLUMN + " asc ");
		List<Category> gs = new ArrayList<Category>();
		while (c.moveToNext()) {
			Category g = getCategoryFromCursor(c, loadImage);
			gs.add(g);
		}
		c.close();
		return gs;

	}

	private static Category getCategoryFromCursor(Cursor c, boolean loadImage) {
		Category i = new Category();
		i.setId(c.getLong(c.getColumnIndex(ID_COLUMN)));
		i.setTitle(c.getString(c.getColumnIndex(TITLE_COLUMN)));
		i.setParentCatId(c.getLong(c.getColumnIndex(PARENT_CAT_ID_COLUMN)));
		if (loadImage) {
			i.setImage(c.getBlob(c.getColumnIndex(IMAGE_COLUMN)));
		}
		i.setImageUrl(c.getString(c.getColumnIndex(IMAGE_URL_COLUMN)));
		i.setImageDirty(c.getLong(c.getColumnIndex(IS_IMAGE_DIRTY_COLUMN)) == Constant.TRUE_VALUE);

		return i;
	}

	public static void update(Context context, Category i,boolean updateImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		ContentValues values = categoryToContentValues(i,updateImage);

		db.update(CATEGORY_TABLE_NAME, values, ID_COLUMN + " =?",
				new String[] { Long.toString(i.getId()) });
	}

	public static void delete(Context context, long id) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		db.delete(CATEGORY_TABLE_NAME, ID_COLUMN + " = ?",
				new String[] { String.valueOf(id) });

	}

	/*
	 * public static void genertateData(Context context, SQLiteDatabase db) {
	 * for(int i=1;i<12;i++){ Category cat = new Category();
	 * cat.setTitle("cat"+i);
	 * cat.setImage(Utility.getBytesOfDrawableResourcePng(context,
	 * R.drawable.test)); cat.setImageUrl(""); cat.setType(CHANNEL_TYPE.STICKER);
	 * insertCategory(db, cat); } for(int i=1;i<12;i++){ Category cat = new
	 * Category(); cat.setTitle("cat"+i);
	 * cat.setImage(Utility.getBytesOfDrawableResourcePng(context,
	 * R.drawable.ic_launcher)); cat.setImageUrl("");
	 * cat.setType(CHANNEL_TYPE.IMAGE); insertCategory(db, cat); } }
	 */

}
