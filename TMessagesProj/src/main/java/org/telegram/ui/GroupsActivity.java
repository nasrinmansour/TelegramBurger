package org.telegram.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.ads.analytics.GoogleAnalyticsHelper;
import org.telegram.groups.dao.GroupCategoryDAO;
import org.telegram.groups.dao.GroupDAO;
import org.telegram.groups.model.GroupCategory;
import org.telegram.groups.model.Group;
import org.telegram.infra.Utility;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.infra.Constant;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SepehrSettingsHelper;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.GroupsAdapter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LetterSectionsListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ir.javan.messenger.R;

/**
 * Created by Morteza on 2016/03/22.
 */
public class GroupsActivity extends BaseFragment {

    private GroupCategory category=null;
    private long catId;
    private GroupsAdapter listViewAdapter;
    private ListView listView;
    private TextView emptyTextView;

    public GroupsActivity(Bundle args) {
        super(args);
        catId=args.getLong(Constant.CATEGORY_ID_KEY);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        return true;
    }

    @Override
    public View createView(final Context context) {
        GoogleAnalyticsHelper.sendScreenView(context, Constant.Analytic.CATEGORY_SCREEN_VIEW_NAME + catId);
        Utility.updateTimeFromServer(context);
        category=GroupCategoryDAO.get(context,catId,false);
        fragmentView = new FrameLayout(context);
        actionBar.setBackgroundColor(AndroidUtilities.getThemeColor());
//        actionBar.setItemsBackground(R.drawable.bar_selector_white);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("groups_of", R.string.groups_of) + " " + category.getTitle());
        ActionBarMenu menu = actionBar.createMenu();
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ((FrameLayout) fragmentView).addView(createChatroomChargeContainer(context), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout emptyTextLayout = new LinearLayout(context);
        emptyTextLayout.setVisibility(View.INVISIBLE);
        emptyTextLayout.setOrientation(LinearLayout.VERTICAL);
        ((FrameLayout) fragmentView).addView(emptyTextLayout);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) emptyTextLayout.getLayoutParams();
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
        emptyTextView.setText(LocaleController.getString("NoGroup", R.string.NoGroup));
        emptyTextLayout.addView(emptyTextView);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) emptyTextView.getLayoutParams();
        layoutParams1.width = LayoutHelper.MATCH_PARENT;
        layoutParams1.height = LayoutHelper.MATCH_PARENT;
        layoutParams1.weight = 0.5f;
        emptyTextView.setLayoutParams(layoutParams1);

        listViewAdapter = new GroupsAdapter(getParentActivity(), GroupDAO.getCategoryGroups(context, category.getId(), true));
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
        layoutParams.setMargins(0,AndroidUtilities.dp(50),0,0);
        listView.setLayoutParams(layoutParams);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(Utility.canUseChatrooms(getParentActivity())) {
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(listViewAdapter.getItem(position).getLink()));
                    Group group = listViewAdapter.getItem(position);
                    GoogleAnalyticsHelper.sendEventAction(context, Constant.Analytic.GROUP_CLICK_EVENT_NAME + group.getId());
                    if (group.getType().equals(org.telegram.infra.Constant.GROUP_TYPE.PUBLICGROUP)) {
                        ((LaunchActivity) getParentActivity()).runLinkRequest(group.getLink(), null, null, null, null, null, false, null, 0);
                    } else {
                        ((LaunchActivity) getParentActivity()).runLinkRequest(null, group.getLink(), null, null, null, null, false, null, 0);
                    }
//                loadChat(group.getLink());
                }else{
                    if(getParentActivity()!=null) {
                        new AlertDialog.Builder(getParentActivity())
                                .setTitle(R.string.chatroom_expired_title)
                                .setMessage(R.string.chatroom_expired_text)
                                .setPositiveButton(R.string.goto_store, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getParentActivity().startActivity(new Intent(getParentActivity(),ChatroomStoreActivity.class));
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
            }
        });
        return fragmentView;
    }

    private View createChatroomChargeContainer(Context context) {
        RelativeLayout chatroomChargeContainer = new RelativeLayout(context);
        RelativeLayout.LayoutParams lp = LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        chatroomChargeContainer.setLayoutParams(lp);
        chatroomChargeContainer.setPadding(AndroidUtilities.dp(5), AndroidUtilities.dp(5), AndroidUtilities.dp(5), AndroidUtilities.dp(5));
        chatroomChargeContainer.setBackgroundColor(0x11000000);

        String remainingStr="0";
        long remainTime = SepehrSettingsHelper.getChatroomExpireDate(context).getTime() - Utility.getServerTime().getTime();
        if(remainTime>0){
            int month = (int) Math.floor(remainTime / (AlarmManager.INTERVAL_DAY * 30));
            remainTime=  (remainTime-month*(AlarmManager.INTERVAL_DAY * 30));
            int day = (int) Math.floor(remainTime / AlarmManager.INTERVAL_DAY );
            remainTime=  (remainTime-day*AlarmManager.INTERVAL_DAY);
            int hour = (int) Math.floor(remainTime / AlarmManager.INTERVAL_HOUR);
            remainTime= (remainTime-hour*AlarmManager.INTERVAL_HOUR);
            int min = (int) Math.floor(remainTime / 60000);
            remainingStr=""+(month>0?month+" "+LocaleController.getString("month",R.string.month)+" ":"")
                           +(day>0?day+" "+LocaleController.getString("day",R.string.day)+" ":"")
                           +(hour>0?hour+" "+LocaleController.getString("hour",R.string.hour)+" ":"");
//                           +(min>0?min+" "+LocaleController.getString("min",R.string.min):"");
        }
        TextView durationLabelTV = new TextView(context);
        durationLabelTV.setText(LocaleController.getString("chatroom_duration",R.string.chatroom_duration)+"  "+remainingStr);
        durationLabelTV.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));
        durationLabelTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        lp = LayoutHelper.createRelative(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 0, AndroidUtilities.dp(10), 0);
        durationLabelTV.setLayoutParams(lp);
        chatroomChargeContainer.addView(durationLabelTV);

        TextView buyDuration = new TextView(context);
        buyDuration.setText(LocaleController.getString("chatroom_buy_duration", R.string.chatroom_buy_duration));
        buyDuration.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.getFont1Path()));
        buyDuration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        buyDuration.setTextColor(0xFF0000EE);
        lp = LayoutHelper.createRelative(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.setMargins(0,0, AndroidUtilities.dp(12), 0);
        buyDuration.setLayoutParams(lp);
        chatroomChargeContainer.addView(buyDuration);

        buyDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentActivity().startActivity(new Intent(getParentActivity(),ChatroomStoreActivity.class));
            }
        });

        return chatroomChargeContainer;
    }

    private void loadChat(final String username) {
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = username;
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                if (error == null) {
                    final TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                    Bundle args = new Bundle();
                    args.putInt("user_id", res.chats.get(0).id);
                    if (MessagesController.checkCanOpenChat(args, GroupsActivity.this)) {
                        presentFragment(new ChatActivity(args), false);
                    }
                }
            }
        });
    }
}
