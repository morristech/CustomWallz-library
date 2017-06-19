package com.dm.wallpaper.board.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.AddToPlaylistAdapter;
import com.dm.wallpaper.board.adapters.FilterAdapter;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.PlaylistItem;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.LogUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Wallpaper Board
 *
 * Copyright (c) 2017 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class AvailablePlaylistsFragment extends DialogFragment {

    @BindView(R2.id.listview)
    ListView mListView;

    private AsyncTask<Void, Void, Boolean> mGetPlaylists;
    private static final String TAG = "com.dm.wallpaper.board.dialog.playlists";
    private int mId = -1;


    private static AvailablePlaylistsFragment newInstance(int wallpaperId) {
        AvailablePlaylistsFragment fragment = new AvailablePlaylistsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Extras.EXTRA_ID, wallpaperId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showPlaylistsDialog(FragmentManager fm, int wallpaperId) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = AvailablePlaylistsFragment.newInstance(wallpaperId);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {}
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.typeface("Font-Medium.ttf", "Font-Regular.ttf");
        builder.title(R.string.wallpaper_playlist);
        builder.customView(R.layout.fragment_filter, false);
        MaterialDialog dialog = builder.build();
        dialog.show();

        ButterKnife.bind(this, dialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mId = bundle.getInt(Extras.EXTRA_ID);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPlaylists();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //TO_DO: Success/failure toast
        super.onDismiss(dialog);
    }

    private void getPlaylists() {
        mGetPlaylists = new AsyncTask<Void, Void, Boolean>() {

            List<PlaylistItem> playlists;

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        Database database = Database.get(getActivity());
                        playlists = database.getPlaylists();
                        return true;
                    } catch (Exception e) {
                        LogUtil.e(Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    mListView.setAdapter(new AddToPlaylistAdapter(getActivity(), playlists));
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            PlaylistItem playlistItem = (PlaylistItem)mListView.getItemAtPosition(position);
                            Database database = Database.get(getActivity());
                            database.putPlaylistItem(mId, playlistItem.getName());
                            dismiss();
                        }
                    });
                } else
                    dismiss();
                mGetPlaylists = null;
            }
        };
        if (mId != -1)
            mGetPlaylists.execute();
    }
}
