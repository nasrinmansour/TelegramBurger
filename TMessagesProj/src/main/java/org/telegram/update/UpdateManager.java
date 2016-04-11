package org.telegram.update;

import ir.javan.messenger.R;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.telegram.infra.SharedPrefrenHelper;
import org.telegram.infra.Utility;
import org.telegram.infra.Constant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.Button;

public class UpdateManager {

	private static final int CHECKS_PERIOD_RUN_COUNT = 6;

	public class Markets {
		public static final int BAZAAR = 1;
		public static final int TELEGRAM = 5;
	}

	public class UpdateResponseParams {
		public static final String UpdateState = "m";
		public static final String UpdateVersionCode = "ver";
		public static final String UpdateVersionName = "ver_name";
		public static final String UpdateMessage = "msg";
	}

	@SuppressLint("NewApi")
	public static void checkForUpdate(Activity activity, final int marketId, final boolean isAutoCheck) {
		try {
			int counter = SharedPrefrenHelper.getUpdateCheckerCounter(activity);
			SharedPrefrenHelper.putUpdateCheckerCounter(activity, (counter + 1)
					% CHECKS_PERIOD_RUN_COUNT);
			String packageName = activity.getPackageName();
			PackageInfo pInfo = activity.getPackageManager().getPackageInfo(packageName, 0);
			int appVersion = pInfo.versionCode;
			Constant.log("" + appVersion);
			if (SharedPrefrenHelper.getExpiredAppVersion(activity) >= appVersion) {
				UpdateManager.showUpdateAvailableDialog(activity, marketId, packageName, true, false);
			} else {
				if (counter % CHECKS_PERIOD_RUN_COUNT != 0
						|| !Utility.isInternetAvailable(activity)) {
					return;
				}
				AsyncTask<String, String, Update> asyncTask = new AsyncTask<String, String, Update>() {
					private Activity activity;
					private int appVersion;
					private String packageName;

					@Override
					protected Update doInBackground(String... params) {
						if(packageName.endsWith(".beta")){
							packageName=packageName.replace(".beta","");
						}
						try {
							String url = "http://sepehrapps.tk/apps_manager/?a=app.get_ver&market_id="
									+ marketId
									+ "&app_id="
									+ packageName
									+ "&ver="
									+ appVersion
									+ "&rnd="
									+ Math.random();
							HttpGet getRequest = new HttpGet(url);
							HttpClient client = new DefaultHttpClient();
							HttpResponse response = client.execute(getRequest);
							InputStream is = response.getEntity().getContent();
							BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
							StringBuffer total = new StringBuffer();
							String line;
							while ((line = reader.readLine()) != null) {
								total.append(line);
							}
							JSONObject jsonResponse = new JSONObject(total.toString());
							Update update = new Update();
							update.setUpdateState(jsonResponse.getInt(UpdateManager.UpdateResponseParams.UpdateState));
							update.setUpdateMessage(jsonResponse.getString(UpdateManager.UpdateResponseParams.UpdateMessage));
							update.setVersionCode(jsonResponse.getInt(UpdateManager.UpdateResponseParams.UpdateVersionCode));
							update.setVersionName(jsonResponse.getString(UpdateManager.UpdateResponseParams.UpdateVersionName));

							return update;
						} catch (Exception e) {
							e.printStackTrace();
						}

						return null;
					}

					public AsyncTask<String, String, Update> init(Activity activity, int appVersion, String packageName) {
						this.activity = activity;
						this.appVersion = appVersion;
						this.packageName = packageName;
						return this;
					}

					protected void onPostExecute(Update update) {
						if (update != null) {
							if (update.isForceUpdateNeeded()) {
								SharedPrefrenHelper.putExpiredAppVersion(activity, appVersion);
								showUpdateAvailableDialog(activity, marketId, packageName, update.isForceUpdateNeeded(), true);
							} else {
								if (update.isUpdateAvaiable()) {
									showUpdateAvailableDialog(activity, marketId, packageName, update.isForceUpdateNeeded(), true);
								}
							}
						}
					};
				}.init(activity, appVersion, packageName);
				if(Build.VERSION.SDK_INT>=11){
					asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				}else{
					asyncTask.execute("");
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	protected static void showUpdateAvailableDialog(final Activity activity, final int marketId, final String packageName, final boolean isForceUpdate, final boolean allowContinue) {
		Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage((isForceUpdate) ? UpdateManager.locateAppName(activity, R.string.update_dialog_force_text) : UpdateManager.locateAppName(activity, R.string.update_dialog_optional_text));
		builder.setTitle(R.string.update_available);
		builder.setCancelable((!isForceUpdate || allowContinue));
		builder.setPositiveButton(R.string.update, null);
		builder.setNegativeButton((isForceUpdate && !allowContinue) ? R.string.exit : R.string.cancel_update, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				if (isForceUpdate && !allowContinue) {
					Utility.exitApp(activity);
				} else {
					dialog.dismiss();
				}
			}
		});
		final AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(final DialogInterface dialog) {

				Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						if (Utility.isInternetAvailable(activity)) {
							activity.startActivity(getMarketUpdateIntent(packageName, marketId));
							if (!isForceUpdate || allowContinue) {
								dialog.dismiss();
							}
						} else {
							Builder b = new AlertDialog.Builder(activity)
							.setMessage(R.string.check_connection_and_try_again)
							.setTitle(R.string.internet_connection_fail)
							.setPositiveButton(R.string.OK, null);
							b.show();
						}
					}
				});
			}
		});
		alertDialog.show();
	}

	private static String locateAppName(Context c, int stringResource) {
		String s = c.getString(stringResource).replaceAll("@app_name", c.getString(R.string.AppName));
		return s;
	}

	protected static Intent getMarketUpdateIntent(String packageName, int marketId) {
		Intent outIntent = null;
		switch (marketId) {
		case Markets.TELEGRAM:
			outIntent = new Intent(Intent.ACTION_VIEW);
			outIntent.setData(Uri.parse("http://www.intellidict.ir/m/apks/" + packageName+".apk"));
			break;
		default:
			outIntent = new Intent(Intent.ACTION_VIEW);
			outIntent.setData(Uri.parse("bazaar://details?id=" + packageName));
			break;
		}
		return outIntent;
	}

}
