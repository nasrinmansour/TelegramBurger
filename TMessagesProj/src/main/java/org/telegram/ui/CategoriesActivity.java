package org.telegram.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.telegram.ads.analytics.GoogleAnalyticsHelper;
import org.telegram.infra.Utility;
import org.telegram.channels.dao.BannerDAO;
import org.telegram.channels.dao.CategoryDAO;
import org.telegram.channels.model.Banner;
import org.telegram.channels.model.Category;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.infra.Constant;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.CategoriesAdapter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LetterSectionsListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ir.javan.messenger.R;

/**
 * Created by Morteza on 2016/03/22.
 */
public class CategoriesActivity extends BaseFragment {

    private static final float BANNER_HEIGHT = 180;
    private static final long SLIDER_TIMER_INTERVAL = 5000;
    private CategoriesAdapter listViewAdapter;
    private ListView listView;
    private TextView emptyTextView;
    private ViewPager bannerViewPager;
    private boolean isSliderTouching;
    private Timer sliderTimer;
    private List<Banner> banners;
    private ActionBarMenuItem addChannel;

    public CategoriesActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        return true;
    }

    public void setIsSliderTouching(boolean b){
        isSliderTouching=b;
        if(b){
            stopSliderTimer();
        }else {
            startSliderTimer();
        }
    }
    @Override
    public View createView(Context context) {
        GoogleAnalyticsHelper.sendScreenView(context, Constant.Analytic.CAFE_CHANNEL_SCREEN_VIEW_NAME);
        swipeBackEnabled =false;
        actionBar.setBackgroundColor(AndroidUtilities.getThemeColor());
//        actionBar.setItemsBackground(R.drawable.bar_selector_white);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("Categories", R.string.Categories));
        ActionBarMenu menu = actionBar.createMenu();
        addChannel = menu.addItem(1, R.drawable.add_channel);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }else if (id==1){
                    AlertDialog.Builder builder=new AlertDialog.Builder(getParentActivity());
                    builder
                            .setTitle(R.string.add_channel_subject)
                            .setMessage(R.string.add_channel_dialog_message)
                            .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("message/rfc822");
                                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{getParentActivity().getString(R.string.add_channel_email)});
                                    i.putExtra(Intent.EXTRA_SUBJECT, getParentActivity().getString(R.string.add_channel_subject));
                                    i.putExtra(Intent.EXTRA_TEXT, "");
                                    try {
                                        getParentActivity().startActivity(Intent.createChooser(i, getParentActivity().getString(R.string.add_channel_activity_title)));
                                    } catch (android.content.ActivityNotFoundException ex) {
                                    }
                                }
                            })
                            .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        bannerViewPager = new ViewPager(context);

        ArrayList<ImageView> imageViews=new ArrayList<ImageView>();
        banners= BannerDAO.getAllBanners(context,true);
        for(int i=0;i<banners.size();i++) {
            ImageView i1 = new ImageView(context);
            i1.setScaleType(ImageView.ScaleType.FIT_CENTER);
            i1.setTag(banners.get(i));
            imageViews.add(i1);
        }
        bannerViewPager.setAdapter(new CustomPagerAdapter(getParentActivity(), imageViews));
        bannerViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setIsSliderTouching(false);
                } else {
                    setIsSliderTouching(true);
                }
                return false;
            }
        });
//        ((FrameLayout) fragmentView).addView(bannerViewPager);

        FrameLayout.LayoutParams layoutParams =LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,AndroidUtilities.dp(BANNER_HEIGHT));//(FrameLayout.LayoutParams) bannerViewPager.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = AndroidUtilities.dp(BANNER_HEIGHT);
        layoutParams.setMargins(0, AndroidUtilities.dp(1), 0, 0);
        bannerViewPager.setLayoutParams(layoutParams);

        LinearLayout emptyTextLayout = new LinearLayout(context);
        emptyTextLayout.setVisibility(View.INVISIBLE);
        emptyTextLayout.setOrientation(LinearLayout.VERTICAL);
        ((FrameLayout) fragmentView).addView(emptyTextLayout);
        layoutParams = (FrameLayout.LayoutParams) emptyTextLayout.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        layoutParams.gravity = Gravity.TOP;
        emptyTextLayout.setLayoutParams(layoutParams);
        emptyTextLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        emptyTextView = new TextView(context);
        emptyTextView.setTextColor(0xff808080);
        emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyTextView.setGravity(Gravity.CENTER);
        emptyTextView.setText(LocaleController.getString("NoCategories", R.string.NoCategories));
        emptyTextLayout.addView(emptyTextView);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) emptyTextView.getLayoutParams();
        layoutParams1.width = LayoutHelper.MATCH_PARENT;
        layoutParams1.height = LayoutHelper.MATCH_PARENT;
        layoutParams1.weight = 0.5f;
        emptyTextView.setLayoutParams(layoutParams1);

        List<Category> categories = CategoryDAO.getAllCategory(context, true);
        categories.add(0,new Category());
        listViewAdapter = new CategoriesAdapter(getParentActivity(), categories,bannerViewPager);
        listView = new LetterSectionsListView(context);
        listView.setEmptyView(emptyTextLayout);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setFastScrollEnabled(true);
        listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        listView.setAdapter(listViewAdapter);
        if (Build.VERSION.SDK_INT >= 11) {
            listView.setFastScrollAlwaysVisible(true);
            listView.setVerticalScrollbarPosition(LocaleController.isRTL ? ListView.SCROLLBAR_POSITION_LEFT : ListView.SCROLLBAR_POSITION_RIGHT);
        }
        ((FrameLayout) fragmentView).addView(listView);
        layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        layoutParams.setMargins(0,AndroidUtilities.dp(1),0,0);
        listView.setLayoutParams(layoutParams);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();
                args.putLong(Constant.CATEGORY_ID_KEY, id);
                presentFragment(new ChannelsActivity(args));
            }
        });

        startSliderTimer();
        return fragmentView;
    }

    private void startSliderTimer() {
        sliderTimer=new Timer();
        sliderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getParentActivity() == null) {
                    return;
                }
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bannerViewPager.setCurrentItem((bannerViewPager.getCurrentItem() + 1) % bannerViewPager.getAdapter().getCount(), true);
                    }
                });
            }
        }, SLIDER_TIMER_INTERVAL, SLIDER_TIMER_INTERVAL);
    }

    private void stopSliderTimer() {
        if(sliderTimer!=null){
            sliderTimer.cancel();
            sliderTimer=null;
        }
    }


    private static class CustomPagerAdapter extends PagerAdapter {

        private final ArrayList<ImageView> imageViews;
        private Activity activity;

        public CustomPagerAdapter(Activity activity, ArrayList<ImageView> listViews) {
            this.activity = activity;
            this.imageViews =listViews;
        }


        @Override
        public Object instantiateItem(ViewGroup layout, final int position) {
//           LayoutInflater inflater = LayoutInflater.from(mContext);
            RelativeLayout relativeLayout = new RelativeLayout(activity);
            final ImageView imageView = imageViews.get(position);
            if (imageView.getParent() != null) {
                ((ViewGroup) imageView.getParent()).removeView(imageView);
            }
            relativeLayout.addView(imageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            Banner banner = (Banner) imageView.getTag();
            if (banner.isImageDirty() && Utility.isInternetAvailable(activity)) {
                        imageView.setVisibility(View.INVISIBLE);
                Thread t = new Thread() {
                    Banner banner;

                    public void run() {

                        byte[] imageByte = Utility.getByteArrayFromURL(banner.getImageUrl(), true);
                        if (imageByte != null) {
                            final Bitmap b = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                            banner.setImage(imageByte);
                            banner.setImageDirty(false);
                            BannerDAO.updateBanner(activity, banner, true);
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                             imageView.setImageBitmap(b);
                                    }
                                });
                        } else {

                        }

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setVisibility(View.VISIBLE);
                            }
                        });

                    }

                    ;

                    public Thread setBanner(Banner b) {
                        this.banner = b;
                        return this;
                    }

                }.setBanner(banner);
                t.start();
            } else {
                Bitmap b = BitmapFactory.decodeByteArray(banner.getImageBytes(), 0, banner.getImageBytes().length);
                imageView.setImageBitmap(b);
                imageView.setVisibility(View.VISIBLE);
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Banner banner = (Banner) v.getTag();
                    GoogleAnalyticsHelper.sendEventAction(activity, Constant.Analytic.BANNER_CLICK_EVENT_NAME+banner.getId());
                    if (banner.getType().equals(Constant.BANNER_LINK_TYPE.PUBLICCHANNEL)) {
                        ((LaunchActivity) activity).runLinkRequest(banner.getLink(), null, null, null, null, null, false, null, 0);
                    } else if (banner.getType().equals(Constant.BANNER_LINK_TYPE.PRIVATECHANNEL)) {
                        ((LaunchActivity) activity).runLinkRequest(null, banner.getLink(), null, null, null, null, false, null, 0);
                    } else if (banner.getType().equals(Constant.BANNER_LINK_TYPE.URL)) {
                        String url=banner.getLink();
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "http://" + url;
                        }
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        activity.startActivity(browserIntent);
                    }
                }
            });
            layout.addView(relativeLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            return relativeLayout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

    }
}
