package org.telegram.groups.dao;

import org.telegram.SQLite.DBHelper;
import org.telegram.infra.Constant;
import org.telegram.groups.model.Group;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GroupDAO {

	private static final String GROUP_TABLE_NAME = "group_tbl";

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

	public static final String GROUP_TABLE_CREATE = "create table  IF NOT EXISTS "
			+ GROUP_TABLE_NAME
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

	public static long insertGroup(Context context, Group i) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return insertGroup(context, db, i);
	}

	public static long insertGroup(Context context, SQLiteDatabase db,
			Group i) {
		i.setCreateDate(new Date(System.currentTimeMillis()));

		ContentValues values = groupToContentValues(i, true);
		return db.insert(GROUP_TABLE_NAME, null, values);
	}

	private static ContentValues groupToContentValues(Group i,
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

	public static Group getGroup(Context context, long id, boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		Cursor c = db.query(GROUP_TABLE_NAME, ALL_COLUMN,
				ID_COLUMN + " =?", new String[]{String.valueOf(id)}, null,
				null, null);
		Group g = null;
		while (c.moveToNext()) {
			g = getGroupFromCursor(c,loadImage);
		}
		c.close();
		return g;
	}

	public static Group getGroup(SQLiteDatabase db, long id,
			boolean loadImage) {
		Cursor c = db.query(GROUP_TABLE_NAME, ALL_COLUMN,
				ID_COLUMN + " =?", new String[] { String.valueOf(id) }, null,
				null, null);
		Group g = null;
		while (c.moveToNext()) {
			g = getGroupFromCursor(c, loadImage);
		}
		c.close();
		return g;

	}

	public static List<Group> getAllGroups(Context context, boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return getAllGroups(db, loadImage);
	}

	public static List<Group> getAllGroups(SQLiteDatabase db, boolean loadImage) {
		Cursor c = db.query(GROUP_TABLE_NAME, ALL_COLUMN, null, null, null,
				null, SHOW_ORDER_COLUMN + " desc ");
		List<Group> gs = new ArrayList<Group>();
		while (c.moveToNext()) {
			Group g = getGroupFromCursor(c, loadImage);
			gs.add(g);
		}
		c.close();
		return gs;

	}

	public static List<Group> getCategoryGroups(Context context,
			long catId, boolean loadImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		return getCategoryGroups(db, catId, loadImage);
	}

	public static List<Group> getCategoryGroups(SQLiteDatabase db,
			long catId, boolean loadImage) {
		Cursor c = db.query(GROUP_TABLE_NAME, ALL_COLUMN,
				CATEGORY_ID_COLUMN + "=? ",
				new String[]{String.valueOf(catId)}, null, null, SHOW_ORDER_COLUMN
						+ " desc ");
		List<Group> gs = new ArrayList<Group>();
		while (c.moveToNext()) {
			Group g = getGroupFromCursor(c,loadImage);
			gs.add(g);
		}
		c.close();
		return gs;

	}

	private static Group getGroupFromCursor(Cursor c, boolean loadImage) {
		Group i = new Group();
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
		i.setType(Constant.GROUP_TYPE.getValueOf(c.getString(c.getColumnIndex(TYPE_COLUMN))));
		i.setImageDirty(c.getLong(c.getColumnIndex(IS_IMAGE_DIRTY_COLUMN)) == Constant.TRUE_VALUE);

		return i;
	}

	public static void updateGroup(Context context, Group i,
			boolean updateImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		updateGroup(db, i, updateImage);
	}

	public static void updateGroup(SQLiteDatabase db, Group i,
			boolean updateImage) {
		ContentValues values = groupToContentValues(i, updateImage);

		db.update(GROUP_TABLE_NAME, values, ID_COLUMN + " =?",
				new String[] { Long.toString(i.getId()) });
	}


	public static long insertOrUpdate(Context context, Group item,
			boolean updateImage) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		ContentValues values = groupToContentValues(item, updateImage);
		return db.insertWithOnConflict(GROUP_TABLE_NAME, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	public static void delete(Context context, long id) {
		SQLiteDatabase db = (new DBHelper(context)).getWritableDatabase();
		
		Group item = getGroup(context, id, false);
//		if(item!= null){
//			Utility.deleteFile(Utility.getFilePath(), item.getFileName());
//		}
		db.delete(GROUP_TABLE_NAME, ID_COLUMN + " = ?",
				new String[] { String.valueOf(id) });
	}


}
