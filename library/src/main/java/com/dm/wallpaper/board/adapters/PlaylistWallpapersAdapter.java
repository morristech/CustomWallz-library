package com.dm.wallpaper.board.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
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
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.activities.WallpaperBoardPreviewActivity;
import com.dm.wallpaper.board.fragments.FavoritesFragment;
import com.dm.wallpaper.board.fragments.WallpaperSearchFragment;
import com.dm.wallpaper.board.fragments.WallpapersFragment;
import com.dm.wallpaper.board.helpers.WallpaperHelper;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.dm.wallpaper.board.utils.listeners.PlaylistWallpaperSelectedListener;
import com.dm.wallpaper.board.utils.listeners.WallpaperListener;
import com.dm.wallpaper.board.utils.views.HeaderView;
import com.kogitune.activitytransition.ActivityTransitionLauncher;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

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

public class PlaylistWallpapersAdapter extends RecyclerView.Adapter<PlaylistWallpapersAdapter.ViewHolder> {

    private final Context mContext;
    private final DisplayImageOptions.Builder mOptions;
    private List<Wallpaper> mWallpapers;
    public List<Integer> mSelected;
    private PlaylistWallpaperSelectedListener mListener;

    public PlaylistWallpapersAdapter(@NonNull Context context, @NonNull List<Wallpaper> wallpapers,
                                     PlaylistWallpaperSelectedListener listener) {
        mContext = context;
        mWallpapers = wallpapers;
        mSelected = new ArrayList<>();
        mListener = listener;


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
                R.layout.fragment_playlist_wallpapers_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mWallpapers.get(position).getName());
        holder.author.setText(mWallpapers.get(position).getAuthor());

        String url = WallpaperHelper.getThumbnailUrl(mContext,
                mWallpapers.get(position).getUrl(),
                mWallpapers.get(position).getThumbUrl());

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
                            holder.author.setTextColor(primary);
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
                                    holder.author.setTextColor(text);
                                });
                            }
                        }
                    }
                }, null);
    }

    @Override
    public int getItemCount() {
        return mWallpapers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @BindView(R2.id.card)
        CardView card;
        @BindView(R2.id.container)
        RelativeLayout container;
        @BindView(R2.id.image)
        HeaderView image;
        @BindView(R2.id.name)
        TextView name;
        @BindView(R2.id.author)
        TextView author;
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

            int colorAccent;
            if (Build.VERSION.SDK_INT >= 21) {
                colorAccent = ColorHelper.getAttributeColor(mContext, android.R.attr.colorAccent);
            } else colorAccent = mContext.getResources().getColor(R.color.colorAccent);
            check.setColorFilter(colorAccent);
            container.setOnClickListener(this);
            container.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (position < 0 || position > mWallpapers.size()) {
                return;
            }
            if (id == R.id.container) {
                if (mSelected.size() > 0) {
                    selectDeselectWallpapers(position);
                } else {
                    try {
                        final Intent intent = new Intent(mContext, WallpaperBoardPreviewActivity.class);
                        intent.putExtra(Extras.EXTRA_URL, mWallpapers.get(position).getUrl());
                        intent.putExtra(Extras.EXTRA_AUTHOR, mWallpapers.get(position).getAuthor());
                        intent.putExtra(Extras.EXTRA_NAME, mWallpapers.get(position).getName());
                        intent.putExtra(Extras.EXTRA_ID, mWallpapers.get(position).getId());
                        intent.putExtra(Extras.EXTRA_PLAYLIST_NAME, mWallpapers.get(position).getPlaylists());

                        ActivityTransitionLauncher.with((AppCompatActivity) mContext)
                                .from(image, Extras.EXTRA_IMAGE)
                                .image(((BitmapDrawable) image.getDrawable()).getBitmap())
                                .launch(intent);
                    } catch (Exception e) {
                    }

                    FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                    if (fm != null) {
                        Fragment fragment = fm.findFragmentById(R.id.container);
                        if (fragment != null) {
                            if (fragment instanceof WallpapersFragment ||
                                    fragment instanceof FavoritesFragment ||
                                    fragment instanceof WallpaperSearchFragment) {
                                WallpaperListener listener = (WallpaperListener) fragment;
                                listener.onWallpaperSelected(position);
                            }
                        }
                    }
                }

            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (position < 0 || position > mWallpapers.size()) {
                return false;
            }
            if (id == R.id.container) {
                selectDeselectWallpapers(position);
                return true;
            }
            return false;
        }

        private void selectDeselectWallpapers(int position) {
            if (check.getVisibility() == View.GONE) {
                mSelected.add(position);
                check.setVisibility(View.VISIBLE);
                mListener.showDelete();
            } else if (check.getVisibility() == View.VISIBLE) {
                int pos = -1;
                for (int i = 0; i <= mSelected.size(); i++) {
                    if (mSelected.get(i) == position) {
                        pos = i;
                        break;
                    }
                }
                if (pos > -1)
                    mSelected.remove(pos);
                check.setVisibility(View.GONE);
                mListener.showDelete();
            }
        }
    }
}
