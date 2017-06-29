package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.PlaylistsHolderAdapter;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.PlaylistItem;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.TextViewPadding;
import com.dm.wallpaper.board.utils.listeners.PlaylistWallpaperSelectedListener;
import com.dm.wallpaper.board.utils.listeners.PlaylistWallpapersListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.dm.wallpaper.board.helpers.ViewHelper.resetViewBottomPadding;

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

public class PlaylistsHolderFragment extends Fragment implements PlaylistWallpaperSelectedListener {

    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R2.id.swipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R2.id.empty_tv)
    TextView mNoPlaylists;

    private AsyncTask<Void, Void, Boolean> mGetWallpapers;
    private List<PlaylistItem> mPlaylists;
    private PlaylistsHolderAdapter adapter;
    private PlaylistWallpaperSelectedListener mListener;
    private MenuItem delete;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists_holder, container, false);
        ButterKnife.bind(this, view);
        mListener = this;

        if (!Preferences.get(getActivity()).isShadowEnabled()) {
            View shadow = ButterKnife.findById(view, R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        resetViewBottomPadding(mRecyclerView, true);
        mSwipe.setEnabled(false);

        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.wallpapers_column_count)));
        mRecyclerView.setHasFixedSize(false);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView, getActivity().getResources().getInteger(
                R.integer.wallpapers_column_count));
        resetViewBottomPadding(mRecyclerView, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_playlists_wallpapers, menu);
        delete = menu.findItem(R.id.menu_delete);
        delete.setVisible(false);
        delete.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_delete) {
            delete.setVisible(false);
            delete.setEnabled(false);
            if (adapter == null || adapter.mSelected == null)
                return true;
            Database db = Database.get(getActivity());

            // Sorting mSelected to prevent IndexOutOfBounds as elements shift after every remove()
            Collections.sort(adapter.mSelected, Collections.reverseOrder());
            for (int position : adapter.mSelected) {
                db.deletePlaylist(mPlaylists.get(position).getName());
                mPlaylists.remove(position);
                adapter.notifyItemRemoved(position);
            }
            adapter.mSelected.clear();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if (mGetWallpapers != null) mGetWallpapers.cancel(true);
        super.onDestroy();
    }

    public void getPlaylists() {
        mGetWallpapers = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mPlaylists = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        mPlaylists = Database.get(getActivity()).getPlaylists();
                        return !(mPlaylists == null || mPlaylists.size() == 0);
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
                    Collections.reverse(mPlaylists);
                    PlaylistWallpapersListener playlistWallpapersListener = (PlaylistWallpapersListener) getActivity();
                    adapter = new PlaylistsHolderAdapter(getActivity(), mPlaylists, true, playlistWallpapersListener, mListener);
                    mRecyclerView.setAdapter(adapter);
                }


                if (adapter == null || adapter.getItemCount() == 0) {
                    TextViewPadding.setPaddings(mNoPlaylists, getActivity());
                    mNoPlaylists.setVisibility(View.VISIBLE);
                } else
                    mNoPlaylists.setVisibility(View.GONE);

                mGetWallpapers = null;
            }
        }.execute();

    }

    @Override
    public void showDelete() {
        if (adapter == null)
            return;
        if (adapter.mSelected.size() > 0) {
            delete.setVisible(true);
            delete.setEnabled(true);
        } else {
            delete.setVisible(false);
            delete.setEnabled(false);
        }
    }
}
