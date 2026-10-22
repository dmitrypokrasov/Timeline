package com.dmitrypokrasov.timelineview.ui;

import android.graphics.RectF;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Immutable frame-owned targets; package visibility keeps query helpers out of the JVM API. */
final class TimelineTargets extends AbstractList<TimelineViewController.Target> {
    private final List<TimelineViewController.Target> values;
    private final Map<Integer, TimelineViewController.Target> byId;
    private final boolean overrideClicks;
    private final boolean stepClickable;
    private final boolean progressClickable;

    private TimelineTargets(List<TimelineViewController.Target> targets) {
        values = new ArrayList<>(targets.size());
        byId = new LinkedHashMap<>();
        for (TimelineViewController.Target target : targets) {
            TimelineViewController.Target owned = copy(target, target.getClickable());
            values.add(owned);
            byId.put(owned.getId(), owned);
        }
        overrideClicks = false;
        stepClickable = false;
        progressClickable = false;
    }

    private TimelineTargets(TimelineTargets source, boolean steps, boolean progress) {
        values = source.values;
        byId = source.byId;
        overrideClicks = true;
        stepClickable = steps;
        progressClickable = progress;
    }

    static List<TimelineViewController.Target> snapshot(List<TimelineViewController.Target> targets) {
        return new TimelineTargets(targets);
    }

    static List<TimelineViewController.Target> withClicks(
            List<TimelineViewController.Target> targets, boolean steps, boolean progress) {
        TimelineTargets source = targets instanceof TimelineTargets
                ? (TimelineTargets) targets : new TimelineTargets(targets);
        return new TimelineTargets(source, steps, progress);
    }

    static TimelineViewController.Target target(List<TimelineViewController.Target> targets, int id) {
        if (targets instanceof TimelineTargets) {
            TimelineTargets indexed = (TimelineTargets) targets;
            TimelineViewController.Target target = indexed.byId.get(id);
            return target == null ? null : copy(target, indexed.clickable(target));
        }
        for (TimelineViewController.Target target : targets) {
            if (target.getId() == id) return copy(target, target.getClickable());
        }
        return null;
    }

    static Set<Integer> ids(List<TimelineViewController.Target> targets) {
        if (targets instanceof TimelineTargets) {
            return new LinkedHashSet<>(((TimelineTargets) targets).byId.keySet());
        }
        Set<Integer> ids = new LinkedHashSet<>();
        for (TimelineViewController.Target target : targets) ids.add(target.getId());
        return ids;
    }

    static Integer hit(List<TimelineViewController.Target> targets, float x, float y, boolean clickableOnly) {
        TimelineTargets indexed = targets instanceof TimelineTargets ? (TimelineTargets) targets : null;
        List<TimelineViewController.Target> owned = indexed == null ? targets : indexed.values;
        Integer nearest = null;
        float distance = Float.POSITIVE_INFINITY;
        for (TimelineViewController.Target target : owned) {
            boolean clickable = indexed == null ? target.getClickable() : indexed.clickable(target);
            RectF bounds = target.getBounds();
            if ((!clickableOnly || clickable) && bounds.contains(x, y)) {
                float dx = x - bounds.centerX();
                float dy = y - bounds.centerY();
                float squared = dx * dx + dy * dy;
                if (nearest == null || squared < distance) {
                    nearest = target.getId();
                    distance = squared;
                }
            }
        }
        return nearest;
    }

    @Override public int size() {
        return values.size();
    }

    @Override public TimelineViewController.Target get(int index) {
        TimelineViewController.Target target = values.get(index);
        return copy(target, clickable(target));
    }

    private boolean clickable(TimelineViewController.Target target) {
        if (!overrideClicks) return target.getClickable();
        return target.getId() == TimelineViewController.PROGRESS_ID ? progressClickable : stepClickable;
    }

    private static TimelineViewController.Target copy(TimelineViewController.Target target, boolean clickable) {
        return new TimelineViewController.Target(target.getId(), new RectF(target.getBounds()),
                target.getDescription(), clickable);
    }
}
