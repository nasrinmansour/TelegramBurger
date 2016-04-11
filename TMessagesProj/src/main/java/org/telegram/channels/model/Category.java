package org.telegram.channels.model;


public class Category {
	private long id;
	private String title;
	private long parentCatId;
	private byte[] image;
	private String imageUrl;
	private boolean imageDirty=true;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public long getParentCatId() {
		return parentCatId;
	}
	public void setParentCatId(long parentCatId) {
		this.parentCatId = parentCatId;
	}
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public boolean isImageDirty() {
		return imageDirty;
	}
	public void setImageDirty(boolean imageDirty) {
		this.imageDirty = imageDirty;
	}
}
