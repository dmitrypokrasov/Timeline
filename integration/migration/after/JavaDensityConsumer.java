package com.example.migration;

import android.content.Context;
import com.dmitrypokrasov.timelineview.config.TimelineConfig;
import com.dmitrypokrasov.timelineview.config.TimelineDefaults;

/** Static entry points are also compiled from Java against the staged AAR. */
public final class JavaDensityConsumer {
    public static TimelineConfig defaults(Context context) {
        return TimelineDefaults.config(context);
    }

    public static float titlePixels(Context context) {
        return TimelineDefaults.sp(context, 18f);
    }
}
