package com.dm.wallpaper.board.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.fragments.FavoritesFragment;
import com.dm.wallpaper.board.fragments.PlaylistWallpapersFragment;
import com.dm.wallpaper.board.helpers.WallpaperHelper;
import com.dm.wallpaper.board.items.PlaylistItem;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.dm.wallpaper.board.utils.listeners.PlaylistWallpaperSelectedListener;
import com.dm.wallpaper.board.utils.listeners.PlaylistWallpapersListener;
import com.dm.wallpaper.board.utils.views.HeaderView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.dm.wallpaper.board.helpers.DrawableHelper.getDefaultImage;

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

public class PlaylistsHolderAdapter extends RecyclerView.Adapter<PlaylistsHolderAdapter.ViewHolder> {

    private final Context mContext;
    private final DisplayImageOptions.Builder mOptions;
    private List<PlaylistItem> mPlaylists;
    private List<PlaylistItem> mPlaylistsAll;
    private PlaylistWallpapersListener mPlaylistWallpapersListener;
    public List<WallpaperIds> mSelected;
    private PlaylistWallpaperSelectedListener mSelectedListener;

    private Database db;

    public PlaylistsHolderAdapter(@NonNull Context context, @NonNull List<PlaylistItem> wallpapers,
                                  boolean isSearchMode, PlaylistWallpapersListener playlistWallpapersListener,
                                  PlaylistWallpaperSelectedListener selectedListener) {
        mContext = context;
        mPlaylists = wallpapers;
        db = Database.get(mContext);
        mPlaylistWallpapersListener = playlistWallpapersListener;
        mSelectedListener = selectedListener;
        mSelected = new ArrayList<>();

        if (isSearchMode) {
            mPlaylistsAll = new ArrayList<>();
            mPlaylistsAll.addAll(mPlaylists);
        }

        int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
        Drawable loading = getDefaultImage(mContext, R.drawable.ic_default_image_loading, color,
                mContext.getResources().getDimensionPixelSize(R.dimen.default_image_padding));
        Drawable failed = mContext.getResources().getDrawable(R.drawable.blank_playlist);
        mOptions = ImageConfig.getRawDefaultImageOptions();
        mOptions.resetViewBeforeLoading(true);
        mOptions.cacheInMemory(true);
        mOptions.cacheOnDisk(true);
        mOptions.showImageForEmptyUri(failed);
        mOptions.showImageOnFail(failed);
        mOptions.showImageOnLoading(loading);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_playlists_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mPlaylists.get(position).getName());
        List<Wallpaper> wallpapers = db.getWallpapersInPlaylist(mPlaylists.get(position).getName());
        String url;
        if (wallpapers != null && wallpapers.size() > 0) {
            url = WallpaperHelper.getThumbnailUrl(mContext, wallpapers.get(0).getUrl(), wallpapers.get(0).getThumbUrl());
            String count = wallpapers.size() > 99 ? "99+" : String.valueOf(wallpapers.size());
            holder.counter.setText(count);
        } else {
            url = "";
            holder.counter.setText("0");
        }
        mPlaylists.get(position).setUrl(url);
        ImageLoader.getInstance().displayImage(url, new ImageViewAware(holder.image),
                mOptions.build(), ImageConfig.getThumbnailSize(mContext), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        if (Preferences.get(mContext).isColoredWallpapersCard()) {
                            int vibrant = ColorHelper.getAttributeColor(
                                    mContext, R.attr.card_background);
                            holder.card.setCardBackgroundColor(vibrant);
                            int primary = ColorHelper.getAttributeColor(
                                    mContext, android.R.attr.textColorPrimary);
                            holder.name.setTextColor(primary);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        if (Preferences.get(mContext).isColoredWallpapersCard()) {
                            if (loadedImage != null) {
                                Palette.from(loadedImage).generate(palette -> {
                                    int vibrant = ColorHelper.getAttributeColor(
                                            mContext, R.attr.card_background);
                                    int color = palette.getVibrantColor(vibrant);
                                    if (color == vibrant)
                                        color = palette.getMutedColor(vibrant);
                                    holder.card.setCardBackgroundColor(color);
                                    int text = ColorHelper.getTitleTextColor(color);
                                    holder.name.setTextColor(text);
                                });
                            }
                        }
                    }
                }, null);
    }

    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @BindView(R2.id.playlist_card)
        CardView card;
        @BindView(R2.id.playlists_container)
        RelativeLayout container;
        @BindView(R2.id.playlists_image)
        HeaderView image;
        @BindView(R2.id.playlists_name)
        TextView name;
        @BindView(R2.id.playlists_counter)
        TextView counter;
        @BindView(R2.id.check)
        ImageView check;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (!Preferences.get(mContext).isShadowEnabled()) {
                card.setCardElevation(0f);
            }

            if (mContext.getResources().getBoolean(R.bool.enable_wallpaper_card_rounded_corner)) {
                card.setRadius(mContext.getResources().getDimensionPixelSize(R.dimen.card_corner_radius));
            }

            container.setOnClickListener(this);
            container.setOnLongClickListener(this);

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            int colorAccent;
            ViewCompat.setBackground(counter, DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_circle, color));
            counter.setTextColor(ColorHelper.getTitleTextColor(color));
            if (Build.VERSION.SDK_INT >= 21) {
                colorAccent = ColorHelper.getAttributeColor(mContext, android.R.attr.colorAccent);
            } else colorAccent = mContext.getResources().getColor(R.color.colorAccent);
            check.setColorFilter(colorAccent);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            int id = view.getId();

            if (position < 0 || position > mPlaylists.size()) {
                return;
            }
            if (id == R.id.playlists_container) {
                if (mSelected.size() > 0) {
                    selectDeselectWallpapers(position);
                } else {
                    String selectedName = mPlaylists.get(position).getName();
                    if ("Favourites".equals(selectedName)) {
                        mPlaylistWallpapersListener.onFavouritesSelected();
                    } else {
                        PlaylistWallpapersFragment playlistWallpapersFragment = new PlaylistWallpapersFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Extras.EXTRA_PLAYLIST_NAME, selectedName);
                        playlistWallpapersFragment.setArguments(bundle);
                        mPlaylistWallpapersListener.onPlaylistSelected(playlistWallpapersFragment);
                    }
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (position < 0 || position > mPlaylists.size()) {
                return false;
            }
            if (id == R.id.playlists_container) {
                selectDeselectWallpapers(position);
                return true;
            }
            return false;
        }

        private void selectDeselectWallpapers(int position) {
            if ("Favourites".equals(mPlaylists.get(position).getName()))
                return;

            if (check.getVisibility() == View.GONE) {
                mSelected.add(new WallpaperIds(position, mPlaylists.get(position).getName()));
                check.setVisibility(View.VISIBLE);
                mSelectedListener.showDelete();
            } else if (check.getVisibility() == View.VISIBLE) {
                int pos = -1;
                for (int i = 0; i <= mSelected.size(); i++) {
                    if (mSelected.get(i).position == position) {
                        pos = i;
                        break;
                    }
                }
                if (pos > -1)
                    mSelected.remove(pos);
                check.setVisibility(View.GONE);
                mSelectedListener.showDelete();
            }
        }
    }

    public void search(String string) {
        String query = string.toLowerCase(Locale.getDefault()).trim();
        mPlaylists.clear();
        if (query.length() == 0) mPlaylists.addAll(mPlaylistsAll);
        else {
            for (int i = 0; i < mPlaylistsAll.size(); i++) {
                PlaylistItem playlistItem = mPlaylistsAll.get(i);
                String name = playlistItem.getName().toLowerCase(Locale.getDefault());
                if (name.contains(query)) {
                    mPlaylists.add(playlistItem);
                }
            }
        }
        notifyDataSetChanged();
    }


    public class WallpaperIds {
        public int position;
        public String name;
        WallpaperIds(int position, String name) {
            this.position = position;
            this.name = name;
        }
    }

}
