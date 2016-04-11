package org.telegram.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import org.telegram.infra.Utility;
import org.telegram.messenger.LocaleController;
import org.telegram.payment_util.IabBroadcastReceiver;
import org.telegram.payment_util.IabHelper;
import org.telegram.payment_util.IabResult;
import org.telegram.payment_util.Inventory;
import org.telegram.payment_util.Purchase;
import org.telegram.payment_util.SkuDetails;

import java.util.ArrayList;

import ir.javan.messenger.R;

/**
 * Created by Morteza on 2016/04/07.
 */
public abstract class BaseStoreActivity extends Activity implements IabBroadcastReceiver.IabBroadcastListener {
    public class Markets{
        public final static int BAZAAR=1;
        public final static int IRANAPPS=2;
    }

    private static final int MARKET=Markets.IRANAPPS;

    private static final int BUY_REQUEST_CODE = 33098;
    private static String bazaarBase64EncodedPublicKey = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDA/roAIz0taxghZ5ipebP/8z7ayRrPRsmow0k3J17s4gZnBzpfo7nw+jc7M5cEQOY4h8LPiJL8QiWi/qD00UHSNzgxAz/EUsVKlx1ZntPFrQmNoAAY6pSbftq7gF9T9bzJBzNf6wv9H2dYEfhHGEiM0Zh2+DpOc5O326+UwoZ83vt6yUvyvIJ9yEMelBu6s6zvmC8ioxeppPQNP4gJ7VK62n/SmGIvUVnWXHxzba0CAwEAAQ==";
    private static String iranAppsBase64EncodedPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDuid8hvxrfejnzInyG381Gz1v44Zjqi3dzvhWBFBmAGsScvfk9nxDuj+HG4+BSTJltMD80anUQscOgBHDcIMHLskykkuogO2tWC5W1zVfuOWM3f5+uN2VpzPA2EhziFRFc+f//0URsngkys1MoFMc2KdokFR9onDSCex5YXyHhuwIDAQAB";
    private static String base64EncodedPublicKey;
    protected ArrayList<String> skuList = null;

    private IabHelper mHelper;

    private IabBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_TMessages);
    }

    protected void initialStore() {
        initSkuList();
        if(MARKET==Markets.BAZAAR) {
            base64EncodedPublicKey = bazaarBase64EncodedPublicKey;
            mHelper = new IabHelper(this, base64EncodedPublicKey) {
                @Override
                public String getMarketPackageName() {
                    return "com.farsitel.bazaar";
                }

                @Override
                public String getServiceURI() {
                    return "ir.cafebazaar.pardakht.InAppBillingService.BIND";
                }
            };
        }else if(MARKET==Markets.IRANAPPS){
            base64EncodedPublicKey = iranAppsBase64EncodedPublicKey;
            mHelper = new IabHelper(this, base64EncodedPublicKey) {
                @Override
                public String getMarketPackageName() {
                    return "ir.tgbs.android.iranapp";
                }

                @Override
                public String getServiceURI() {
                    return "ir.tgbs.iranapps.billing.InAppBillingService.BIND";
                }
            };
        }

        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    return;
                }

                if (mHelper == null) return;

                mBroadcastReceiver = new IabBroadcastReceiver(BaseStoreActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                try {
                    if (Utility.isInternetAvailable(BaseStoreActivity.this)) {
                        mHelper.queryInventoryAsync(true, skuList, mGotInventoryListener);
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    protected void startBuy(String productKey) {
        mHelper.launchPurchaseFlow(this, productKey, BUY_REQUEST_CODE,
                mPurchaseFinishedListener, null);
    }
    protected void startConsume(Purchase purchase) {
        if (purchase != null ) {
            mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                initDefaultData();
                return;
            }

            for(int i=0;i<skuList.size();i++) {
                SkuDetails skuDetail = inventory.getSkuDetails(skuList.get(i));
                if (skuDetail != null) {
                    if (MARKET == Markets.BAZAAR) {
                        initData(skuDetail.getSku(),skuDetail.getTitle(),skuDetail.getPrice());
                    } else if (MARKET == Markets.IRANAPPS) {
                        initData(skuDetail.getSku(),skuDetail.getTitle(),skuDetail.getPrice() + " " + LocaleController.getString("toman", R.string.toman));
                    }
                }
            }
            if(getInProgressProductSku()!=null) {
                Purchase premiumPurchase = inventory.getPurchase(getInProgressProductSku());
                if (premiumPurchase != null) {
                    buySucceed(premiumPurchase.getSku(),premiumPurchase);
                }
                ;
            }
//            Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
//            if (gasPurchase != null ) {
//                mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
//                return;
//            }
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                buyFailed(getInProgressProductSku());
                return;
            } else if (purchase.getSku().equals(getInProgressProductSku())) {
                buySucceed(getInProgressProductSku(),purchase);
//                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (mHelper == null) return;
            if (result.isSuccess()) {
                consumeSucceed(purchase.getSku());
            } else {
                buyFailed(purchase.getSku());
            }
        }
    };

    @Override
    public void receivedBroadcast() {
        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    protected abstract void consumeSucceed(String sku);
    protected abstract void initData(String sku,String title,String premiumPrice);
    protected abstract void initDefaultData();
    protected abstract void buyFailed(String productKey);
    protected abstract void buySucceed(String productKey,Purchase purchase);
    protected abstract void initSkuList();
    protected abstract String getInProgressProductSku();
}
