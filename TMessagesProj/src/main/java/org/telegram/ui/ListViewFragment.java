package org.telegram.ui;

import android.support.v4.app.Fragment;

import org.telegram.ui.Components.RecyclerListView;

/**
 * Created by Morteza on 2016/02/17.
 */
public class ListViewFragment extends Fragment {

    private RecyclerListView listView;


    public RecyclerListView getListView() {
        return listView;
    }

    public void setListView(RecyclerListView listView) {
        this.listView = listView;
    }
}
