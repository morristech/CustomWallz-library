package com.dm.wallpaper.board.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;

import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.helpers.WallpaperHelper;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.utils.ScheduleAutoApply;

import java.util.List;

import static com.dm.wallpaper.board.utils.Extras.EXTRA_PLAYLIST_NAME;

public class WallpaperAutoChangeService extends JobService {

    public static final String TAG = "wallpaperChange";
    public static final String INTERVAL = "interval";
    public static final String LAST_WALLPAPER_NAME = "lastWallpaper";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        SharedPreferences preferences = getSharedPreferences(TAG, MODE_PRIVATE);
        String playlistName = preferences.getString(EXTRA_PLAYLIST_NAME, null);

        if (playlistName == null)
            return false;

        Database db = Database.get(getApplicationContext());
        List<Wallpaper> wallpapers = db.getWallpapersInPlaylist(playlistName);
        Log.i(TAG, "onStartJob: wall size" + wallpapers.size());
        String lastWallpaperName = preferences.getString(LAST_WALLPAPER_NAME, null);
        Log.i(TAG, "onStartJob: name " + lastWallpaperName);
        int lastIndex = lastWallpaperName == null ? 0 : findWallpaperIndex(wallpapers, lastWallpaperName);
        Log.i(TAG, "onStartJob: last index" + lastIndex);

        if (wallpapers.size() == 0 || wallpapers.size() == 1)
            return false;

        int nextIndex;
        if (wallpapers.size() > lastIndex + 1)
            nextIndex = lastIndex + 1;
        else
            nextIndex = 0;
        Wallpaper wallpaper = wallpapers.get(nextIndex);

        WallpaperHelper.applyWallpaper(this, null, -1, wallpaper.getUrl(), wallpaper.getName());
        preferences.edit().putString(LAST_WALLPAPER_NAME, wallpaper.getName()).apply();

        ScheduleAutoApply.schedule(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private int findWallpaperIndex(List<Wallpaper> wallpapers, String name) {
        for (int i = 0; i < wallpapers.size(); i++)
            if (name.equals(wallpapers.get(i).getName()))
                return i;

        // Last wallpaper was removed. Restart from the beginning.
        return 0;
    }
}
