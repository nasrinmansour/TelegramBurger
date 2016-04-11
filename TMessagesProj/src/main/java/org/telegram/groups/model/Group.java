package org.telegram.groups.model;

import java.util.Date;

import android.graphics.Bitmap;

import org.telegram.infra.Constant;

public class Group {
    private long id;
    private String title;
    private Bitmap imageBitmap;
    private byte[] imageBytes;
    private String imageUrl;
    private long categoryId;
    private Date createDate;
    private Constant.GROUP_TYPE type= Constant.GROUP_TYPE.PUBLICGROUP;
    private String description;
    private String link;
    private int showOrder;
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

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap sampleImage) {
        this.imageBitmap = sampleImage;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] sampleImageBytes) {
        this.imageBytes = sampleImageBytes;
    }

    public Constant.GROUP_TYPE getType() {
        return type;
    }

    public void setType(Constant.GROUP_TYPE type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(int showOrder) {
        this.showOrder = showOrder;
    }

    public void setImage(byte[] imageByte) {
        this.imageBytes=imageByte;
    }

    public boolean isImageDirty() {
        return imageDirty;
    }

    public void setImageDirty(boolean imageDirty) {
        this.imageDirty = imageDirty;
    }
}
