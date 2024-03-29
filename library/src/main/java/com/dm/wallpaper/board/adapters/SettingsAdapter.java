package com.dm.wallpaper.board.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.fragments.dialogs.AvailablePlaylistsFragment;
import com.dm.wallpaper.board.fragments.dialogs.LanguagesFragment;
import com.dm.wallpaper.board.items.PlaylistItem;
import com.dm.wallpaper.board.items.Setting;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.services.WallpaperAutoChangeService;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.ScheduleAutoApply;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Setting> mSettings;

    private static final int TYPE_CONTENT = 0;
    private static final int TYPE_FOOTER = 1;

    public SettingsAdapter(@NonNull Context context, @NonNull List<Setting> settings) {
        mContext = context;
        mSettings = settings;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CONTENT) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_settings_item_list, parent, false);
            return new ContentViewHolder(view);
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_settings_item_footer, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_CONTENT) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            Setting setting = mSettings.get(position);

            if (setting.getTitle().length() == 0) {
                contentViewHolder.title.setVisibility(View.GONE);
                contentViewHolder.divider.setVisibility(View.GONE);
                contentViewHolder.container.setVisibility(View.VISIBLE);

                contentViewHolder.subtitle.setText(setting.getSubtitle());

                if (setting.getContent().length() == 0) {
                    contentViewHolder.content.setVisibility(View.GONE);
                } else {
                    contentViewHolder.content.setText(setting.getContent());
                    contentViewHolder.content.setVisibility(View.VISIBLE);
                }

                if (setting.getFooter().length() == 0) {
                    contentViewHolder.footer.setVisibility(View.GONE);
                } else {
                    contentViewHolder.footer.setText(setting.getFooter());
                }

                if (setting.getCheckState() >= 0) {
                    contentViewHolder.checkBox.setVisibility(View.VISIBLE);
                    contentViewHolder.checkBox.setChecked(setting.getCheckState() == 1);
                } else {
                    contentViewHolder.checkBox.setVisibility(View.GONE);
                }
            } else {
                contentViewHolder.container.setVisibility(View.GONE);
                contentViewHolder.title.setVisibility(View.VISIBLE);
                contentViewHolder.title.setText(setting.getTitle());

                if (position > 0) {
                    contentViewHolder.divider.setVisibility(View.VISIBLE);
                } else {
                    contentViewHolder.divider.setVisibility(View.GONE);
                }

                if (setting.getIcon() != -1) {
                    int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                    contentViewHolder.title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                            mContext, setting.getIcon(), color), null, null, null);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mSettings.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == (getItemCount() - 1)) return TYPE_FOOTER;
        return TYPE_CONTENT;
    }

    class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R2.id.title)
        TextView title;
        @BindView(R2.id.subtitle)
        TextView subtitle;
        @BindView(R2.id.content)
        TextView content;
        @BindView(R2.id.footer)
        TextView footer;
        @BindView(R2.id.container)
        LinearLayout container;
        @BindView(R2.id.checkbox)
        AppCompatCheckBox checkBox;
        @BindView(R2.id.divider)
        View divider;

        ContentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = getAdapterPosition();

                if (position < 0 || position > mSettings.size()) return;

                Setting setting = mSettings.get(position);
                switch (setting.getType()) {
                    case CACHE:
                        new MaterialDialog.Builder(mContext)
                                .typeface("Font-Medium.ttf", "Font-Regular.ttf")
                                .content(R.string.pref_data_cache_clear_dialog)
                                .positiveText(R.string.clear)
                                .negativeText(android.R.string.cancel)
                                .onPositive((dialog, which) -> {
                                    try {
                                        File cache = mContext.getCacheDir();
                                        FileHelper.clearDirectory(cache);

                                        double size = (double) FileHelper.getDirectorySize(
                                                mContext.getCacheDir()) / FileHelper.MB;
                                        NumberFormat formatter = new DecimalFormat("#0.00");

                                        setting.setFooter(String.format(mContext.getResources().getString(
                                                R.string.pref_data_cache_size),
                                                formatter.format(size) + " MB"));
                                        notifyItemChanged(position);

                                        Toast.makeText(mContext, R.string.pref_data_cache_cleared,
                                                Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        LogUtil.e(Log.getStackTraceString(e));
                                    }
                                })
                                .show();
                        break;
                    case THEME:
                        Preferences.get(mContext).setDarkTheme(!checkBox.isChecked());
                        ((AppCompatActivity) mContext).recreate();
                        break;
                    case LANGUAGE:
                        LanguagesFragment.showLanguageChooser(((AppCompatActivity) mContext).getSupportFragmentManager());
                        break;
                    case COLORED_CARD:
                        Preferences.get(mContext).setColoredWallpapersCard(
                                !Preferences.get(mContext).isColoredWallpapersCard());
                        checkBox.setChecked(Preferences.get(mContext).isColoredWallpapersCard());
                        break;
                    case RESET_TUTORIAL:
                        Preferences.get(mContext).setTimeToShowWallpapersIntro(true);
                        Preferences.get(mContext).setTimeToShowWallpaperPreviewIntro(true);

                        Toast.makeText(mContext, R.string.pref_others_reset_tutorial_reset, Toast.LENGTH_LONG).show();
                        break;
                    case APPLY:
                        SharedPreferences prefrences = mContext.getSharedPreferences(WallpaperAutoChangeService.TAG, Context.MODE_PRIVATE);
                        new MaterialDialog.Builder(mContext)
                                .typeface("Font-Medium.ttf", "Font-Regular.ttf")
                                .content(R.string.auto_apply_content)
                                .inputType(InputType.TYPE_CLASS_NUMBER)
                                .input(mContext.getResources().getString(R.string.auto_apply_hint),
                                        String.valueOf(prefrences.getLong(WallpaperAutoChangeService.INTERVAL, 0)),
                                        false,
                                        (dialog, input) -> {
                                            prefrences
                                                    .edit()
                                                    .putLong(WallpaperAutoChangeService.INTERVAL, Long.parseLong(input.toString()) / 1000)
                                                    .apply();
                                            Log.i("GAAH", "input callback: ");
                                            ScheduleAutoApply.schedule(mContext);
                                        }
                                )
                                .show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(View itemView) {
            super(itemView);
            if (!Preferences.get(mContext).isShadowEnabled()) {
                View shadow = ButterKnife.findById(itemView, R.id.shadow);
                shadow.setVisibility(View.GONE);

                View root = shadow.getRootView();
                root.setPadding(0, 0, 0, 0);
            }
        }
    }
}
