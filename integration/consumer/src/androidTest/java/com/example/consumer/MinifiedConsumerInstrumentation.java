package com.example.consumer;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

/** Platform-only runner: no shared AndroidX Test dependencies for R8 to strip from the target. */
public class MinifiedConsumerInstrumentation extends Instrumentation {
    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }

    @Override public void onStart() {
        Bundle status = new Bundle();
        status.putString("id", "InstrumentationTestRunner");
        status.putString("class", getClass().getName());
        status.putString("test", "xmlAnimationAndClicksSurviveShrinking");
        status.putInt("numtests", 1);
        status.putInt("current", 1);
        sendStatus(1, status);
        Bundle result = new Bundle();
        try {
            xmlAnimationAndClicksSurviveShrinking();
            sendStatus(0, status);
            result.putString("stream", "\nOK (1 test)\n");
        } catch (Throwable failure) {
            status.putString("stack", Log.getStackTraceString(failure));
            sendStatus(-2, status);
            result.putString("stream", "\nFAILURES!!! Tests run: 1, Failures: 1\n" + Log.getStackTraceString(failure));
        }
        finish(Activity.RESULT_OK, result);
    }

    private static void assertTrue(String message, boolean value) {
        if (!value) throw new AssertionError(message);
    }

    private void onMain(Runnable action) {
        final Throwable[] failure = {null};
        runOnMainSync(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                failure[0] = error;
            }
        });
        if (failure[0] != null) throw new AssertionError("Main-thread smoke check failed", failure[0]);
    }

    private void xmlAnimationAndClicksSurviveShrinking() {
        Instrumentation instrumentation = this;
        Intent intent = new Intent().setClassName(instrumentation.getTargetContext(),
                "com.example.consumer.ConsumerActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity activity = instrumentation.startActivitySync(intent);
        try {
            instrumentation.waitForIdleSync();
            // Loading is asynchronous: poll rendered animation pixels with a bounded timeout.
            boolean animated = false;
            long deadline = SystemClock.uptimeMillis() + 10000;
            while (!animated && SystemClock.uptimeMillis() < deadline) {
                final boolean[] visible = {false};
                onMain(() -> {
                    View view = activity.getWindow().getDecorView().findViewWithTag("timeline");
                    assertTrue("Timeline must be measured", view.getWidth() > 0 && view.getHeight() > 0);
                    Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.WHITE);
                    view.draw(new Canvas(bitmap));
                    int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
                    bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    // Fixture is orange; line/text defaults are teal and gray. This proves Lottie parsed/drew.
                    for (int color : pixels) {
                        if (Color.red(color) > 180 && Color.green(color) > 50 && Color.green(color) < 190 && Color.blue(color) < 100) {
                            visible[0] = true;
                            break;
                        }
                    }
                    bitmap.recycle();
                });
                animated = visible[0];
                if (!animated) SystemClock.sleep(100);
            }
            assertTrue("Lottie overlay did not render after resource/code shrinking", animated);
            onMain(() -> {
                View view = activity.getWindow().getDecorView().findViewWithTag("timeline");
                assertTrue("Accessible steps must exist", view.getAccessibilityNodeProvider() != null);
                assertTrue("Step must accept click", view.getAccessibilityNodeProvider().performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null));
                TextView status = activity.getWindow().getDecorView().findViewWithTag("status");
                assertTrue("Click callback must reach the host", "Clicked order".contentEquals(status.getText()));
            });
        } finally {
            onMain(activity::finish);
        }
    }
}
