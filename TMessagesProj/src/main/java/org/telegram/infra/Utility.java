package org.telegram.infra;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.channels.dao.BannerDAO;
import org.telegram.channels.dao.CategoryDAO;
import org.telegram.channels.dao.ChannelDAO;
import org.telegram.channels.model.Banner;
import org.telegram.channels.model.Category;
import org.telegram.channels.model.Channel;
import org.telegram.groups.dao.GroupCategoryDAO;
import org.telegram.groups.dao.GroupDAO;
import org.telegram.groups.model.Group;
import org.telegram.groups.model.GroupCategory;
import org.telegram.messenger.SepehrSettingsHelper;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.StoreActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import ir.javan.messenger.R;

public class Utility {

	private static Date currentServerTime;

	public static boolean isInternetAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null

		&& activeNetworkInfo.isConnected();
	}

	public static void exitApp(Activity activity) {
		activity.moveTaskToBack(true);
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}

	public static Intent getCommentIntent(Activity activity, int marketId) {
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setData(Uri.parse("bazaar://details?id="
				+ activity.getPackageName()));
		return intent;
	}

	private static boolean UPDATING_APP = false;

	public static Bitmap loadImageFromFile(String fileName) {
		try {
			File f = new File(getFilePath(), fileName);
			if (!f.exists()) {
				return null;
			}
			Bitmap tmp = BitmapFactory.decodeFile(f.getAbsolutePath());
			return tmp;
		} catch (Exception e) {
			return null;
		}
	}


	public static String getFilePath() {
		return Constant.SAVED_IMAGE_DIR + File.separator + "gallery";
	}


	public static void deleteFile(String path, String filName) {
		File file = new File(getFilePath() + File.separator + filName);
		try {
			file.delete();
		} catch (Exception e) {

		}
	}


	public static boolean updateChannelsFromServer(Context context) {
		if(UPDATING_APP){
			return true;
		}
		UPDATING_APP = true;
		String result = "";
		long currentVersion = SharedPrefrenHelper.getChannelsVersion(context);
		Channel cc = ChannelDAO.getChannel(context, 243, false);
		try {
			String urlStr = String.format(
					Constant.CHANNEL_LIST_UPDATE_WEB_SERVICE_URL,
					currentVersion);
			result = Utility.getDataFromUrl(urlStr);
			if (result.length() > 0) {
				result = result.substring(result.indexOf("{"),
						result.lastIndexOf("}") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (result != null && result.length() > 0) {
			try {
				JSONObject jsonObject = new JSONObject(result);
				long newVersion = jsonObject.getLong(Constant.JSON_PARAM.VERSION);
				JSONArray changes = jsonObject
						.getJSONArray(Constant.JSON_PARAM.CHANNEL_CHANGES_LIST);
				for (int j = 0; j < changes.length(); j++) {
					JSONObject obj = changes.getJSONObject(j);
					long id = obj.getLong(Constant.JSON_PARAM.CHANNEL_ID);
					String imageUrl = obj.getString(Constant.JSON_PARAM.CHANNEL_IMAGE);
					String title = obj.getString(Constant.JSON_PARAM.CHANNEL_TITLE);
					String link = obj.getString(Constant.JSON_PARAM.CHANNEL_LINK);
					Constant.CHANNEL_TYPE type = Constant.CHANNEL_TYPE.getValueOf(obj.getString(Constant.JSON_PARAM.CHANNEL_TYPE));
					int order = obj.getInt(Constant.JSON_PARAM.CHANNEL_ORDER);
					int categoryId = obj.getInt(Constant.JSON_PARAM.CHANNEL_CAT_ID);
					Channel c = ChannelDAO.getChannel(context, id, false);
					boolean isNew = false;
					if (c != null) {
						if (!c.getImageUrl().trim().equals(imageUrl.trim())) {
							c.setImageDirty(true);
						}
					} else {
						isNew = true;
						c = new Channel();
						c.setImageDirty(true);
					}
					c.setId(id);
					c.setImageUrl(imageUrl);
					c.setTitle(title);
					c.setLink(link);
					c.setType(type);
					c.setShowOrder(order);
					c.setCategoryId(categoryId);

					if (isNew) {
						ChannelDAO.insertChannel(context, c);
					} else {
						ChannelDAO.updateChannel(context, c, false);
					}

				}

				JSONArray dels = jsonObject.getJSONArray(Constant.JSON_PARAM.CHANNEL_DEL_LIST);
				int delLen = dels.length();
				for (int i = 0; i < delLen; i++) {
					JSONObject obj = dels.getJSONObject(i);
					long id = obj.getLong(Constant.JSON_PARAM.CHANNEL_ID);

					ChannelDAO.delete(context, id);
				}

				SharedPrefrenHelper.putChannelsVersion(context, newVersion);
				Constant.log("update compeleted");
			} catch (Exception e) {
				Constant.log("failed update");
				e.printStackTrace();
				return false;
			}
		}
		UPDATING_APP = false;
		return true;
	}

	public static boolean updateBannersFromServer(Context context) {
		if(UPDATING_APP){
			return true;
		}
		UPDATING_APP = true;
		String result = "";
		long currentVersion = SharedPrefrenHelper.getChannlesBannersVersion(context);
		try {
			String urlStr = String.format(
					Constant.BANNER_LIST_UPDATE_WEB_SERVICE_URL,
					currentVersion);
			result = Utility.getDataFromUrl(urlStr);
			if (result.length() > 0) {
				result = result.substring(result.indexOf("{"),
						result.lastIndexOf("}") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (result != null && result.length() > 0) {
			try {
				JSONObject jsonObject = new JSONObject(result);
				long newVersion = jsonObject.getLong(Constant.JSON_PARAM.VERSION);
				JSONArray changes = jsonObject
						.getJSONArray(Constant.JSON_PARAM.BANNER_CHANGES_LIST);
				for (int j = 0; j < changes.length(); j++) {
					JSONObject obj = changes.getJSONObject(j);
					long id = obj.getLong(Constant.JSON_PARAM.BANNER_ID);
					String imageUrl = obj.getString(Constant.JSON_PARAM.BANNER_IMAGE);
					String title = obj.getString(Constant.JSON_PARAM.BANNER_TITLE);
					String link = obj.getString(Constant.JSON_PARAM.BANNER_LINK);
					Constant.BANNER_LINK_TYPE type = Constant.BANNER_LINK_TYPE.getValueOf(obj.getString(Constant.JSON_PARAM.BANNER_TYPE));
					int order = obj.getInt(Constant.JSON_PARAM.BANNER_ORDER);
					Banner c = BannerDAO.getBanner(context, id, false);
					boolean isNew = false;
					if (c != null) {
						if (!c.getImageUrl().trim().equals(imageUrl.trim())) {
							c.setImageDirty(true);
						}
					} else {
						isNew = true;
						c = new Banner();
						c.setImageDirty(true);
					}
					c.setId(id);
					c.setImageUrl(imageUrl);
					c.setTitle(title);
					c.setLink(link);
					c.setType(type);
					c.setShowOrder(order);

					if (isNew) {
						BannerDAO.insertBanner(context, c);
					} else {
						BannerDAO.updateBanner(context, c, false);
					}

				}

				JSONArray dels = jsonObject.getJSONArray(Constant.JSON_PARAM.BANNER_DEL_LIST);
				int delLen = dels.length();
				for (int i = 0; i < delLen; i++) {
					JSONObject obj = dels.getJSONObject(i);
					long id = obj.getLong(Constant.JSON_PARAM.BANNER_ID);

					BannerDAO.delete(context, id);
				}

				SharedPrefrenHelper.putChannelsBannersVersion(context, newVersion);
				Constant.log("update compeleted");
			} catch (Exception e) {
				Constant.log("failed update");
				e.printStackTrace();
				return false;
			}
		}
		UPDATING_APP = false;
		return true;
	}

	public static boolean updateCategoryFromServer(Context context) {
		String result = "";
		long currentVersion = SharedPrefrenHelper
				.getChannelsCategoryVersion(context);

		try {
			String urlStr = String.format(
					Constant.CATEGORY_LIST_UPDATE_WEB_SERVICE_URL,
					currentVersion);
			result = Utility.getDataFromUrl(urlStr);
			if (result.length() > 0) {
				result = result.substring(result.indexOf("{"),
						result.lastIndexOf("}") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (result != null && result.length() > 0) {
			try {
				JSONObject jsonObject = new JSONObject(result);
				long newVersion = jsonObject.getLong(Constant.JSON_PARAM.VERSION);
				JSONArray changes = jsonObject
						.getJSONArray(Constant.JSON_PARAM.CATEGORY_CHANGES_LIST);
				for (int j = 0; j < changes.length(); j++) {
					JSONObject obj = changes.getJSONObject(j);
					long id = obj.getLong(Constant.JSON_PARAM.CATEGORY_ID);
					String imageUrl = obj
							.getString(Constant.JSON_PARAM.CATEGORY_IMAGE_URL);
					String title = obj.getString(Constant.JSON_PARAM.CATEGORY_NAME);

					Category c = CategoryDAO.get(context, id, false);
					boolean isNew = false;
					if (c != null) {
						if (!c.getImageUrl().trim().equals(imageUrl.trim())) {
							c.setImageDirty(true);
						}
					} else {
						isNew = true;
						c = new Category();
						c.setImageDirty(true);
					}
/*                    boolean updateImage=false;
                    if ((c.getImageUrl() == null
                            || !c.getImageUrl().trim().equals(imageUrl.trim())
                            ||c.getImage()==null) && imageUrl!=null) {

                        byte[] thumb = getByteArrayFromURL(imageUrl, true);
                        if (thumb != null) {
                            c.setImage(thumb);
                            updateImage=true;
                        }

                    }*/
					c.setId(id);
					c.setImageUrl(imageUrl);
					c.setTitle(title);

					if (isNew) {
						CategoryDAO.insert(context, c);
					} else {
						CategoryDAO.update(context, c, false);
					}

				}

				JSONArray dels = jsonObject
						.getJSONArray(Constant.JSON_PARAM.CATEGORY_DEL_LIST);
				int delLen = dels.length();
				for (int i = 0; i < delLen; i++) {
					JSONObject obj = dels.getJSONObject(i);
					long id = obj.getLong(Constant.JSON_PARAM.CATEGORY_ID);

					CategoryDAO.delete(context, id);
				}

				SharedPrefrenHelper.putChannelsCategoryVersion(context, newVersion);

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static String getDataFromUrl(String url) {
		String output = "";
		try {
			HttpGet getRequest = new HttpGet(url);
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(getRequest);
			InputStream is = response.getEntity().getContent();
			BufferedReader in = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			String str;
			while ((str = in.readLine()) != null) {
				output = output.concat(str);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	public static byte[] getByteArrayFromURL(String src, boolean isTransparent) {
		try {

			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			Bitmap.CompressFormat format = isTransparent ? Bitmap.CompressFormat.PNG
					: Bitmap.CompressFormat.JPEG;
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			myBitmap.compress(format, Constant.IMAGE_QUALITY, stream);
			byte[] logo = stream.toByteArray();
			return logo;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getAppVersion(Context context) {
		String packageName = context.getPackageName();
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
			return pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;

	}

	public static boolean activateApp(Activity activity, String key) {
		String result="";
		try {
			String urlStr = String.format(Constant.ACTIVATION_SERVICE_URL, key,getDeviceKey(activity));
			result = Utility.getDataFromUrl(urlStr);
			if (result.length() > 0) {
				result = result.substring(result.indexOf("{"),
						result.lastIndexOf("}") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (result != null && result.length() > 0) {
			try {
				JSONObject jsonObject = new JSONObject(result);
				boolean canActivate = jsonObject.getBoolean("result");
				return canActivate;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}

	public static String getDeviceKey(Activity activity){
//        final TelephonyManager tm = (TelephonyManager) activity.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + Math.random();//tm.getDeviceId();
		tmSerial = "" + Math.random();//tm.getSimSerialNumber();
		androidId = "" + android.provider.Settings.Secure.getString(activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		return deviceUuid.toString();
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
				.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static boolean updateGroupsFromServer(Context context) {
		if(UPDATING_APP){
			return true;
		}
		UPDATING_APP = true;
		String result = "";
		long currentVersion = SharedPrefrenHelper.getGroupsVersion(context);
		try {
			String urlStr = String.format(
					org.telegram.infra.Constant.GROUP_LIST_UPDATE_WEB_SERVICE_URL,
					currentVersion);
			result = Utility.getDataFromUrl(urlStr);
			if (result.length() > 0) {
				result = result.substring(result.indexOf("{"),
						result.lastIndexOf("}") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (result != null && result.length() > 0) {
			try {
				JSONObject jsonObject = new JSONObject(result);
				long newVersion = jsonObject.getLong(org.telegram.infra.Constant.JSON_PARAM.VERSION);
				JSONArray changes = jsonObject
						.getJSONArray(org.telegram.infra.Constant.JSON_PARAM.GROUP_CHANGES_LIST);
				for (int j = 0; j < changes.length(); j++) {
					JSONObject obj = changes.getJSONObject(j);
					long id = obj.getLong(org.telegram.infra.Constant.JSON_PARAM.GROUP_ID);
					String imageUrl = obj.getString(org.telegram.infra.Constant.JSON_PARAM.GROUP_IMAGE);
					String title = obj.getString(org.telegram.infra.Constant.JSON_PARAM.GROUP_TITLE);
					String link = obj.getString(org.telegram.infra.Constant.JSON_PARAM.GROUP_LINK);
					org.telegram.infra.Constant.GROUP_TYPE type = org.telegram.infra.Constant.GROUP_TYPE.getValueOf(obj.getString(org.telegram.infra.Constant.JSON_PARAM.GROUP_TYPE));
					int order = obj.getInt(org.telegram.infra.Constant.JSON_PARAM.GROUP_ORDER);
					int categoryId = obj.getInt(org.telegram.infra.Constant.JSON_PARAM.GROUP_CAT_ID);
					Group c = GroupDAO.getGroup(context, id, false);
					boolean isNew = false;
					if (c != null) {
						if (!c.getImageUrl().trim().equals(imageUrl.trim())) {
							c.setImageDirty(true);
						}
					} else {
						isNew = true;
						c = new Group();
						c.setImageDirty(true);
					}
					c.setId(id);
					c.setImageUrl(imageUrl);
					c.setTitle(title);
					c.setLink(link);
					c.setType(type);
					c.setShowOrder(order);
					c.setCategoryId(categoryId);

					if (isNew) {
						GroupDAO.insertGroup(context, c);
					} else {
						GroupDAO.updateGroup(context, c, false);
					}

				}

				JSONArray dels = jsonObject.getJSONArray(org.telegram.infra.Constant.JSON_PARAM.GROUP_DEL_LIST);
				int delLen = dels.length();
				for (int i = 0; i < delLen; i++) {
					JSONObject obj = dels.getJSONObject(i);
					long id = obj.getLong(org.telegram.infra.Constant.JSON_PARAM.GROUP_ID);

					GroupDAO.delete(context, id);
				}

				SharedPrefrenHelper.putGroupsVersion(context, newVersion);
				Constant.log("update compeleted");
			} catch (Exception e) {
				Constant.log("failed update");
				e.printStackTrace();
				return false;
			}
		}
		UPDATING_APP = false;
		return true;
	}

	public static boolean updateGroupCategoryFromServer(Context context) {
		String result = "";
		long currentVersion = SharedPrefrenHelper
				.getGroupCategoryVersion(context);

		try {
			String urlStr = String.format(
					org.telegram.infra.Constant.GROUP_CATEGORY_LIST_UPDATE_WEB_SERVICE_URL,
					currentVersion);
			result = Utility.getDataFromUrl(urlStr);
			if (result.length() > 0) {
				result = result.substring(result.indexOf("{"),
						result.lastIndexOf("}") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (result != null && result.length() > 0) {
			try {
				JSONObject jsonObject = new JSONObject(result);
				long newVersion = jsonObject.getLong(org.telegram.infra.Constant.JSON_PARAM.VERSION);
				JSONArray changes = jsonObject
						.getJSONArray(org.telegram.infra.Constant.JSON_PARAM.CATEGORY_CHANGES_LIST);
				for (int j = 0; j < changes.length(); j++) {
					JSONObject obj = changes.getJSONObject(j);
					long id = obj.getLong(org.telegram.infra.Constant.JSON_PARAM.CATEGORY_ID);
					String imageUrl = obj
							.getString(org.telegram.infra.Constant.JSON_PARAM.CATEGORY_IMAGE_URL);
					String title = obj.getString(org.telegram.infra.Constant.JSON_PARAM.CATEGORY_NAME);

					GroupCategory c = GroupCategoryDAO.get(context, id, false);
					boolean isNew = false;
					if (c != null) {
						if (!c.getImageUrl().trim().equals(imageUrl.trim())) {
							c.setImageDirty(true);
						}
					} else {
						isNew = true;
						c = new GroupCategory();
						c.setImageDirty(true);
					}
/*                    boolean updateImage=false;
                    if ((c.getImageUrl() == null
                            || !c.getImageUrl().trim().equals(imageUrl.trim())
                            ||c.getImage()==null) && imageUrl!=null) {

                        byte[] thumb = getByteArrayFromURL(imageUrl, true);
                        if (thumb != null) {
                            c.setImage(thumb);
                            updateImage=true;
                        }

                    }*/
					c.setId(id);
					c.setImageUrl(imageUrl);
					c.setTitle(title);

					if (isNew) {
						GroupCategoryDAO.insert(context, c);
					} else {
						GroupCategoryDAO.update(context, c, false);
					}

				}

				JSONArray dels = jsonObject
						.getJSONArray(org.telegram.infra.Constant.JSON_PARAM.CATEGORY_DEL_LIST);
				int delLen = dels.length();
				for (int i = 0; i < delLen; i++) {
					JSONObject obj = dels.getJSONObject(i);
					long id = obj.getLong(org.telegram.infra.Constant.JSON_PARAM.CATEGORY_ID);

					GroupCategoryDAO.delete(context, id);
				}

				SharedPrefrenHelper.putGroupCategoryVersion(context, newVersion);

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static void showGuidDialog(Activity activity, String title, String text) {
		if(activity==null){
			return;
		}
		new AlertDialog.Builder(activity)
				.setTitle(title)
				.setMessage(text)
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.show();
	}

	public static void showBuyPremiumDialog(final Activity activity) {
		if(activity==null){
			return;
		}
		new AlertDialog.Builder(activity)
				.setTitle(R.string.premium_version)
				.setMessage(R.string.buy_premium_version)
				.setPositiveButton(R.string.goto_store, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.startActivity(new Intent(activity, StoreActivity.class));
					}
				})
				.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.show();
	}

	public static void updateTimeFromServer(Context context, final Runnable... onUpdateRunnable) {
		new Thread(){
			@Override
			public void run() {
				String result=null;
				try {
					String urlStr = Constant.TIME_UPDATE_WEB_SERVICE_URL;
					result = Utility.getDataFromUrl(urlStr);
					if (result.length() > 0) {
						result = result.substring(result.indexOf("{"),
								result.lastIndexOf("}") + 1);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				if (result != null && result.length() > 0) {
					try {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						JSONObject j = new JSONObject(result);
						String timeStr = j.getString(Constant.JSON_PARAM.TIME);
						currentServerTime = dateFormat.parse(timeStr);
						Constant.log("time="+currentServerTime.getTime());
						if(onUpdateRunnable!=null && onUpdateRunnable.length>0){
							onUpdateRunnable[0].run();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	public  static Date getServerTime(){
		return currentServerTime;
	}

	public static boolean canUseChatrooms(Context context) {
		updateTimeFromServer(context);
		if(getServerTime().getTime()<= SepehrSettingsHelper.getChatroomExpireDate(context).getTime()){
			return true;
		}else {
			return false;
		}
	}
}
