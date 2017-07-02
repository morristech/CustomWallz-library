package com.dm.wallpaper.board.utils;


import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dm.wallpaper.board.services.WallpaperAutoChangeService;

public class ScheduleAutoApply {
    public static void schedule (Context context) {
        Log.i("GAAH", "schedule: ");
        ComponentName componentName = new ComponentName(context, WallpaperAutoChangeService.class);
        JobInfo.Builder builder = new JobInfo.Builder(1, componentName);

        SharedPreferences preferences = context.getSharedPreferences(
                WallpaperAutoChangeService.TAG, Context.MODE_PRIVATE);
        long interval = preferences.getLong(WallpaperAutoChangeService.INTERVAL, 0);
        if (interval == 0)
            return;

        ((JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancelAll();
        builder.setMinimumLatency(interval);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }
}
