package org.telegram.infra;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.telegram.messenger.ApplicationLoader;

import java.util.Date;


public class SharedPrefrenHelper {

	private static final String EXPIRED_APP_VERSION_KEY="expired_app_version";
	private static final String UPDATE_CHECKER_COUNTER_KEY="update_checker_counter";
	private static final String GROUPS_VERSION = "app_group_version";
	private static final String GROUPS_CATEGORY_VERSION = "app_group_category_version";
	private static final String GROUPS_BANNERS_VERSION = "app_group_banner_version";
	private static final String CHANNELS_VERSION = "app_channel_version";
	private static final String CHANNELS_CATEGORY_VERSION = "app_category_version";
	private static final String CHANNELS_BANNERS_VERSION = "app_banner_version";
	private static final String IS_GHOST_GUID_SHOWED_KEY = "is_ghost_guid_showed";
	private static final String APP_INSTALL_DATE_KEY = "app_install_date";


	private static Date appInstallationDate;

	public static void putExpiredAppVersion(Context context, int val){
		SharedPreferences share= PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putInt(EXPIRED_APP_VERSION_KEY, val);
		edit.commit();
	}
	
	public static int getExpiredAppVersion(Context context) {
		SharedPreferences share= PreferenceManager.getDefaultSharedPreferences(context);
		return share.getInt(EXPIRED_APP_VERSION_KEY, -1);
	}

	public static void putUpdateCheckerCounter(Context context, int count){
		SharedPreferences share= PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putInt(UPDATE_CHECKER_COUNTER_KEY, count);
		edit.commit();
	}
	public static int getUpdateCheckerCounter(Context context){
		SharedPreferences share= PreferenceManager.getDefaultSharedPreferences(context);
		return share.getInt(UPDATE_CHECKER_COUNTER_KEY, 1);
	}

	public static void putGroupsVersion(Context context, long version) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putLong(GROUPS_VERSION, version);
		edit.commit();
	}

	public static long getGroupsVersion(Context context) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		return share.getLong(GROUPS_VERSION, 0l);
	}


	public static void putGroupBannersVersion(Context context, long version) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putLong(GROUPS_BANNERS_VERSION, version);
		edit.commit();
	}

	public static long getGroupBannersVersion(Context context) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		return share.getLong(GROUPS_BANNERS_VERSION, 0l);
	}

	public static void putGroupCategoryVersion(Context context, long version) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putLong(GROUPS_CATEGORY_VERSION, version);
		edit.commit();
	}

	public static long getGroupCategoryVersion(Context context) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		return share.getLong(GROUPS_CATEGORY_VERSION, 0l);
	}

	public static void putChannelsVersion(Context context, long version) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putLong(CHANNELS_VERSION, version);
		edit.commit();
	}

	public static long getChannelsVersion(Context context) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		return share.getLong(CHANNELS_VERSION, 0l);
	}


	public static void putChannelsBannersVersion(Context context, long version) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putLong(CHANNELS_BANNERS_VERSION, version);
		edit.commit();
	}

	public static long getChannlesBannersVersion(Context context) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		return share.getLong(CHANNELS_BANNERS_VERSION, 0l);
	}

	public static void putChannelsCategoryVersion(Context context, long version) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putLong(CHANNELS_CATEGORY_VERSION, version);
		edit.commit();
	}

	public static long getChannelsCategoryVersion(Context context) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		return share.getLong(CHANNELS_CATEGORY_VERSION, 0l);
	}

	public static void putIsGhostGuidShowed(Context context, Boolean val) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putBoolean(IS_GHOST_GUID_SHOWED_KEY, val);
		edit.commit();
	}

	public static boolean getIsGhostGuidShowed(Context context) {
		SharedPreferences share= PreferenceManager.getDefaultSharedPreferences(context);
		return share.getBoolean(IS_GHOST_GUID_SHOWED_KEY, false);
	}

	public static void putAppInstallationDate(Context context, Date val) {
		SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = share.edit();
		edit.putLong(APP_INSTALL_DATE_KEY, val.getTime());
		edit.commit();
	}

	public static long getAppInstallationDateMill(Context context) {
		SharedPreferences share= PreferenceManager.getDefaultSharedPreferences(context);
		return share.getLong(APP_INSTALL_DATE_KEY, 0);
	}


	public static void putAppInstallationDateIfNotSet(Context context,Date serverNow) {
		if(appInstallationDate!=null){
			return;
		}
		long savedInstallDate = getAppInstallationDateMill(context);
		if(savedInstallDate!=0){
			appInstallationDate=new Date(savedInstallDate);
			return;
		}
		appInstallationDate = serverNow;
		putAppInstallationDate(context,appInstallationDate);
	}
}
