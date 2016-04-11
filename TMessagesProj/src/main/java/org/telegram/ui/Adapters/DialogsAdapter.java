/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.infra.Constant;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.LoadingCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DialogsAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private int dialogsType;
    private long openedDialogId;
    private int currentCount;

    private class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
    }

    public DialogsAdapter(Context context, int type) {
        mContext = context;
        dialogsType = type;
    }

    public void setOpenedDialogId(long id) {
        openedDialogId = id;
    }

    public boolean isDataSetChanged() {
        int current = currentCount;
        return current != getItemCount() || current == 1;
    }

    public ArrayList<TLRPC.Dialog> getDialogsArray() {
//        if (dialogsType == 0) {
//            return MessagesController.getInstance().dialogs;
//        } else if (dialogsType == 1) {
//            return MessagesController.getInstance().dialogsServerOnly;
//        } else if (dialogsType == 2) {
//            return MessagesController.getInstance().dialogsGroupsOnly;
//        }
        SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Constant.PLUS_PREFS_KEY, 0);
        if (this.dialogsType == Constant.DIALOG_TYPE_DIALOG){
            boolean bool = localSharedPreferences.getBoolean(Constant.HIDE_TABS_PREFS, false);
            if ((localSharedPreferences.getInt(Constant.SORT_ALL_PREFS, 0) == 0) || (bool)) {
                sortDefault(MessagesController.getInstance().dialogs);
            }else{
                sortUnread(MessagesController.getInstance().dialogs);
            }
            return MessagesController.getInstance().dialogs;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_SERVER_ONLY) {
            return MessagesController.getInstance().dialogsServerOnly;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_GROUP_ONLY) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_USERS){
            if (localSharedPreferences.getInt(Constant.SORT_USERS_PREFS, 0) == 0) {
                sortUsersDefault();
            }else{
                sortUsersByStatus();
            }
            return MessagesController.getInstance().dialogsUsers;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_GROUPS){
            if (localSharedPreferences.getInt(Constant.SORT_GROUPS_PREFS, 0) == 0) {
                sortDefault(MessagesController.getInstance().dialogsGroups);
            }else{
                sortUnread(MessagesController.getInstance().dialogsGroups);
            }
            return MessagesController.getInstance().dialogsGroups;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_CHANNELS){
            if (localSharedPreferences.getInt(Constant.SORT_CHANNELS_PREFS, 0) == 0) {
                sortDefault(MessagesController.getInstance().dialogsChannels);
            }else{
                sortUnread(MessagesController.getInstance().dialogsChannels);
            }
            return MessagesController.getInstance().dialogsChannels;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_BOTS){
            if (localSharedPreferences.getInt(Constant.SORT_BOTS_PREFS, 0) == 0) {
                sortDefault(MessagesController.getInstance().dialogsBots);
            }else{
                sortUnread(MessagesController.getInstance().dialogsBots);
            }
            return MessagesController.getInstance().dialogsBots;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_MEGA_GROUPS){
            if (localSharedPreferences.getInt(Constant.SORT_MEGAGROUPS_PREFS, 0) == 0) {
                sortDefault(MessagesController.getInstance().dialogsMegaGroups);
            }else{
                sortUnread(MessagesController.getInstance().dialogsMegaGroups);
            }
            return MessagesController.getInstance().dialogsMegaGroups;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_FAVS){
            if (localSharedPreferences.getInt(Constant.SORT_FAVS_PREFS, 0) == 0) {
                sortDefault(MessagesController.getInstance().dialogsFavs);
            }else{
                sortUnread(MessagesController.getInstance().dialogsFavs);
            }
            return MessagesController.getInstance().dialogsFavs;
        }
        if (this.dialogsType == Constant.DIALOG_TYPE_GROUPS_ALL){
            if (localSharedPreferences.getInt(Constant.SORT_GROUPS_PREFS, 0) == 0) {
                sortDefault(MessagesController.getInstance().dialogsGroupsAll);
            }else{
                sortUnread(MessagesController.getInstance().dialogsGroupsAll);
            }
            return MessagesController.getInstance().dialogsGroupsAll;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        int count = getDialogsArray().size();
        if (count == 0 && MessagesController.getInstance().loadingDialogs) {
            return 0;
        }
        if (!MessagesController.getInstance().dialogsEndReached) {
            count++;
        }
        currentCount = count;
        return count;
    }

    public TLRPC.Dialog getItem(int i) {
        ArrayList<TLRPC.Dialog> arrayList = getDialogsArray();
        if (i < 0 || i >= arrayList.size()) {
            return null;
        }
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == 0) {
            view = new DialogCell(mContext);
        } else if (viewType == 1) {
            view = new LoadingCell(mContext);
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder.getItemViewType() == 0) {
            DialogCell cell = (DialogCell) viewHolder.itemView;
            cell.useSeparator = (i != getItemCount() - 1);
            TLRPC.Dialog dialog = getItem(i);
            if (dialogsType == 0) {
                if (AndroidUtilities.isTablet()) {
                    cell.setDialogSelected(dialog.id == openedDialogId);
                }
            }
            cell.setDialog(dialog, i, dialogsType);
        }
    }

    @Override
    public int getItemViewType(int i) {
        if (i == getDialogsArray().size()) {
            return 1;
        }
        return 0;
    }
    // added by sepehr

    private void sortDefault(ArrayList<TLRPC.Dialog> paramArrayList) {
        Collections.sort(paramArrayList, new Comparator<TLRPC.Dialog>() {
            public int compare(TLRPC.Dialog paramAnonymousDialog1, TLRPC.Dialog paramAnonymousDialog2) {
                if (paramAnonymousDialog1.last_message_date == paramAnonymousDialog2.last_message_date) {
                    return 0;
                }
                if (paramAnonymousDialog1.last_message_date < paramAnonymousDialog2.last_message_date) {
                    return 1;
                }
                return -1;
            }
        });
    }

    private void sortUnread(ArrayList<TLRPC.Dialog> paramArrayList) {
        Collections.sort(paramArrayList, new Comparator<TLRPC.Dialog>() {
            public int compare(TLRPC.Dialog paramAnonymousDialog1, TLRPC.Dialog paramAnonymousDialog2) {
                if (paramAnonymousDialog1.unread_count == paramAnonymousDialog2.unread_count) {
                    return 0;
                }
                if (paramAnonymousDialog1.unread_count < paramAnonymousDialog2.unread_count) {
                    return 1;
                }
                return -1;
            }
        });
    }

    private void sortUsersByStatus() {
        //TODO
        /*Collections.sort(MessagesController.getInstance().dialogsUsers, new Comparator<TLRPC.Dialog>() {
            public int compare(TLRPC.Dialog paramAnonymousDialog1, TLRPC.Dialog paramAnonymousDialog2) {
                TLRPC.User localUser1 = MessagesController.getInstance().getUser(Integer.valueOf((int) paramAnonymousDialog2.id));
                TLRPC.User localUser2 = MessagesController.getInstance().getUser(Integer.valueOf((int) paramAnonymousDialog1.id));
                int i = 0;
                if (localUser1 != null) {
                    TLRPC.UserStatus localUserStatus2 = localUser1.status;
                    i = 0;
                    if (localUserStatus2 != null) {
                        if (localUser1.id == UserConfig.getClientUserId()) {
                            i = 50000 + ConnectionsManager.getInstance().getCurrentTime();
                        }
                    }
                }
                int j = 0;
                if (localUser2 != null) {
                    TLRPC.UserStatus localUserStatus1 = localUser2.status;
                    j = 0;
                    if (localUserStatus1 != null) {
                        if (localUser2.id == UserConfig.getClientUserId()) {
                            j = 50000 + ConnectionsManager.getInstance().getCurrentTime();
                        }
                    }
                }

                if ((i > 0) && (j > 0)) {
                    if (i <= j) {
                        return -1;
                    }
                }
                do {
                    do {
                        return 1;
                        i = localUser1.status.expires;
                        break;
                        j = localUser2.status.expires;
                        break label118;
                        if (i < j) {
                            return -1;
                        }
                        return 0;
                        if ((i >= 0) || (j >= 0)) {
                            break label201;
                        }
                    } while (i > j);
                    if (i < j) {
                        return -1;
                    }
                    return 0;
                    if (((i < 0) && (j > 0)) || ((i == 0) && (j != 0))) {
                        return -1;
                    }
                } while (((j < 0) && (i > 0)) || ((j == 0) && (i != 0)));
                return 0;
            }
        });*/
    }

    private void sortUsersDefault() {
        Collections.sort(MessagesController.getInstance().dialogsUsers, new Comparator<TLRPC.Dialog>() {
            public int compare(TLRPC.Dialog paramAnonymousDialog1, TLRPC.Dialog paramAnonymousDialog2) {
                if (paramAnonymousDialog1.last_message_date == paramAnonymousDialog2.last_message_date) {
                    return 0;
                }
                if (paramAnonymousDialog1.last_message_date < paramAnonymousDialog2.last_message_date) {
                    return 1;
                }
                return -1;
            }
        });
    }
}
