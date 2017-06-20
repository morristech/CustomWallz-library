package com.dm.wallpaper.board.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.activities.WallpaperBoardPreviewActivity;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.fragments.FavoritesFragment;
import com.dm.wallpaper.board.fragments.WallpaperSearchFragment;
import com.dm.wallpaper.board.fragments.WallpapersFragment;
import com.dm.wallpaper.board.fragments.dialogs.WallpaperOptionsFragment;
import com.dm.wallpaper.board.helpers.WallpaperHelper;
import com.dm.wallpaper.board.items.PlaylistItem;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.dm.wallpaper.board.utils.listeners.WallpaperListener;
import com.dm.wallpaper.board.utils.views.HeaderView;
import com.kogitune.activitytransition.ActivityTransitionLauncher;
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

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

    private final Context mContext;
    private final DisplayImageOptions.Builder mOptions;
    private List<PlaylistItem> mPlaylists;
    private List<PlaylistItem> mPlaylistsAll;

    private int mLastSelectedPosition = -1;
    private Database db;
    public static boolean sIsClickable = true;

    public PlaylistsAdapter(@NonNull Context context, @NonNull List<PlaylistItem> wallpapers,
                            boolean isSearchMode) {
        mContext = context;
        mPlaylists = wallpapers;
        db = Database.get(mContext);

        if (isSearchMode) {
            mPlaylistsAll = new ArrayList<>();
            mPlaylistsAll.addAll(mPlaylists);
        }

        int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
        Drawable loading = getDefaultImage(mContext, R.drawable.ic_default_image_loading, color,
                mContext.getResources().getDimensionPixelSize(R.dimen.default_image_padding));
        Drawable failed = getDefaultImage(mContext, R.drawable.ic_default_image_failed, color,
                mContext.getResources().getDimensionPixelSize(R.dimen.default_image_padding));
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
        List<Wallpaper> t = db.getWallpapersInPlaylist(mPlaylists.get(position).getName());
        String url;
        if (t != null && t.size() > 0)
            url = WallpaperHelper.getThumbnailUrl(mContext, t.get(0).getUrl(), t.get(0).getThumbUrl());
        else
            url = "";
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R2.id.playlist_card)
        CardView card;
        @BindView(R2.id.playlists_container)
        LinearLayout container;
        @BindView(R2.id.playlists_image)
        HeaderView image;
        @BindView(R2.id.playlists_name)
        TextView name;

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
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.playlists_container) {
                if (sIsClickable) {
                    sIsClickable = false;
                    try {
                        final Intent intent = new Intent(mContext, WallpaperBoardPreviewActivity.class);
                        intent.putExtra(Extras.EXTRA_URL, mPlaylists.get(position).getUrl());
                        intent.putExtra(Extras.EXTRA_NAME, mPlaylists.get(position).getName());

                        ActivityTransitionLauncher.with((AppCompatActivity) mContext)
                                .from(image, Extras.EXTRA_IMAGE)
                                .image(((BitmapDrawable) image.getDrawable()).getBitmap())
                                .launch(intent);
                    } catch (Exception e) {
                        sIsClickable = true;
                    }
                }
            }
        }
    }


    /*public void filter(String includeFilterTags, String excludeFilterTags) {
        mPlaylists = Database.get(mContext).getFilteredWallpapers(includeFilterTags, excludeFilterTags);
        notifyDataSetChanged();
    }*/
/*
    public void downloadLastSelectedWallpaper() {
        if (mLastSelectedPosition < 0 || mLastSelectedPosition > mPlaylists.size()) return;

        WallpaperHelper.downloadWallpaper(mContext,
                ColorHelper.getAttributeColor(mContext, R.attr.colorAccent),
                mPlaylists.get(mLastSelectedPosition).getUrl(),
                mPlaylists.get(mLastSelectedPosition).getName());
    }*/

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
}
