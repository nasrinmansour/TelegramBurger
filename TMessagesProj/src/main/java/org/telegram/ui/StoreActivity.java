package org.telegram.ui;

import android.annotation.TargetApi;
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
import android.widget.TextView;

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
public class StoreActivity extends BaseStoreActivity {
    private TextView priceTextView;
    private Button buyButton;
    private ViewGroup rootView;
    private ActionBar actionBar;
    private TextView priceLabelTextView;
    protected static final String SKU_PREMIUM = "burgram_premium";
    private String inProgressProductSku=SKU_PREMIUM;

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
        setContentView(R.layout.activity_store);

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

    }

    private void initComponentsBehaviour() {
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    buy();
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

    private void buy() {
        startBuy(SKU_PREMIUM);
    }

    private void initComponents() {
        priceTextView=(TextView)findViewById(R.id.store_price_tv);
        priceLabelTextView = ((TextView) findViewById(R.id.t2));
        buyButton=(Button)findViewById(R.id.store_buy_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            buyButton.setBackground(getBuyButtonBackgroundDrawable());
        }else{
            buyButton.setBackgroundDrawable(getBuyButtonBackgroundDrawable());
        }
        priceTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));
        buyButton.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));
        ((TextView)findViewById(R.id.t1)).setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));
        priceLabelTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));
        priceLabelTextView.setVisibility(View.INVISIBLE);
        priceTextView.setVisibility(View.INVISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Drawable getBuyButtonBackgroundDrawable() {
        Drawable mDrawable = getResources().getDrawable(R.drawable.regbtn_states);
        mDrawable.setColorFilter(new PorterDuffColorFilter(AndroidUtilities.getThemeColor(), PorterDuff.Mode.MULTIPLY));
        return mDrawable;
    }
    @Override
    protected void initData(String sku,String title,String premiumPrice) {
        priceTextView.setText(premiumPrice);
        priceLabelTextView.setVisibility(View.VISIBLE);
        priceTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initDefaultData() {

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
    protected void buySucceed(String productKey,Purchase purchase) {
        SepehrSettingsHelper.saveIsPremiumSetting(true);
        AlertDialog builder=new AlertDialog.Builder(this)
                .setTitle(R.string.payment_succeed)
                .setMessage(R.string.payment_succeed_message)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        StoreActivity.this.finish();
                    }
                })
                .show();
    }

    protected void initSkuList() {
        skuList = new ArrayList<String>();
        skuList.add(SKU_PREMIUM);
    }

    @Override
    protected String getInProgressProductSku() {
        return inProgressProductSku;
    }
}
