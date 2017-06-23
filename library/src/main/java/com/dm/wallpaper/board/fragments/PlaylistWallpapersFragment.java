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
import android.widget.ImageView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.PlaylistWallpapersAdapter;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.listeners.PlaylistWallpaperSelectedListener;
import com.dm.wallpaper.board.utils.listeners.WallpaperListener;

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

public class PlaylistWallpapersFragment extends Fragment implements WallpaperListener, PlaylistWallpaperSelectedListener {

    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R2.id.swipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R2.id.favorite_empty)
    ImageView mFavoriteEmpty;

    private AsyncTask<Void, Void, Boolean> mGetWallpapers;
    private String mPlaylistName;
    private MenuItem delete;
    private List<Wallpaper> mWallpapers;
    private PlaylistWallpapersAdapter mPlaylistWallpapersAdapter;
    private PlaylistWallpaperSelectedListener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpapers, container, false);
        mListener = this;
        ButterKnife.bind(this, view);

        if (!Preferences.get(getActivity()).isShadowEnabled()) {
            View shadow = ButterKnife.findById(view, R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }

        Bundle bundle = getArguments();
        if (bundle != null)
            mPlaylistName = bundle.getString(Extras.EXTRA_PLAYLIST_NAME);
        setHasOptionsMenu(true);
        getWallpapers();
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
    public void onDestroy() {
        if (mGetWallpapers != null) mGetWallpapers.cancel(true);
        super.onDestroy();
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
            if (mPlaylistWallpapersAdapter == null || mPlaylistWallpapersAdapter.mSelected == null)
                return true;
            Database db = Database.get(getActivity());

            // Sorting mSelected to prevent IndexOutOfBounds as elements shift after every remove()
            Collections.sort(mPlaylistWallpapersAdapter.mSelected, Collections.reverseOrder());
            for (int position : mPlaylistWallpapersAdapter.mSelected) {
                db.deleteWallpaperFromPlaylist(mWallpapers.get(position).getId());
                mWallpapers.remove(position);
                mPlaylistWallpapersAdapter.notifyItemRemoved(position);
            }
            mPlaylistWallpapersAdapter.mSelected.clear();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWallpaperSelected(int position) {
        if (mRecyclerView == null) return;
        if (position < 0 || position > mRecyclerView.getAdapter().getItemCount()) return;

        mRecyclerView.scrollToPosition(position);
    }

    private void getWallpapers() {
        mGetWallpapers = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mWallpapers = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        mWallpapers = Database.get(getActivity()).getWallpapersInPlaylist(mPlaylistName);
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
                    mPlaylistWallpapersAdapter = new PlaylistWallpapersAdapter(getActivity(), mWallpapers, mListener);
                    mRecyclerView.setAdapter(mPlaylistWallpapersAdapter);

                    if (mRecyclerView.getAdapter().getItemCount() == 0) {
                        int color = ColorHelper.getAttributeColor(getActivity(),
                                android.R.attr.textColorSecondary);

                        mFavoriteEmpty.setImageDrawable(
                                DrawableHelper.getTintedDrawable(getActivity(),
                                        R.drawable.ic_wallpaper_favorite_empty,
                                        ColorHelper.setColorAlpha(color, 0.7f)));
                        mFavoriteEmpty.setVisibility(View.VISIBLE);
                    }
                }
                mGetWallpapers = null;
            }
        }.execute();
    }

    @Override
    public void showDelete() {
        if (mPlaylistWallpapersAdapter == null)
            return;
        if (mPlaylistWallpapersAdapter.mSelected.size() > 0) {
            delete.setVisible(true);
            delete.setEnabled(true);
        } else {
            delete.setVisible(false);
            delete.setEnabled(false);
        }
    }
}
