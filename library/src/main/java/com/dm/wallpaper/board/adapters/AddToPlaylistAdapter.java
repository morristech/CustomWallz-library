package com.dm.wallpaper.board.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.PlaylistItem;
import com.dm.wallpaper.board.items.Wallpaper;

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

public class AddToPlaylistAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<PlaylistItem> mPlaylists;
    private Database db;

    public AddToPlaylistAdapter(@NonNull Context context, @NonNull List<PlaylistItem> playlists) {
        mContext = context;
        mPlaylists = playlists;
        db = Database.get(mContext);
    }

    @Override
    public int getCount() {
        return mPlaylists.size();
    }

    @Override
    public PlaylistItem getItem(int position) {
        return mPlaylists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_playlists_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        PlaylistItem playlistItem = mPlaylists.get(position);
        holder.title.setText(playlistItem.getName());

        List<Wallpaper> wallpapers = db.getWallpapersInPlaylist(playlistItem.getName());
        String count = wallpapers.size() > 99 ? "99+" : String.valueOf(wallpapers.size());
        holder.counter.setText(count);
        return view;
    }

    class ViewHolder {

        @BindView(R2.id.playlist_container)
        LinearLayout container;
        @BindView(R2.id.playlists_title)
        TextView title;
        @BindView(R2.id.playlists_counter)
        TextView counter;

        ViewHolder(@NonNull View view) {
            ButterKnife.bind(this, view);
            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            ViewCompat.setBackground(counter, DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_circle, color));
            counter.setTextColor(ColorHelper.getTitleTextColor(color));
        }
    }
}
