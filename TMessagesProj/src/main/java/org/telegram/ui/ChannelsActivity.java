package org.telegram.ui;

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
import android.widget.TextView;

import org.telegram.ads.analytics.GoogleAnalyticsHelper;
import org.telegram.channels.dao.CategoryDAO;
import org.telegram.channels.dao.ChannelDAO;
import org.telegram.channels.model.Category;
import org.telegram.channels.model.Channel;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.infra.Constant;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.ChannelsAdapter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LetterSectionsListView;

import ir.javan.messenger.R;

/**
 * Created by Morteza on 2016/03/22.
 */
public class ChannelsActivity extends BaseFragment {

    private Category category=null;
    private long catId;
    private ChannelsAdapter listViewAdapter;
    private ListView listView;
    private TextView emptyTextView;
    private ActionBarMenuItem addChannel;

    public ChannelsActivity(Bundle args) {
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
        GoogleAnalyticsHelper.sendScreenView(context, Constant.Analytic.CATEGORY_SCREEN_VIEW_NAME+catId);
        category=CategoryDAO.get(context,catId,false);
        fragmentView = new FrameLayout(context);
        actionBar.setBackgroundColor(AndroidUtilities.getThemeColor());
//        actionBar.setItemsBackground(R.drawable.bar_selector_white);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("channels_of", R.string.channels_of) + " " + category.getTitle());
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
        emptyTextView.setText(LocaleController.getString("NoChannel", R.string.NoChannel));
        emptyTextLayout.addView(emptyTextView);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) emptyTextView.getLayoutParams();
        layoutParams1.width = LayoutHelper.MATCH_PARENT;
        layoutParams1.height = LayoutHelper.MATCH_PARENT;
        layoutParams1.weight = 0.5f;
        emptyTextView.setLayoutParams(layoutParams1);

        listViewAdapter = new ChannelsAdapter(getParentActivity(), ChannelDAO.getCategoryChannels(context, category.getId(), true));
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
        listView.setLayoutParams(layoutParams);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(listViewAdapter.getItem(position).getLink()));
                Channel channel = listViewAdapter.getItem(position);
                GoogleAnalyticsHelper.sendEventAction(context, Constant.Analytic.CHANNEL_CLICK_EVENT_NAME+channel.getId());
                if (channel.getType().equals(Constant.CHANNEL_TYPE.PUBLICCHANNEL)) {
                    ((LaunchActivity) getParentActivity()).runLinkRequest(channel.getLink(), null, null, null, null, null, false, null, 0);
                } else {
                    ((LaunchActivity) getParentActivity()).runLinkRequest(null,channel.getLink(), null, null, null, null, false, null, 0);
                }
//                loadChat(channel.getLink());
            }
        });
        return fragmentView;
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
                    if (MessagesController.checkCanOpenChat(args, ChannelsActivity.this)) {
                        presentFragment(new ChatActivity(args), false);
                    }
                }
            }
        });
    }
}
