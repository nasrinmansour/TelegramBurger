package org.telegram.ui;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.telegram.infra.SharedPrefrenHelper;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.SepehrSettingsHelper;
import org.telegram.payment_util.Purchase;
import org.telegram.ui.ActionBar.ActionBar;

import java.util.ArrayList;

import ir.javan.messenger.R;

/**
 * Created by Morteza on 2016/04/07.
 */
public class ChatroomStoreActivity extends  BaseStoreActivity {
    private final static String PRODUCT_1_SKU="coins1000";
    private final static String PRODUCT_2_SKU="coins2000";
    private final static String PRODUCT_3_SKU="coins3000";

    private ViewGroup rootView;
    private ActionBar actionBar;
    private String inProgressProductSku;

    private RelativeLayout product1ButtonRelative;
    private TextView product1ButtonLabel;
    private TextView product1ButtonPrice;
    private Button product1Button;

    private RelativeLayout product2ButtonRelative;
    private TextView product2ButtonLabel;
    private TextView product2ButtonPrice;
    private Button product2Button;

    private RelativeLayout product3ButtonRelative;
    private TextView product3ButtonLabel;
    private TextView product3ButtonPrice;
    private Button product3Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ApplicationLoader.postInitApplication();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.Theme_TMessages);
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom_store);

        rootView=(ViewGroup)findViewById(R.id.store_root);
        actionBar = new ActionBar(this);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setBackgroundColor(0xff54759e);
        actionBar.setItemsBackground(R.drawable.bar_selector);
        actionBar.setTitle(LocaleController.getString("EditAndForward", R.string.EditAndForward));
//        actionBar.setOccupyStatusBar(false);
        rootView.addView(actionBar,0);
        ViewGroup.LayoutParams layoutParams = actionBar.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        actionBar.setLayoutParams(layoutParams);
        initComponents();
        initComponentsBehaviour();
        initialStore();
    }

    @Override
    protected void consumeSucceed(String sku) {
        long currentDuration = SepehrSettingsHelper.getChatroomValidUseDuration();
        if(sku.equals(PRODUCT_1_SKU)){
            SepehrSettingsHelper.saveChatroomValidUseDuration(currentDuration+ AlarmManager.INTERVAL_DAY*30);
        }else if(sku.equals(PRODUCT_2_SKU)){
            SepehrSettingsHelper.saveChatroomValidUseDuration(currentDuration+ AlarmManager.INTERVAL_DAY*30*2);
        }else if(sku.equals(PRODUCT_3_SKU)){
            SepehrSettingsHelper.saveChatroomValidUseDuration(currentDuration+ AlarmManager.INTERVAL_DAY*30*5);
        }
        AlertDialog builder=new AlertDialog.Builder(this)
                .setTitle(R.string.payment_succeed)
                .setMessage(R.string.payment_succeed_message)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ChatroomStoreActivity.this.finish();
                    }
                })
                .show();
    }

    private void initComponentsBehaviour() {
        product1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    buy(PRODUCT_1_SKU);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        product2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    buy(PRODUCT_2_SKU);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        product3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    buy(PRODUCT_3_SKU);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finish();
                }
            }
        });
    }

    private void buy(String productSku) {
        inProgressProductSku=productSku;
        startBuy(productSku);
    }

    private void initComponents() {
        product1ButtonRelative=(RelativeLayout)findViewById(R.id.store_product1);
        product1ButtonLabel = ((TextView) findViewById(R.id.store_price_label1));
        product1ButtonPrice = ((TextView) findViewById(R.id.store_price_tv1));
        product1Button = ((Button) findViewById(R.id.store_buy_button1));

        product2ButtonRelative=(RelativeLayout)findViewById(R.id.store_product2);
        product2ButtonLabel = ((TextView) findViewById(R.id.store_price_label2));
        product2ButtonPrice = ((TextView) findViewById(R.id.store_price_tv2));
        product2Button = ((Button) findViewById(R.id.store_buy_button2));

        product3ButtonRelative=(RelativeLayout)findViewById(R.id.store_product3);
        product3ButtonLabel = ((TextView) findViewById(R.id.store_price_label3));
        product3ButtonPrice = ((TextView) findViewById(R.id.store_price_tv3));
        product3Button = ((Button) findViewById(R.id.store_buy_button3));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            product1ButtonRelative.setBackground(getBuyButtonBackgroundDrawable());
            product2ButtonRelative.setBackground(getBuyButtonBackgroundDrawable());
            product3ButtonRelative.setBackground(getBuyButtonBackgroundDrawable());
        }else{
            product1ButtonRelative.setBackgroundDrawable(getBuyButtonBackgroundDrawable());
            product2ButtonRelative.setBackgroundDrawable(getBuyButtonBackgroundDrawable());
            product3ButtonRelative.setBackgroundDrawable(getBuyButtonBackgroundDrawable());
        }

        product1ButtonLabel.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));
        product1ButtonPrice.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));

        product2ButtonLabel.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));
        product2ButtonPrice.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));

        product3ButtonLabel.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));
        product3ButtonPrice.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));

        ((TextView)findViewById(R.id.t1)).setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));

        product1ButtonRelative.setVisibility(View.INVISIBLE);
        product2ButtonRelative.setVisibility(View.INVISIBLE);
        product3ButtonRelative.setVisibility(View.INVISIBLE);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Drawable getBuyButtonBackgroundDrawable() {
        Drawable mDrawable = getResources().getDrawable(R.drawable.regbtn_states);
        mDrawable.setColorFilter(new PorterDuffColorFilter(AndroidUtilities.getThemeColor(), PorterDuff.Mode.MULTIPLY));
        return mDrawable;
    }
    @Override
    protected void initData(String sku,String title,String premiumPrice) {
        if(sku.equals(PRODUCT_1_SKU)){
            product1ButtonLabel.setText(title);
            product1ButtonPrice.setText(premiumPrice);
            product1ButtonRelative.setVisibility(View.VISIBLE);
        }else if(sku.equals(PRODUCT_2_SKU)){
            product2ButtonLabel.setText(title);
            product2ButtonPrice.setText(premiumPrice);
            product2ButtonRelative.setVisibility(View.VISIBLE);
        }else if(sku.equals(PRODUCT_3_SKU)){
            product3ButtonLabel.setText(title);
            product3ButtonPrice.setText(premiumPrice);
            product3ButtonRelative.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initDefaultData() {
        product1ButtonPrice.setText("");
        product1ButtonRelative.setVisibility(View.VISIBLE);
        product2ButtonPrice.setText("");
        product2ButtonRelative.setVisibility(View.VISIBLE);
        product3ButtonPrice.setText("");
        product3ButtonRelative.setVisibility(View.VISIBLE);
    }

    @Override
    protected void buyFailed(String productKey) {
        AlertDialog builder=new AlertDialog.Builder(this)
                .setTitle(R.string.payment_failed)
                .setMessage(R.string.payment_failed_message)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    protected void buySucceed(String sku,Purchase purchase) {
        startConsume(purchase);
    }

    protected void initSkuList() {
        skuList = new ArrayList<String>();
        skuList.add(PRODUCT_1_SKU);
        skuList.add(PRODUCT_2_SKU);
        skuList.add(PRODUCT_3_SKU);
    }

    @Override
    protected String getInProgressProductSku() {
        return inProgressProductSku;
    }
}
