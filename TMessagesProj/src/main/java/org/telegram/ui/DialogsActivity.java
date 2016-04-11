/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ogaclejapan.arclayout.Arc;
import com.ogaclejapan.arclayout.ArcLayout;

import org.telegram.ads.analytics.GoogleAnalyticsHelper;
import org.telegram.infra.SharedPrefrenHelper;
import org.telegram.infra.Utility;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.infra.Constant;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Lock;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SepehrSettingsHelper;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.TLRPC;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import ir.javan.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.messenger.Favourite;
import org.telegram.ui.Components.PlayerView;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    
    private RecyclerListView listView;
    private ArrayList<RecyclerListView> listViews;
    private int[] listViewsDialogTypes={Constant.DIALOG_TYPE_BOTS, Constant.DIALOG_TYPE_CHANNELS, Constant.DIALOG_TYPE_MEGA_GROUPS, Constant.DIALOG_TYPE_GROUPS, Constant.DIALOG_TYPE_USERS, Constant.DIALOG_TYPE_FAVS, Constant.DIALOG_TYPE_DIALOG};
    private LinearLayoutManager layoutManager;
    private DialogsAdapter dialogsAdapter;
    private DialogsSearchAdapter dialogsSearchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private ProgressBar progressView;
    private LinearLayout emptyView;
    private ActionBarMenuItem passcodeItem;
    private ImageView floatingButton;
    private static boolean isFloatingArcMenuOpen=false;

    private AlertDialog permissionDialog;

    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private boolean checkPermission = true;

    private String selectAlertString;
    private String selectAlertStringGroup;
    private String addToGroupAlertString;
    private int dialogsType;

    private static boolean dialogsLoaded;
    private boolean searching;
    private boolean searchWas;
    private boolean onlySelect;
    private long selectedDialog;
    private String searchString;
    private long openedDialogId;

    private MessagesActivityDelegate delegate;
    private FrameLayout tabsView;
    private int tabsHeight;
    private int selectedTab=0;
    private ImageButton allTab;
    private ImageButton channelsTab;
    private ImageButton favsTab;
    private ImageButton groupsTab;
    private ImageButton superGroupsTab;
    private ImageButton usersTab;
    private ImageButton botsTab;
    private LinearLayout tabsLayout;
    private ArrayList<ImageButton> tabsImageButtonList;
    private boolean hideTabs;
    private ViewPager tabsViewPager;
    private boolean fakeScroll=false;
    private TLRPC.Chat currentChat;

    private ImageView floatingChannelsButton;
    private ImageView floatingGroupsButton;
    private ArcLayout floatingButtonsArcContainer;
    private ImageView floatingMenuButton;

    public interface MessagesActivityDelegate {
        void didSelectDialog(DialogsActivity fragment, long dialog_id, boolean param);
    }

    public DialogsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        if (getArguments() != null) {
            onlySelect = arguments.getBoolean("onlySelect", false);
            dialogsType = arguments.getInt("dialogsType", 0);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
            addToGroupAlertString = arguments.getString("addToGroupAlertString");
        }

        if (searchString == null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didLoadedReplyMessages);
        }


        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 100, true);
            ContactsController.getInstance().checkInviteText();
            dialogsLoaded = true;
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (searchString == null) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didLoadedReplyMessages);
        }
        delegate = null;
    }

    @Override
    public View createView(final Context context) {
        searching = false;
        searchWas = false;

        ResourceLoader.loadRecources(context);

        ActionBarMenu menu = actionBar.createMenu();
        if (!onlySelect && searchString == null) {
            passcodeItem = menu.addItem(1, R.drawable.lock_close);
            updatePasscodeButton();
        }
        boolean isGhostModeActive = SepehrSettingsHelper.getGhostModeActivationState();
        final ActionBarMenuItem ghostButtonItem = menu.addItem(4, isGhostModeActive?R.drawable.ic_ghost:R.drawable.ic_ghost_disable);
        final ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                searching = true;
                if (listView != null) {
                    if (searchString != null) {
                        listView.setEmptyView(searchEmptyView);
                        progressView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.GONE);
                    }
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.GONE);
                    }
                }
                updatePasscodeButton();
            }

            @Override
            public boolean canCollapseSearch() {
                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                return true;
            }

            @Override
            public void onSearchCollapse() {
                searching = false;
                searchWas = false;
                if (listView != null) {
                    searchEmptyView.setVisibility(View.GONE);
                    if (MessagesController.getInstance().loadingDialogs && dialogsAdapter.getDialogsArray().isEmpty()) {
                        emptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        listView.setEmptyView(emptyView);
                    }
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.VISIBLE);
                        floatingHidden = true;
                        ViewProxy.setTranslationY(floatingButton, AndroidUtilities.dp(100));
                        hideFloatingButton(false);
                    }
                    if (listView.getAdapter() != dialogsAdapter) {
                        listView.setAdapter(dialogsAdapter);
                        dialogsAdapter.notifyDataSetChanged();
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(null);
                }
                updatePasscodeButton();
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (text.length() != 0 || dialogsSearchAdapter != null && dialogsSearchAdapter.hasRecentRearch()) {
                    searchWas = true;
                    if (dialogsSearchAdapter != null && listView.getAdapter() != dialogsSearchAdapter) {
                        listView.setAdapter(dialogsSearchAdapter);
                        dialogsSearchAdapter.notifyDataSetChanged();
                    }
                    if (searchEmptyView != null && listView.getEmptyView() != searchEmptyView) {
                        emptyView.setVisibility(View.GONE);
                        progressView.setVisibility(View.GONE);
                        searchEmptyView.showTextView();
                        listView.setEmptyView(searchEmptyView);
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(text);
                }
            }
        });
        item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        if (onlySelect) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
        } else {
            if (searchString != null) {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            } else {
                actionBar.setBackButtonDrawable(new MenuDrawable());
            }
            if (BuildVars.DEBUG_VERSION) {
                actionBar.setTitle(LocaleController.getString("AppNameBeta", R.string.AppNameBeta));
            } else {
                actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
            }
        }
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onlySelect) {
                        finishFragment();
                    } else if (parentLayout != null) {
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == 1) {
                    UserConfig.appLocked = !UserConfig.appLocked;
                    UserConfig.saveConfig(false);
                    updatePasscodeButton();
                }else if (id==4){
                    if(!SepehrSettingsHelper.getIsPremiumSetting()){
                        Utility.showBuyPremiumDialog(getParentActivity());
                    }
                    if(!SharedPrefrenHelper.getIsGhostGuidShowed(getParentActivity())){
                        Utility.showGuidDialog(getParentActivity(),
                                                LocaleController.getString("ghostMode",R.string.ghostMode),
                                                LocaleController.getString("ghostModeGuid",R.string.ghostModeGuid));
                        SharedPrefrenHelper.putIsGhostGuidShowed(getParentActivity(), true);
                    }
                    if(!SepehrSettingsHelper.getIsPremiumSetting()){
                        return;
                    }
                    boolean isGhostModeActive = !SepehrSettingsHelper.getGhostModeActivationState();
                    GoogleAnalyticsHelper.sendEventAction(context, Constant.Analytic.GHOST_MODE_CLICK_EVENT_NAME+isGhostModeActive);
                    SepehrSettingsHelper.saveGhostModeActivationState(isGhostModeActive);
                    ghostButtonItem.setIcon(isGhostModeActive?R.drawable.ic_ghost:R.drawable.ic_ghost_disable);
                }
            }
        });


        FrameLayout frameLayout = new FrameLayout(context);
        fragmentView = frameLayout;
        
        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setItemAnimator(null);
        listView.setInstantClick(true);
        listView.setLayoutAnimation(null);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        if (Build.VERSION.SDK_INT >= 11) {
            listView.setVerticalScrollbarPosition(LocaleController.isRTL ? ListView.SCROLLBAR_POSITION_LEFT : ListView.SCROLLBAR_POSITION_RIGHT);
        }
//        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listView == null || listView.getAdapter() == null) {
                    return;
                }
                long dialog_id = 0;
                int message_id = 0;
                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter == dialogsAdapter) {
                    TLRPC.Dialog dialog = dialogsAdapter.getItem(position);
                    if (dialog == null) {
                        return;
                    }
                    dialog_id = dialog.id;
                } else if (adapter == dialogsSearchAdapter) {
                    Object obj = dialogsSearchAdapter.getItem(position);
                    if (obj instanceof TLRPC.User) {
                        dialog_id = ((TLRPC.User) obj).id;
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add((TLRPC.User) obj);
                            MessagesController.getInstance().putUsers(users, false);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.User) obj);
                        }
                    } else if (obj instanceof TLRPC.Chat) {
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.Chat> chats = new ArrayList<>();
                            chats.add((TLRPC.Chat) obj);
                            MessagesController.getInstance().putChats(chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                        }
                        if (((TLRPC.Chat) obj).id > 0) {
                            dialog_id = -((TLRPC.Chat) obj).id;
                        } else {
                            dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.Chat) obj);
                        }
                    } else if (obj instanceof TLRPC.EncryptedChat) {
                        dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.EncryptedChat) obj);
                        }
                    } else if (obj instanceof MessageObject) {
                        MessageObject messageObject = (MessageObject) obj;
                        dialog_id = messageObject.getDialogId();
                        message_id = messageObject.getId();
                        dialogsSearchAdapter.addHashtagsFromMessage(dialogsSearchAdapter.getLastSearchString());
                    } else if (obj instanceof String) {
                        actionBar.openSearchField((String) obj);
                    }
                }

                if (dialog_id == 0) {
                    return;
                }

                if (onlySelect) {
                    didSelectResult(dialog_id, true, false);
                } else {
                    Bundle args = new Bundle();
                    int lower_part = (int) dialog_id;
                    int high_id = (int) (dialog_id >> 32);
                    if (lower_part != 0) {
                        if (high_id == 1) {
                            args.putInt("chat_id", lower_part);
                        } else {
                            if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                if (message_id != 0) {
                                    TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                                    if (chat != null && chat.migrated_to != null) {
                                        args.putInt("migrated_to", lower_part);
                                        lower_part = -chat.migrated_to.channel_id;
                                    }
                                }
                                args.putInt("chat_id", -lower_part);
                            }
                        }
                    } else {
                        args.putInt("enc_id", high_id);
                    }
                    if (message_id != 0) {
                        args.putInt("message_id", message_id);
                    } else {
                        if (actionBar != null) {
                            actionBar.closeSearchField();
                        }
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                            return;
                        }
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(View view, int position) {
                if (onlySelect || searching && searchWas || getParentActivity() == null) {
                    if (searchWas && searching || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                        RecyclerView.Adapter adapter = listView.getAdapter();
                        if (adapter == dialogsSearchAdapter) {
                            Object item = dialogsSearchAdapter.getItem(position);
                            if (item instanceof String || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                                builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                            dialogsSearchAdapter.clearRecentSearch();
                                        } else {
                                            dialogsSearchAdapter.clearRecentHashtags();
                                        }
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder.create());
                                return true;
                            }
                        }
                    }
                    return false;
                }
                TLRPC.Dialog dialog;
                ArrayList<TLRPC.Dialog> dialogs = getDialogsArray();
                if (position < 0 || position >= dialogs.size()) {
                    return false;
                }
                dialog = dialogs.get(position);
                selectedDialog = dialog.id;

                BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                int lower_id = (int) selectedDialog;
                int high_id = (int) (selectedDialog >> 32);
                final TLRPC.Dialog finalDialog = dialog;
                final boolean isInFavourites = Favourite.isFavourite(Long.valueOf(finalDialog.id));
                String favouritesItem = isInFavourites ? LocaleController.getString("DeleteFromFavorites", R.string.delete_from_favourites) : LocaleController.getString("AddToFavorites", R.string.add_to_favourites);

                final boolean isChatLocked = Lock.isLock(Long.valueOf(finalDialog.id));
                String lockItem = isChatLocked ? LocaleController.getString("RemoveLock", R.string.remove_lock) : LocaleController.getString("AddLock", R.string.add_lock);
//                final boolean[] isLockSelected = {false};
                if (dialog instanceof TLRPC.TL_dialogChannel) {
                    final TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                    CharSequence items[];
                    if (chat != null && chat.megagroup) {
                        items = new CharSequence[]{LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache), chat == null || !chat.creator ? LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu) : LocaleController.getString("DeleteMegaMenu", R.string.DeleteMegaMenu),favouritesItem,lockItem};
                    } else {
                        items = new CharSequence[]{LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache), chat == null || !chat.creator ? LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu) : LocaleController.getString("ChannelDeleteMenu", R.string.ChannelDeleteMenu),favouritesItem,lockItem};
                    }
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            if (which == 0) {
                                if (chat != null && chat.megagroup) {
                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistorySuper", R.string.AreYouSureClearHistorySuper));
                                } else {
                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", R.string.AreYouSureClearHistoryChannel));
                                }
                                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MessagesController.getInstance().deleteDialog(selectedDialog, 2);
                                    }
                                });
                            } else if(which==2){
                                if(isInFavourites){
                                    Favourite.deleteFavourite(getParentActivity(),Long.valueOf(selectedDialog));
                                    MessagesController.getInstance().dialogsFavs.remove(finalDialog);
                                }else{
                                    Favourite.addFavourite(getParentActivity(),Long.valueOf(selectedDialog));
                                    MessagesController.getInstance().dialogsFavs.add(finalDialog);
                                }
                                dialogsAdapter.notifyDataSetChanged();
                                return;
                            }else if(which== 3 ){
                                if(!SepehrSettingsHelper.getIsPremiumSetting()){
                                    Utility.showBuyPremiumDialog(getParentActivity());
                                }else {
                                    lockUnlockSelected(selectedDialog);
                                }
                                return;
                            }else {
                                if (chat != null && chat.megagroup) {
                                    if (!chat.creator) {
                                        builder.setMessage(LocaleController.getString("MegaLeaveAlert", R.string.MegaLeaveAlert));
                                    } else {
                                        builder.setMessage(LocaleController.getString("MegaDeleteAlert", R.string.MegaDeleteAlert));
                                    }
                                } else {
                                    if (chat == null || !chat.creator) {
                                        builder.setMessage(LocaleController.getString("ChannelLeaveAlert", R.string.ChannelLeaveAlert));
                                    } else {
                                        builder.setMessage(LocaleController.getString("ChannelDeleteAlert", R.string.ChannelDeleteAlert));
                                    }
                                }
                                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, UserConfig.getCurrentUser(), null);
                                        if (AndroidUtilities.isTablet()) {
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                        }
                                    }
                                });
                            }
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showDialog(builder.create());
                        }
                    });
                    showDialog(builder.create());
                } else {
                    final boolean isChat = lower_id < 0 && high_id != 1;
                    TLRPC.User user = null;
                    if (!isChat && lower_id > 0 && high_id != 1) {
                        user = MessagesController.getInstance().getUser(lower_id);
                    }
                    final boolean isBot = user != null && user.bot;
                    builder.setItems(new CharSequence[]{LocaleController.getString("ClearHistory", R.string.ClearHistory),
                            isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) :
                                    isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete),favouritesItem,lockItem}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            if (which == 0) {
                                builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                            }else if(which== 2 ){
                                if(isInFavourites){
                                    Favourite.deleteFavourite(getParentActivity(),Long.valueOf(selectedDialog));
                                    MessagesController.getInstance().dialogsFavs.remove(finalDialog);
                                }else{
                                    Favourite.addFavourite(getParentActivity(),Long.valueOf(selectedDialog));
                                    MessagesController.getInstance().dialogsFavs.add(finalDialog);
                                }
                                dialogsAdapter.notifyDataSetChanged();
                                return;
                            }else if(which== 3 ){
                                lockUnlockSelected(selectedDialog);
                                return;
                            }else {
                                if (isChat) {
                                    builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
                                } else {
                                    builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
                                }
                            }
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (which != 0) {
                                        if (isChat) {
                                            TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
                                            if (currentChat != null && ChatObject.isNotInChat(currentChat)) {
                                                MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                            } else {
                                                MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, MessagesController.getInstance().getUser(UserConfig.getClientUserId()), null);
                                            }
                                        } else {
                                            MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                        }
                                        if (isBot) {
                                            MessagesController.getInstance().blockUser((int) selectedDialog);
                                        }
                                        if (AndroidUtilities.isTablet()) {
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                        }
                                    } else {
                                        MessagesController.getInstance().deleteDialog(selectedDialog, 1);
                                    }
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showDialog(builder.create());
                        }
                    });
                    showDialog(builder.create());
                }
                return true;
            }
        });

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setVisibility(View.GONE);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        frameLayout.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new LinearLayout(context);
        emptyView.setOrientation(LinearLayout.VERTICAL);
        emptyView.setVisibility(View.GONE);
        emptyView.setGravity(Gravity.CENTER);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        emptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        TextView textView = new TextView(context);
        textView.setText(LocaleController.getString("NoChats", R.string.NoChats));
        textView.setTextColor(0xff959595);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyView.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        String help = LocaleController.getString("NoChatsHelp", R.string.NoChatsHelp);
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace("\n", " ");
        }
        textView.setText(help);
        textView.setTextColor(0xff959595);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(6), AndroidUtilities.dp(8), 0);
        textView.setLineSpacing(AndroidUtilities.dp(2), 1);
        emptyView.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        progressView = new ProgressBar(context);
        progressView.setVisibility(View.GONE);
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        this.tabsView = new FrameLayout(context);
        createTabs(context);

        floatingButton = new ImageView(context);
        floatingButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
//        floatingButton.setBackgroundResource(R.drawable.floating_states);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            floatingButton.setBackground(getFloatingButtonBackgroundDrawable());
        }else{
            floatingButton.setBackgroundDrawable(getFloatingButtonBackgroundDrawable());
        }
        floatingButton.setImageResource(R.drawable.floating_pencil);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFloattingMenu();
                Bundle args = new Bundle();
                args.putBoolean("destroyAfterSelect", true);
                presentFragment(new ContactsActivity(args));
            }
        });

        floatingChannelsButton = new ImageView(context);
        floatingChannelsButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        floatingChannelsButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            floatingChannelsButton.setBackground(getFloatingButtonBackgroundDrawable());
        }else{
            floatingChannelsButton.setBackgroundDrawable(getFloatingButtonBackgroundDrawable());
        }
        floatingChannelsButton.setImageResource(R.drawable.cafe_channel);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingChannelsButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingChannelsButton.setStateListAnimator(animator);
            floatingChannelsButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        floatingChannelsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFloattingMenu();
                Bundle args = new Bundle();
                presentFragment(new CategoriesActivity(args));
            }
        });

        floatingGroupsButton = new ImageView(context);
        floatingGroupsButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        floatingGroupsButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            floatingGroupsButton.setBackground(getFloatingButtonBackgroundDrawable());
        }else{
            floatingGroupsButton.setBackgroundDrawable(getFloatingButtonBackgroundDrawable());
        }
        floatingGroupsButton.setImageResource(R.drawable.groups_button);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingGroupsButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingGroupsButton.setStateListAnimator(animator);
            floatingGroupsButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        floatingGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFloattingMenu();
                Bundle args = new Bundle();
                presentFragment(new GroupCategoriesActivity(args));
            }
        });

        floatingMenuButton = new ImageView(context);
        floatingMenuButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        floatingMenuButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            floatingMenuButton.setBackground(getFloatingButtonBackgroundDrawable());
        }else{
            floatingMenuButton.setBackgroundDrawable(getFloatingButtonBackgroundDrawable());
        }
        floatingMenuButton.setImageResource(R.drawable.arc_menu);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingMenuButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingMenuButton.setStateListAnimator(animator);
            floatingMenuButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        frameLayout.addView(floatingMenuButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, tabsHeight));
        floatingMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFloattingMenu();
            }
        });

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();

                if (searching && searchWas) {
                    if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached()) {
                        dialogsSearchAdapter.loadMoreSearchMessages();
                    }
                    return;
                }
                if (visibleItemCount > 0) {
                    if (layoutManager.findLastVisibleItemPosition() >= getDialogsArray().size() - 10) {
                        MessagesController.getInstance().loadDialogs(-1, 100, !MessagesController.getInstance().dialogsEndReached);
                    }
                }

                if (floatingButton.getVisibility() != View.GONE) {
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated && !fakeScroll) {
                        hideFloatingButton(goingDown);
                    }
                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }
            }
        });

        if (searchString == null) {
            dialogsAdapter = new DialogsAdapter(context, dialogsType);
            if (AndroidUtilities.isTablet() && openedDialogId != 0) {
                dialogsAdapter.setOpenedDialogId(openedDialogId);
            }
            listView.setAdapter(dialogsAdapter);
        }
        int type = 0;
        if (searchString != null) {
            type = 2;
        } else if (!onlySelect) {
            type = 1;
        }
        dialogsSearchAdapter = new DialogsSearchAdapter(context, type, dialogsType);
        dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.MessagesActivitySearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && searchEmptyView != null) {
                    if (search) {
                        searchEmptyView.showProgress();
                    } else {
                        searchEmptyView.showTextView();
                    }
                }
            }
        });

//        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
//            searchEmptyView.setVisibility(View.GONE);
//            emptyView.setVisibility(View.GONE);
//            listView.setEmptyView(progressView);
//        } else {
//            searchEmptyView.setVisibility(View.GONE);
//            progressView.setVisibility(View.GONE);
//            listView.setEmptyView(emptyView);
//        }
        if (searchString != null) {
            actionBar.openSearchField(searchString);
        }

        if (!onlySelect && dialogsType == 0) {
            frameLayout.addView(new PlayerView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
        }
        initialListViews(context, listView);
        createViewPager(context, frameLayout, listViews);
        frameLayout.addView(this.tabsView, LayoutHelper.createFrame(-1, this.tabsHeight, Gravity.BOTTOM, 0.0F, 0.0F, 0.0F, 0.0F));

        initialFloatingButtons(context, frameLayout);
        floatingMenuButton.bringToFront();

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isFloatingArcMenuOpen) {
                        showFloatingArcMenu();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
        return fragmentView;
    }

    private void lockUnlockSelected(final long selectedDialogId) {
        boolean isChatLocked = Lock.isLock(selectedDialogId);
        GoogleAnalyticsHelper.sendEventAction(getParentActivity(), Constant.Analytic.LOCK_EVENT_NAME + isChatLocked);
        if(isChatLocked){
            ((LaunchActivity)getParentActivity()).showPasscodeActivityForChat(new Runnable() {
                @Override
                public void run() {
                    Lock.deleteLock(selectedDialogId);
                }
            });
        }else{
            if(UserConfig.sepehrPasscodeHash.length()>0) {
                Lock.addLock(selectedDialogId);
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("LockChat", R.string.LockChat));
                builder.setMessage(LocaleController.getString("PasswordNotSet", R.string.PasswordNotSet));
                builder.setPositiveButton(LocaleController.getString("Ok", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presentFragment(new SepehrSettingsActivity(SepehrSettingsActivity.TYPE_SET_NEW_PASS));
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                showDialog(builder.create());
            }
        }
        dialogsAdapter.notifyDataSetChanged();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
        if (dialogsSearchAdapter != null) {
            dialogsSearchAdapter.notifyDataSetChanged();
        }
        if (checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else {
                        askForPermissons();
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissons() {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> permissons = new ArrayList<>();
        if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_CONTACTS);
            permissons.add(Manifest.permission.WRITE_CONTACTS);
            permissons.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissons.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] items = permissons.toArray(new String[permissons.size()]);
        activity.requestPermissions(items, 1);
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null) {
            askForPermissons();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!onlySelect && floatingButton != null) {
            floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewProxy.setTranslationY(floatingButton, floatingHidden ? AndroidUtilities.dp(100) : 0);
                    floatingButton.setClickable(!floatingHidden);
                    if (floatingButton != null) {
                        if (Build.VERSION.SDK_INT < 16) {
                            floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults.length <= a || grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    continue;
                }
                switch (permissions[a]) {
                    case Manifest.permission.READ_CONTACTS:
                        ContactsController.getInstance().readContacts();
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        ImageLoader.getInstance().checkMediaPaths();
                        break;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (dialogsAdapter != null) {
                if (dialogsAdapter.isDataSetChanged()) {
                    dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
                }
            }
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
            if (listView != null) {
                try {
                    if (MessagesController.getInstance().loadingDialogs && dialogsAdapter.getDialogsArray().isEmpty()) {
                        searchEmptyView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        if (searching && searchWas) {
                            emptyView.setVisibility(View.GONE);
                            listView.setEmptyView(searchEmptyView);
                        } else {
                            searchEmptyView.setVisibility(View.GONE);
                            listView.setEmptyView(emptyView);
                        }
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e); //TODO fix it in other way?
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.updateInterfaces) {
//            updateVisibleRows((Integer) args[0]);
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded = false;
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.openedChatChanged) {
            if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                boolean close = (Boolean) args[1];
                long dialog_id = (Long) args[0];
                if (close) {
                    if (dialog_id == openedDialogId) {
                        openedDialogId = 0;
                    }
                } else {
                    openedDialogId = dialog_id;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId);
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        } if (id == NotificationCenter.needReloadRecentDialogsSearch) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.loadRecentSearch();
            }
        } else if (id == NotificationCenter.didLoadedReplyMessages) {
            updateVisibleRows(0);
        }
    }

    private ArrayList<TLRPC.Dialog> getDialogsArray() {
        if (this.dialogsType == Constant.DIALOG_TYPE_DIALOG) {
            return MessagesController.getInstance().dialogs;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_SERVER_ONLY) {
            return MessagesController.getInstance().dialogsServerOnly;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_GROUP_ONLY) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_USERS) {
            return MessagesController.getInstance().dialogsUsers;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_GROUPS) {
            return MessagesController.getInstance().dialogsGroups;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_CHANNELS) {
            return MessagesController.getInstance().dialogsChannels;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_BOTS) {
            return MessagesController.getInstance().dialogsBots;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_MEGA_GROUPS) {
            return MessagesController.getInstance().dialogsMegaGroups;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_FAVS) {
            return MessagesController.getInstance().dialogsFavs;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_GROUPS_ALL) {
            return MessagesController.getInstance().dialogsGroupsAll;
        }
        return null;
    }

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (UserConfig.passcodeHash.length() != 0 && !searching) {
            passcodeItem.setVisibility(View.VISIBLE);
            if (UserConfig.appLocked) {
                passcodeItem.setIcon(R.drawable.lock_close);
            } else {
                passcodeItem.setIcon(R.drawable.lock_open);
            }
        } else {
            passcodeItem.setVisibility(View.GONE);
        }
    }

    private void hideFloatingButton(boolean hide) {
        if(hide){
           return;
        }
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        ObjectAnimatorProxy animator = ObjectAnimatorProxy.ofFloatProxy(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(150) : 0).setDuration(300);
        animator.setInterpolator(floatingInterpolator);
        floatingButton.setClickable(!hide);
        animator.start();
        hideFloatingChannelButton(hide);
    }

    private void hideFloatingChannelButton(boolean hide) {
//        if(hide){
//           return;
//        }
//        if (floatingHidden == hide) {
//            return;
//        }
//        floatingHidden = hide;
        ObjectAnimatorProxy animator = ObjectAnimatorProxy.ofFloatProxy(floatingChannelsButton, "translationY", floatingHidden ? AndroidUtilities.dp(150) : 0).setDuration(300);
        animator.setInterpolator(floatingInterpolator);
        floatingChannelsButton.setClickable(!hide);
        animator.start();
    }

    private void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof DialogCell) {
                if (listView.getAdapter() != dialogsSearchAdapter) {
                    DialogCell cell = (DialogCell) child;
                    if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                        cell.checkCurrentDialogIndex();
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else {
                        cell.update(mask);
                    }
                }
            } else if (child instanceof UserCell) {
                ((UserCell) child).update(mask);
            } else if (child instanceof ProfileSearchCell) {
                ((ProfileSearchCell) child).update(mask);
            }
        }
    }

    public void setDelegate(MessagesActivityDelegate delegate) {
        this.delegate = delegate;
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    public boolean isMainDialogList() {
        return delegate == null && searchString == null;
    }

    private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param) {
        if (addToGroupAlertString == null) {
            if ((int) dialog_id < 0 && ChatObject.isChannel(-(int) dialog_id) && !ChatObject.isCanWriteToChannel(-(int) dialog_id)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
                builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                showDialog(builder.create());
                return;
            }
        }
        if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null)) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    TLRPC.Chat chat = MessagesController.getInstance().getChat(lower_part);
                    if (chat == null) {
                        return;
                    }
                    builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                } else {
                    if (lower_part > 0) {
                        TLRPC.User user = MessagesController.getInstance().getUser(lower_part);
                        if (user == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
                    } else if (lower_part < 0) {
                        TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                        if (chat == null) {
                            return;
                        }
                        if (addToGroupAlertString != null) {
                            builder.setMessage(LocaleController.formatStringSimple(addToGroupAlertString, chat.title));
                        } else {
                            builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                        }
                    }
                }
            } else {
                TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
                TLRPC.User user = MessagesController.getInstance().getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
            }

            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    didSelectResult(dialog_id, false, false);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
        } else {
            if (delegate != null) {
                delegate.didSelectDialog(DialogsActivity.this, dialog_id, param);
                delegate = null;
            } else {
                finishFragment();
            }
        }
    }
    // added by sepehr
    private void refreshAdapter(Context context){
        GoogleAnalyticsHelper.sendEventAction(context, Constant.Analytic.TAB_CHANGE_EVENT_NAME +selectedTab);
        dialogsType=listViewsDialogTypes[selectedTab];
        listView=listViews.get(selectedTab);
        layoutManager = (LinearLayoutManager) listView.getLayoutManager();
        refreshAdapterAndTabs(new DialogsAdapter(context, this.dialogsType));
        if (MessagesController.getInstance().loadingDialogs && dialogsAdapter.getDialogsArray().isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setEmptyView(progressView);
        } else {
            searchEmptyView.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);
        }
//        listView.onScrollListener.onScrolled(listView,0,0);
    }

    private void refreshAdapterAndTabs(DialogsAdapter paramDialogsAdapter){
        this.dialogsAdapter = paramDialogsAdapter;
        this.listView.setAdapter(this.dialogsAdapter);
        this.dialogsAdapter.notifyDataSetChanged();
        if (!this.onlySelect) {
            ApplicationLoader.applicationContext.getSharedPreferences(Constant.PLUS_PREFS_KEY, 0).edit().putInt(Constant.SELECTED_TAB, this.selectedTab).apply();
        }
        refreshTabs();
    }
    private void createTabs(Context context) {
        SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Constant.PLUS_PREFS_KEY,0);
        this.tabsHeight = localSharedPreferences.getInt(Constant.TAB_HEIGHT_PREFS_KEY, 40);
        refreshTabAndListViews(false);
        this.tabsLayout = new LinearLayout(context);
        this.tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        this.tabsLayout.setGravity(Gravity.RIGHT);

        tabsImageButtonList=new ArrayList<>();
        View.OnClickListener onclickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTab = tabsImageButtonList.indexOf(v);
                tabsViewPager.setCurrentItem(selectedTab);
            }
        };


        tabsImageButtonList.add(this.botsTab = new ImageButton(context));
        this.botsTab.setImageResource(R.drawable.tab_bot);

        tabsImageButtonList.add(this.channelsTab = new ImageButton(context));
        this.channelsTab.setImageResource(R.drawable.tab_channel);

        tabsImageButtonList.add(this.superGroupsTab = new ImageButton(context));
        this.superGroupsTab.setImageResource(R.drawable.tab_supergroup);

        tabsImageButtonList.add(this.groupsTab = new ImageButton(context));
        this.groupsTab.setImageResource(R.drawable.tab_group);

        tabsImageButtonList.add(this.usersTab = new ImageButton(context));
        this.usersTab.setImageResource(R.drawable.tab_user);

        tabsImageButtonList.add(this.favsTab = new ImageButton(context));
        this.favsTab.setImageResource(R.drawable.tab_favs);

        tabsImageButtonList.add(this.allTab = new ImageButton(context));
        this.allTab.setImageResource(R.drawable.tab_all);


        for(int i=0;i<tabsImageButtonList.size();i++){
            tabsImageButtonList.get(i).setScaleType(ImageView.ScaleType.CENTER);
            tabsImageButtonList.get(i).setOnClickListener(onclickListener);
            this.tabsLayout.addView(tabsImageButtonList.get(i), LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0F));
        }

        this.tabsView.addView(this.tabsLayout, LayoutHelper.createFrame( LayoutHelper.MATCH_PARENT,  LayoutHelper.MATCH_PARENT));

    }

    private void refreshTabs() {
        for (ImageButton ib : tabsImageButtonList) {
            ib.setBackgroundResource(R.drawable.tabs_deactive_back);
        }

        tabsImageButtonList.get(this.selectedTab).setBackgroundResource(R.drawable.tabs_selected_back);
//        actionBar.setTitle((CharSequence) tabsImageButtonList.get(this.selectedTab).getTag());
        actionBar.setTitle(getHeaderAllTitles());

        TextView localTextView;
//      paintHeader(true);
        if ((getDialogsArray() != null) && (getDialogsArray().isEmpty())) {
            this.searchEmptyView.setVisibility(View.GONE);
            this.progressView.setVisibility(View.GONE);
            if (this.emptyView.getChildCount() > 0) {
                localTextView = (TextView) this.emptyView.getChildAt(0);
                if (localTextView != null) {
                    localTextView.setText(getNoChatText());
                    if (this.emptyView.getChildAt(1) != null) {
                        this.emptyView.getChildAt(1).setVisibility(View.GONE);
                    }
                    this.emptyView.setVisibility(View.VISIBLE);
                    this.listView.setEmptyView(this.emptyView);
                }
            }
        }
    }
    private void refreshTabAndListViews(boolean hideTabs)
    {
        if ((this.hideTabs) || (hideTabs)){
            this.tabsView.setVisibility(View.GONE);
            this.listView.setPadding(0, 0, 0, 0);
        }else{
            this.listView.scrollToPosition(0);
//            this.tabsView.setVisibility(View.VISIBLE);
            int i = AndroidUtilities.dp(this.tabsHeight);
            ViewGroup.LayoutParams localLayoutParams = this.tabsView.getLayoutParams();
            if (localLayoutParams != null)
            {
                localLayoutParams.height = i;
                this.tabsView.setLayoutParams(localLayoutParams);
            }
            this.listView.setPadding(0, i, 0, 0);
        }
    }

    private void initialListViews(Context context, RecyclerListView listView) {
        listViews = new ArrayList<>();
        for(int i=0;i<listViewsDialogTypes.length;i++) {
            RecyclerListView l = new RecyclerListView(context);
            l.setVerticalScrollBarEnabled(listView.isVerticalScrollBarEnabled());
            l.setItemAnimator(listView.getItemAnimator());
            l.setInstantClick(listView.instantClick);
            l.setLayoutAnimation(listView.getLayoutAnimation());
            l.setLayoutManager(new LinearLayoutManager(context) {
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            });
            ((LinearLayoutManager)l.getLayoutManager()).setOrientation(LinearLayoutManager.VERTICAL);
            if (Build.VERSION.SDK_INT >= 11) {
                l.setVerticalScrollbarPosition(listView.getVerticalScrollbarPosition());
            }
            l.setOnItemClickListener(listView.onItemClickListener);
            l.setOnItemLongClickListener(listView.onItemLongClickListener);
            l.setOnScrollListener(listView.onScrollListener);

            listViews.add(l);
        }
    }
    private void createViewPager(final Context context, FrameLayout frameLayout, final ArrayList<RecyclerListView> listViews) {
//        ListViewFragment f = new ListViewFragment();
//        f.setListView(listView);
        tabsViewPager = new ViewPager(context);
        tabsViewPager.setAdapter(new CustomPagerAdapter(context, listViews));
        tabsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                selectedTab = position;
                refreshAdapter(context);
            }

        });

        selectedTab = listViewsDialogTypes.length-1;
        tabsViewPager.setCurrentItem(selectedTab);
        refreshAdapter(context);
        frameLayout.addView(tabsViewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.TOP,0,0,0,this.tabsHeight));
    }
    private static class CustomPagerAdapter extends PagerAdapter {

        private final ArrayList<RecyclerListView> listViews;
        private Context mContext;

        public CustomPagerAdapter(Context context, ArrayList<RecyclerListView> listViews) {
            mContext = context;
            this.listViews=listViews;
        }


        @Override
        public Object instantiateItem(ViewGroup layout, int position) {
//           LayoutInflater inflater = LayoutInflater.from(mContext);
            RelativeLayout relativeLayout = new RelativeLayout(mContext);
            if (listViews.get(position).getParent() != null) {
                ((ViewGroup) listViews.get(position).getParent()).removeView(listViews.get(position));
            }
            relativeLayout.addView(listViews.get(position), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            layout.addView(relativeLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            return relativeLayout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return listViews.size();
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
    private String getHeaderAllTitles()
    {
        switch (this.dialogsType)
        {
            default:
                return LocaleController.getString("AppName", R.string.AppName);
            case Constant.DIALOG_TYPE_USERS:
                return LocaleController.getString("Users", R.string.header_users);
            case Constant.DIALOG_TYPE_GROUPS_ALL:
            case Constant.DIALOG_TYPE_GROUPS:
                return LocaleController.getString("Groups", R.string.header_groups);
            case Constant.DIALOG_TYPE_CHANNELS:
                return LocaleController.getString("Channels", R.string.header_channels);
            case Constant.DIALOG_TYPE_BOTS:
                return LocaleController.getString("Bots", R.string.header_bots);
            case Constant.DIALOG_TYPE_MEGA_GROUPS:
                return LocaleController.getString("SuperGroups", R.string.header_mega_groups);
            case Constant.DIALOG_TYPE_FAVS:
                return LocaleController.getString("Favorites", R.string.header_favs);
        }
    }
    private String getNoChatText()
    {
        switch (this.dialogsType)
        {
            default:
                return LocaleController.getString("NoResult", R.string.NoResult);
            case Constant.DIALOG_TYPE_USERS:
                return LocaleController.getString("Users", R.string.header_users);
            case Constant.DIALOG_TYPE_GROUPS_ALL:
            case Constant.DIALOG_TYPE_GROUPS:
                return LocaleController.getString("Groups", R.string.header_groups);
            case Constant.DIALOG_TYPE_CHANNELS:
                return LocaleController.getString("Channels", R.string.header_channels);
            case Constant.DIALOG_TYPE_BOTS:
                return LocaleController.getString("Bots", R.string.header_bots);
            case Constant.DIALOG_TYPE_MEGA_GROUPS:
                return LocaleController.getString("SuperGroups", R.string.header_mega_groups);
            case Constant.DIALOG_TYPE_FAVS:
                return LocaleController.getString("Favorites", R.string.header_favs);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Drawable getFloatingButtonBackgroundDrawable() {
        Drawable mDrawable = getParentActivity().getResources().getDrawable(R.drawable.floating_states);
        mDrawable.setColorFilter(new PorterDuffColorFilter(AndroidUtilities.getThemeColor(), PorterDuff.Mode.MULTIPLY));
        return mDrawable;
    }

    private void initialFloatingButtons(Context context, FrameLayout frameLayout) {
        floatingButtonsArcContainer = new ArcLayout(context);
        floatingButtonsArcContainer.setVisibility(View.INVISIBLE);
        if(LocaleController.isRTL) {
            floatingButtonsArcContainer.setArc(Arc.BOTTOM_LEFT);
        }else{
            floatingButtonsArcContainer.setArc(Arc.BOTTOM_RIGHT);
        }
        floatingButtonsArcContainer.setRadius(AndroidUtilities.dp(250));
        frameLayout.addView(floatingButtonsArcContainer, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, tabsHeight));
        floatingButtonsArcContainer.bringToFront();

        floatingButtonsArcContainer.addView(floatingChannelsButton, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
//        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) floatingChannelsButton.getLayoutParams();
//        lp.setMargins(0, 0, 0, AndroidUtilities.dp(2));
//        floatingChannelsButton.setLayoutParams(lp);

        floatingButtonsArcContainer.addView(floatingButton, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
//        lp = (LinearLayout.LayoutParams) floatingButton.getLayoutParams();
//        lp.setMargins(0, 0, 0, AndroidUtilities.dp(3));
//        floatingButton.setLayoutParams(lp);

        floatingButtonsArcContainer.addView(floatingGroupsButton, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
//        lp = (LinearLayout.LayoutParams) floatingGroupsButton.getLayoutParams();
//        lp.setMargins(0, 0, 0, AndroidUtilities.dp(3));
//        floatingGroupsButton.setLayoutParams(lp);

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isFloatingArcMenuOpen) {
                        hideFloatingArcMenu();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 5000);
    }

    @SuppressWarnings("NewApi")
    private void showFloatingArcMenu() {
//        menuLayout.setVisibility(View.VISIBLE);
        floatingMenuButton.setImageResource(R.drawable.close_arc_menu);
        List<Animator> animList = new ArrayList<>();

        for (int i = 0, len = floatingButtonsArcContainer.getChildCount(); i < len; i++) {
            animList.add(createShowItemAnimator(floatingButtonsArcContainer.getChildAt(i)));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(400);
        animSet.setInterpolator(new OvershootInterpolator());
        animSet.playTogether(animList);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                floatingButtonsArcContainer.setVisibility(View.VISIBLE);
            }
        });
        animSet.start();
        isFloatingArcMenuOpen=true;
    }

    @SuppressWarnings("NewApi")
    private void hideFloatingArcMenu() {

        List<Animator> animList = new ArrayList<>();
        floatingMenuButton.setImageResource(R.drawable.arc_menu);
        for (int i = floatingButtonsArcContainer.getChildCount() - 1; i >= 0; i--) {
            animList.add(createHideItemAnimator(floatingButtonsArcContainer.getChildAt(i)));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(400);
        animSet.setInterpolator(new AnticipateInterpolator());
        animSet.playTogether(animList);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                menuLayout.setVisibility(View.INVISIBLE);
                floatingButtonsArcContainer.setVisibility(View.INVISIBLE);
            }
        });
        animSet.start();
        isFloatingArcMenuOpen=false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Animator createShowItemAnimator(View item) {
        float dx;
        float dy;
        if(LocaleController.isRTL) {
            dx = floatingButtonsArcContainer.getChildAt(0).getX() - item.getX();
            dy = floatingButtonsArcContainer.getChildAt(floatingButtonsArcContainer.getChildCount() - 1).getY() - item.getY();
        }else{
            dx = floatingButtonsArcContainer.getChildAt(floatingButtonsArcContainer.getChildCount() - 1).getX() - item.getX();
            dy = floatingButtonsArcContainer.getChildAt(0).getY() - item.getY();
        }
        item.setRotation(0f);
        item.setTranslationX(dx);
        item.setTranslationY(dy);

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(0f, 720f),
                AnimatorUtils.translationX(dx, 0f),
                AnimatorUtils.translationY(dy, 0f)
        );

        return anim;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Animator createHideItemAnimator(final View item) {
        float dx;
        float dy;
        if(LocaleController.isRTL) {
            dx = floatingButtonsArcContainer.getChildAt(0).getX() - item.getX();
            dy = floatingButtonsArcContainer.getChildAt(floatingButtonsArcContainer.getChildCount() - 1).getY() - item.getY();
        }else{
            dx = floatingButtonsArcContainer.getChildAt(floatingButtonsArcContainer.getChildCount() - 1).getX() - item.getX();
            dy = floatingButtonsArcContainer.getChildAt(0).getY() - item.getY();
        }

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(720f, 0f),
                AnimatorUtils.translationX(0f, dx),
                AnimatorUtils.translationY(0f, dy)
        );

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                item.setTranslationY(0f);
                item.setTranslationX(0f);
            }
        });
        return anim;
    }


    private void toggleFloattingMenu() {
        if (isFloatingArcMenuOpen) {
            hideFloatingArcMenu();
        } else {
            showFloatingArcMenu();
        }
    }
}
