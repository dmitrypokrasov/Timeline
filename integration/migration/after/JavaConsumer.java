package com.example.migration;

import android.content.Context;
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig;
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig;
import com.dmitrypokrasov.timelineview.ui.TimelineView;

/** Java source compatibility is checked against the staged AAR too. */
public final class JavaConsumer {
    public static TimelineView create(Context context) {
        TimelineView view = new TimelineView(context);
        view.setMathEngine(new CompactCustomMath(new TimelineMathConfig()));
        view.setConfig(new TimelineMathConfig(), new TimelineUiConfig());
        return view;
    }
}
