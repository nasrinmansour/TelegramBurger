package org.telegram.update;

public class Update {
	private static final int STATE_FORCE_UPDATE_NEEDED = 10;
	private static final int STATE_OPTIONAL_UPDATE_AVAILABLE = 11;
	private static final int STATE_NO_UPDATE_NEEDED = 0;
	private int versionCode;
	private String versionName;
	private int updateState;
	private String updateMessage;

	public boolean isForceUpdateNeeded() {
		return updateState==STATE_FORCE_UPDATE_NEEDED;
	}

	public boolean isUpdateAvaiable() {
		return updateState==STATE_FORCE_UPDATE_NEEDED || updateState==STATE_OPTIONAL_UPDATE_AVAILABLE;
	}

	public int getUpdateState() {
		return updateState;
	}

	public void setUpdateState(int updateState) {
		this.updateState = updateState;
	}

	public String getUpdateMessage() {
		return updateMessage;
	}

	public void setUpdateMessage(String updateMessage) {
		this.updateMessage = updateMessage;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
}
