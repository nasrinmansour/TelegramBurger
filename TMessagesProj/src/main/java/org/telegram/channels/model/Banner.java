package org.telegram.channels.model;

import android.graphics.Bitmap;

import org.telegram.infra.Constant;

import java.util.Date;

public class Banner {
    private long id;
    private String title;
    private Bitmap imageBitmap;
    private byte[] imageBytes;
    private String imageUrl;
    private Date createDate;
    private Constant.BANNER_LINK_TYPE type= Constant.BANNER_LINK_TYPE.PUBLICCHANNEL;
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

    public Constant.BANNER_LINK_TYPE getType() {
        return type;
    }

    public void setType(Constant.BANNER_LINK_TYPE type) {
        this.type = type;
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
