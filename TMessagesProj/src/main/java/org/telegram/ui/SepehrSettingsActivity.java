/*
 * This is the source code of Telegram for Android v. 3.x.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.ColorPickerView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.color_picker.ColorPickerDialog;

import ir.javan.messenger.R;

public class SepehrSettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListAdapter listAdapter;
    private ListView listView;
    private TextView titleTextView;
    private EditText passwordEditText;
//    private TextView dropDown;
//    private ActionBarMenuItem dropDownContainer;

    private int type;
    private int passcodeSetStep = 0;
    private String firstPassword;

    private int changePasscodeRow;
    private int passcodeDetailRow;
    private int fingerprintRow;
    private int themeColorRow;

    private int rowCount;

    private final static int done_button = 1;
    private final static int pin_item = 2;
    private final static int password_item = 3;

    public final static int TYPE_SHOW_SETTINGS =0;
    public final static int TYPE_SET_NEW_PASS =1;
    public final static int TYPE_GET_CURRENT_AND_SET_NEW =2;
//    public final static int TYPE_3=3;


    private static final int VIEW_TYPE_CHECKBOX = 0;
    private static final int VIEW_TYPE_SIMPLE_BUTTON = 1;
    private static final int VIEW_TYPE_DESCRIPTION = 2;
    private static final int VIEW_TYPE_COLOR = 3;

    public SepehrSettingsActivity(int type) {
        super();
        this.type = type;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        if (type == TYPE_SHOW_SETTINGS) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (type == TYPE_SHOW_SETTINGS) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
        }
    }

    @Override
    public View createView(final Context context) {
//        if (type != TYPE_3) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//        }
        actionBar.setAllowOverlayTitle(false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    if (passcodeSetStep == 0) {
                        processNext();
                    } else if (passcodeSetStep == 1) {
                        processDone();
                    }
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        if (type != TYPE_SHOW_SETTINGS) {
            ActionBarMenu menu = actionBar.createMenu();
            menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

            titleTextView = new TextView(context);
            titleTextView.setTextColor(0xff757575);
            if (type == TYPE_SET_NEW_PASS) {
                if (UserConfig.sepehrPasscodeHash.length() != 0) {
                    titleTextView.setText(LocaleController.getString("EnterNewPasscode", R.string.EnterNewPasscode));
                } else {
                    titleTextView.setText(LocaleController.getString("EnterNewFirstPasscode", R.string.EnterNewFirstPasscode));
                }
            } else {
                titleTextView.setText(LocaleController.getString("EnterCurrentPasscode", R.string.EnterCurrentPasscode));
            }
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            frameLayout.addView(titleTextView);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) titleTextView.getLayoutParams();
            layoutParams.width = LayoutHelper.WRAP_CONTENT;
            layoutParams.height = LayoutHelper.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            layoutParams.topMargin = AndroidUtilities.dp(38);
            titleTextView.setLayoutParams(layoutParams);

            passwordEditText = new EditText(context);
            passwordEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            passwordEditText.setTextColor(0xff000000);
            passwordEditText.setMaxLines(1);
            passwordEditText.setLines(1);
            passwordEditText.setGravity(Gravity.CENTER_HORIZONTAL);
            passwordEditText.setSingleLine(true);
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(4);
            passwordEditText.setFilters(filterArray);
            passwordEditText.setInputType(InputType.TYPE_CLASS_PHONE);
            passwordEditText.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
            if (type == TYPE_SET_NEW_PASS) {
                passcodeSetStep = 0;
                passwordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            } else {
                passcodeSetStep = 1;
                passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setTypeface(Typeface.DEFAULT);
            AndroidUtilities.clearCursorDrawable(passwordEditText);
            frameLayout.addView(passwordEditText);
            layoutParams = (FrameLayout.LayoutParams) passwordEditText.getLayoutParams();
            layoutParams.topMargin = AndroidUtilities.dp(90);
            layoutParams.height = AndroidUtilities.dp(36);
            layoutParams.leftMargin = AndroidUtilities.dp(40);
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            layoutParams.rightMargin = AndroidUtilities.dp(40);
            layoutParams.width = LayoutHelper.MATCH_PARENT;
            passwordEditText.setLayoutParams(layoutParams);
            passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (passcodeSetStep == 0) {
                        processNext();
                        return true;
                    } else if (passcodeSetStep == 1) {
                        processDone();
                        return true;
                    }
                    return false;
                }
            });
            passwordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (passwordEditText.length() == 4) {
                        if (type == TYPE_GET_CURRENT_AND_SET_NEW /*&& UserConfig.passcodeType == 0*/) {
                            processDone();
                        } else if (type == TYPE_SET_NEW_PASS /*&& currentPasswordType == 0*/) {
                            if (passcodeSetStep == 0) {
                                processNext();
                            } else if (passcodeSetStep == 1) {
                                processDone();
                            }
                        }
                    }
                }
            });
            if (Build.VERSION.SDK_INT < 11) {
                passwordEditText.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        menu.clear();
                    }
                });
            } else {
                passwordEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    public void onDestroyActionMode(ActionMode mode) {
                    }

                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }
                });
            }

            if (type == TYPE_SET_NEW_PASS) {
                actionBar.setTitle(LocaleController.getString("Passcode", R.string.Passcode));
            } else {
                actionBar.setTitle(LocaleController.getString("Passcode", R.string.Passcode));
            }

//            updateDropDownTextView();
        } else {
            actionBar.setTitle(LocaleController.getString("SepehrSetting", R.string.sepehr_settings));
            frameLayout.setBackgroundColor(0xfff0f0f0);
            listView = new ListView(context);
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setVerticalScrollBarEnabled(false);
            listView.setDrawSelectorOnTop(true);
            frameLayout.addView(listView);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.width = LayoutHelper.MATCH_PARENT;
            layoutParams.height = LayoutHelper.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP;
            listView.setLayoutParams(layoutParams);
            listView.setAdapter(listAdapter = new ListAdapter(context));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, final View view, final int i, long l) {
                    if (i == changePasscodeRow) {
                        presentFragment(new SepehrSettingsActivity(TYPE_GET_CURRENT_AND_SET_NEW));
                    }  else if (i == fingerprintRow) {
                        UserConfig.useFingerprintForSepehr = !UserConfig.useFingerprintForSepehr;
                        UserConfig.saveConfig(false);
                        ((TextCheckCell) view).setChecked(UserConfig.useFingerprintForSepehr);
                    } else if (i==themeColorRow){
                        ColorPickerDialog colorPicker = new ColorPickerDialog(context,AndroidUtilities.getThemeColor());
                        colorPicker.setTitle(LocaleController.getString("SepehrThemeColor", R.string.theme_color));
                        colorPicker.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int color) {
                                final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                TextColorCell textCell = (TextColorCell) view;
                                AndroidUtilities.setThemeColor(color);
                                textCell.setTextAndColor(LocaleController.getString("SepehrThemeColor", R.string.theme_color), color, true);
                                editor.commit();

                                Intent intent = new Intent(context, LaunchActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                            }
                        });
                        colorPicker.show();
                    }
                }
            });
        }

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        if (type != TYPE_SHOW_SETTINGS) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (passwordEditText != null) {
                        passwordEditText.requestFocus();
                        AndroidUtilities.showKeyboard(passwordEditText);
                    }
                }
            }, 200);
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.didSetPasscode) {
            if (type == TYPE_SHOW_SETTINGS) {
                updateRows();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void updateRows() {
        rowCount = 0;
        changePasscodeRow = rowCount++;
        passcodeDetailRow = rowCount++;
        themeColorRow = rowCount++;
        fingerprintRow = -1;
        if (UserConfig.sepehrPasscodeHash.length() > 0) {
//            try {
//                if (Build.VERSION.SDK_INT >= 23) {
//                    FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(ApplicationLoader.applicationContext);
//                    if (fingerprintManager.isHardwareDetected()) {
//                        fingerprintRow = rowCount++;
//                    }
//                }
//            } catch (Throwable e) {
//                FileLog.e("tmessages", e);
//            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (listView != null) {
            ViewTreeObserver obs = listView.getViewTreeObserver();
            obs.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    listView.getViewTreeObserver().removeOnPreDrawListener(this);
//                    fixLayoutInternal();
                    return true;
                }
            });
        }
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen && type != TYPE_SHOW_SETTINGS) {
            AndroidUtilities.showKeyboard(passwordEditText);
        }
    }

/*    private void updateDropDownTextView() {
        if (dropDown != null) {
            if (currentPasswordType == 0) {
                dropDown.setText(LocaleController.getString("PasscodePIN", R.string.PasscodePIN));
            } else if (currentPasswordType == 1) {
                dropDown.setText(LocaleController.getString("PasscodePassword", R.string.PasscodePassword));
            }
        }
        if (type == 1 && currentPasswordType == 0 || type == 2 && UserConfig.passcodeType == 0) {
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(4);
            passwordEditText.setFilters(filterArray);
            passwordEditText.setInputType(InputType.TYPE_CLASS_PHONE);
            passwordEditText.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
        } else if (type == 1 && currentPasswordType == 1 || type == 2 && UserConfig.passcodeType == 1) {
            passwordEditText.setFilters(new InputFilter[0]);
            passwordEditText.setKeyListener(null);
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }*/

    private void processNext() {
        if (passwordEditText.getText().length() == 0 || /*currentPasswordType == 0 &&*/ passwordEditText.getText().length() != 4) {
            onPasscodeError();
            return;
        }
/*        if (currentPasswordType == 0) {
            actionBar.setTitle(LocaleController.getString("PasscodePIN", R.string.PasscodePIN));
        } else {
            actionBar.setTitle(LocaleController.getString("PasscodePassword", R.string.PasscodePassword));
        }*/
//        dropDownContainer.setVisibility(View.GONE);
        titleTextView.setText(LocaleController.getString("ReEnterYourPasscode", R.string.ReEnterYourPasscode));
        firstPassword = passwordEditText.getText().toString();
        passwordEditText.setText("");
        passcodeSetStep = 1;
    }

    private void processDone() {
        if (passwordEditText.getText().length() == 0) {
            onPasscodeError();
            return;
        }
        if (type == TYPE_SET_NEW_PASS) {
            if (!firstPassword.equals(passwordEditText.getText().toString())) {
                try {
                    Toast.makeText(getParentActivity(), LocaleController.getString("PasscodeDoNotMatch", R.string.PasscodeDoNotMatch), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
                AndroidUtilities.shakeView(titleTextView, 2, 0);
                passwordEditText.setText("");
                return;
            }

            try {
                UserConfig.sepehrPasscodeSalt = new byte[16];
                Utilities.random.nextBytes(UserConfig.sepehrPasscodeSalt);
                byte[] passcodeBytes = firstPassword.getBytes("UTF-8");
                byte[] bytes = new byte[32 + passcodeBytes.length];
                System.arraycopy(UserConfig.sepehrPasscodeSalt, 0, bytes, 0, 16);
                System.arraycopy(passcodeBytes, 0, bytes, 16, passcodeBytes.length);
                System.arraycopy(UserConfig.sepehrPasscodeSalt, 0, bytes, passcodeBytes.length + 16, 16);
                UserConfig.sepehrPasscodeHash = Utilities.bytesToHex(Utilities.computeSHA256(bytes, 0, bytes.length));
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }

//            UserConfig.passcodeType = currentPasswordType;
            UserConfig.saveConfig(false);
            finishFragment();
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.didSetPasscode);
            passwordEditText.clearFocus();
            AndroidUtilities.hideKeyboard(passwordEditText);
        } else if (type == TYPE_GET_CURRENT_AND_SET_NEW) {
            if (!UserConfig.checkSepehrPasscode(passwordEditText.getText().toString())) {
                passwordEditText.setText("");
                onPasscodeError();
                return;
            }
            passwordEditText.clearFocus();
            AndroidUtilities.hideKeyboard(passwordEditText);
            presentFragment(new SepehrSettingsActivity(TYPE_SET_NEW_PASS), true);
        }
    }

    private void onPasscodeError() {
        if (getParentActivity() == null) {
            return;
        }
        Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(200);
        }
        AndroidUtilities.shakeView(titleTextView, 2, 0);
    }

/*    private void fixLayoutInternal() {
        if (dropDownContainer != null) {
            if (!AndroidUtilities.isTablet()) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) dropDownContainer.getLayoutParams();
                layoutParams.topMargin = (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
                dropDownContainer.setLayoutParams(layoutParams);
            }
            if (!AndroidUtilities.isTablet() && ApplicationLoader.applicationContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                dropDown.setTextSize(18);
            } else {
                dropDown.setTextSize(20);
            }
        }
    }*/

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return i == fingerprintRow || i == changePasscodeRow || i==themeColorRow;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int viewType = getItemViewType(i);
            if (viewType == 0) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                TextCheckCell textCell = (TextCheckCell) view;

                /*if (i == passcodeRow) {
                    textCell.setTextAndCheck(LocaleController.getString("Passcode", R.string.Passcode), UserConfig.passcodeHash.length() > 0, true);
                } else*/ if (i == fingerprintRow) {
                    textCell.setTextAndCheck(LocaleController.getString("UnlockFingerprint", R.string.UnlockFingerprint), UserConfig.useFingerprintForSepehr, true);
                }
            } else if (viewType == VIEW_TYPE_SIMPLE_BUTTON) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == changePasscodeRow) {
                    textCell.setText(LocaleController.getString("ChangeChatLock", R.string.ChangeChatLock), false);
                    textCell.setTextColor(0xff000000);
                }
            } else if (viewType == VIEW_TYPE_DESCRIPTION) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(mContext);
                }
                if (i == passcodeDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("ChangeSepehrPasscodeInfo", R.string.ChangeSepehrPasscodeInfo));
/*                    if (autoLockDetailRow != -1) {
                        view.setBackgroundResource(R.drawable.greydivider);
                    } else {*/
                        view.setBackgroundResource(R.drawable.greydivider_bottom);
//                    }
                }/* else if (i == autoLockDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("AutoLockInfo", R.string.AutoLockInfo));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                }*/
            } else if (viewType == VIEW_TYPE_COLOR) {
                if (view == null) {
                    view = new TextColorCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                TextColorCell textCell = (TextColorCell) view;
                textCell.setTextAndColor(LocaleController.getString("SepehrThemeColor", R.string.theme_color), AndroidUtilities.getThemeColor(), true);
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == fingerprintRow) {
                return VIEW_TYPE_CHECKBOX;
            } else if (i == changePasscodeRow) {
                return VIEW_TYPE_SIMPLE_BUTTON;
            } else if (i == passcodeDetailRow) {
                return VIEW_TYPE_DESCRIPTION;
            } else if (i == themeColorRow) {
                return VIEW_TYPE_COLOR;
            }
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
