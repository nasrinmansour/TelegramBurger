/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.ui.Adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.telegram.infra.Utility;
import org.telegram.groups.dao.GroupCategoryDAO;
import org.telegram.groups.model.GroupCategory;

import java.util.ArrayList;
import java.util.List;

import ir.javan.messenger.R;

public class GroupCategoriesAdapter extends ArrayAdapter<GroupCategory> {

    Activity activity;
    private List<Integer> positions = new ArrayList<Integer>();

    public GroupCategoriesAdapter(Activity activity, List<GroupCategory> list) {
        super(activity, 0, list);
        this.activity = activity;
    }

    public class Holder {
        public ImageView icon;
        public TextView title;
        public ProgressBar bar;
        ImageView newLable;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder h;
        final GroupCategory p = getItem(position);
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item_category, null);
            h = new Holder();
            h.icon = (ImageView) convertView.findViewById(R.id.list_item_store_package_icon);
            h.title = (TextView) convertView.findViewById(R.id.list_item_store_package_title);
            h.bar = (ProgressBar) convertView.findViewById(R.id.list_item_store_package_progress_bar);
            h.newLable = (ImageView) convertView.findViewById(R.id.list_item_store_package_new_label);
            convertView.setTag(h);

        } else {
            h = (Holder) convertView.getTag();
        }
        h.icon.setTag(p);
        h.title.setTag(p);
        h.bar.setTag(p);

//        if (!p.isReaded()) {
//            h.newLable.setVisibility(View.VISIBLE);
//        } else {
        h.newLable.setVisibility(View.INVISIBLE);
//        }

        if (p.isImageDirty() && Utility.isInternetAvailable(activity)) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (p.getId() == ((GroupCategory) h.icon.getTag()).getId()) {
                        h.icon.setImageResource(R.drawable.channel_image_default);
                        h.icon.setVisibility(View.INVISIBLE);
                    }
                }
            });

            if (!positions.contains(position)) {
                positions.add(position);
                Thread t = new Thread() {
                    GroupCategory p;

                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (p.getId() == ((GroupCategory) h.bar.getTag()).getId()) {
                                    h.bar.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                        byte[] imageByte = Utility.getByteArrayFromURL(p.getImageUrl(), true);
                        if (imageByte != null) {
                            final Bitmap b = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                            p.setImage(imageByte);
                            p.setImageDirty(false);
                            GroupCategoryDAO.update(activity, p, true);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (p.getId() == ((GroupCategory) h.icon.getTag()).getId()) {
                                        h.icon.setImageBitmap(b);
                                    }
                                }
                            });
                        } else {

                        }

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (p.getId() == ((GroupCategory) h.bar.getTag()).getId()) {
                                    h.bar.setVisibility(View.INVISIBLE);
                                    h.icon.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                    }

                    ;

                    public Thread setGroupCategory(GroupCategory p) {
                        this.p = p;
                        return this;
                    }

                }.setGroupCategory(p);
                t.start();
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (p.getId() == ((GroupCategory) h.icon.getTag()).getId()) {
                            h.icon.setVisibility(View.VISIBLE);
                            h.bar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }

        } else {
            Bitmap b = BitmapFactory.decodeByteArray(p.getImage(), 0, p.getImage().length);
            h.icon.setImageBitmap(b);
            h.icon.setVisibility(View.VISIBLE);
            h.bar.setVisibility(View.INVISIBLE);
        }
        h.title.setText(p.getTitle());
        return convertView;
    }

    public void reloadList(List<GroupCategory> list) {
        clear();
//        addAll(list);
        for (GroupCategory c : list) {
            add(c);
        }
        notifyDataSetChanged();

    }

    public void removeGroupCategory(GroupCategory p) {
        remove(p);
        notifyDataSetChanged();
    }

}

