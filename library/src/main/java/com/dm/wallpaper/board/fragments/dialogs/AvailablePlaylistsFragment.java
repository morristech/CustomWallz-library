package com.dm.wallpaper.board.fragments.dialogs;

import android.app.Dialog;
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
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.AddToPlaylistAdapter;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.PlaylistItem;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.LogUtil;

import java.util.Collections;
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
    TextView mNewPlaylist;

    private static final String TAG = "com.dm.wallpaper.board.dialog.playlists";
    private int mId = -1;
    private Database database;


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
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.typeface("Font-Medium.ttf", "Font-Regular.ttf");
        builder.title(R.string.wallpaper_playlist);
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_filter, null);
        builder.customView(view, false);

        mNewPlaylist = (TextView) view.findViewById(R.id.new_playlist);
        mNewPlaylist.setVisibility(View.VISIBLE);
        mNewPlaylist.setOnClickListener((v) -> newPlaylistDialog());

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
        database = Database.get(getActivity());
        getPlaylists();
    }

    private void getPlaylists() {
        if (mId != -1)
            new GetPlaylists().execute();
    }

    private void newPlaylistDialog() {
        new MaterialDialog.Builder(getContext())
                .input(R.string.playlists_add_hint, R.string.playlists_add_prefill, false,
                        (dialog, input) -> database.putNewPlaylist(new PlaylistItem(0, input.toString()))
                ).onPositive((@NonNull MaterialDialog dialog, @NonNull DialogAction which) ->
                new GetPlaylists().execute()
        ).show();
    }

    private class GetPlaylists extends AsyncTask<Void, Void, Boolean> {
        List<PlaylistItem> playlists;

        @Override
        protected Boolean doInBackground(Void... voids) {
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

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Collections.reverse(playlists);
                mListView.setAdapter(new AddToPlaylistAdapter(getActivity(), playlists));
                mListView.setOnItemClickListener((parent, view, position, id) -> {
                    if (((PlaylistItem) mListView.getItemAtPosition(position)).getId() == -2) {
                        newPlaylistDialog();
                        return;
                    }
                    PlaylistItem playlistItem = (PlaylistItem) mListView.getItemAtPosition(position);
                    database.putWallpaperInPlaylist(mId, playlistItem.getName());

                    CafeBar.builder(getContext())
                            .theme(new CafeBarTheme.Custom(ColorHelper.getAttributeColor(
                                    getContext(), R.attr.card_background)))
                            .fitSystemWindow()
                            .typeface("Font-Regular.ttf", "Font-Bold.ttf")
                            .content(String.format(
                                    getContext().getResources().getString(
                                            R.string.playlist_wallpaper_added), playlistItem.getName()))
                            .icon(R.drawable.ic_toolbar_storage)
                            .show();

                    dismiss();
                });
            } else
                dismiss();
        }
    }
}
