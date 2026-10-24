package com.dmitrypokrasov.timelineview.testing;

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig;
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryInput;
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryResult;
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryStrategy;
import com.dmitrypokrasov.timelineview.geometry.TimelinePathCommand;
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryBounds;
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryStep;
import com.dmitrypokrasov.timelineview.geometry.TimelineTextMetrics;
import com.dmitrypokrasov.timelineview.model.TimelineStepData;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Переиспользуемый контракт чистой стратегии, доступный потребителям через testFixtures. */
public abstract class TimelineGeometryContractTest {
    private final TimelineGeometryStrategy strategy;

    /** Наследник передаёт свою стратегию; семантика прогресса пользователя не ограничивается. */
    protected TimelineGeometryContractTest(TimelineGeometryStrategy strategy) {
        this.strategy = strategy;
    }

    @Test
    public void deterministicEmptyAndNarrowInputs() {
        for (int width : new int[] {0, 1, 320}) {
            TimelineGeometryInput input = new TimelineGeometryInput(
                new TimelineMathConfig(), width, 0f, Collections.emptyList());
            TimelineGeometryResult result = strategy.calculate(input);
            assertEquals(result, strategy.calculate(input));
            assertTrue(Float.isFinite(result.getStartPosition()));
            for (TimelinePathCommand command : result.getCompleted()) assertFinite(command);
            for (TimelinePathCommand command : result.getRemaining()) assertFinite(command);
        }
    }

    @Test
    public void populatedInputsPreserveLayoutAndMeasuredWidths() {
        for (TimelineMathConfig.StartPosition start : TimelineMathConfig.StartPosition.values()) {
            for (int width : new int[] {0, 1, 320}) {
                for (int progress : new int[] {0, 1, 99, 100}) {
                    TimelineStepData step = new TimelineStepData("Title", "Description", null, null, null, null, progress, null, null);
                    List<TimelineStepData> steps = Arrays.asList(step, step);
                    TimelineMathConfig config = new TimelineMathConfig(start, steps,
                        new TimelineMathConfig.Spacing(), new TimelineMathConfig.Sizes(),
                        TimelineMathConfig.ProgressMode.SEQUENTIAL, TimelineMathConfig.HorizontalLayout.FIT, 160f, 0.001);
                    TimelineGeometryInput initial = new TimelineGeometryInput(config, width, 1000f, Collections.emptyList());
                    TimelineGeometryResult result = strategy.calculate(initial);
                    assertEquals(result, strategy.calculate(initial));
                    assertEquals(steps.size(), result.getLayout().getSteps().size());
                    TimelineGeometryBounds bounds = result.getBounds();
                    assertTrue(Float.isFinite(bounds.getLeft()) && Float.isFinite(bounds.getTop())
                        && Float.isFinite(bounds.getRight()) && Float.isFinite(bounds.getBottom()));
                    assertTrue(bounds.getRight() >= bounds.getLeft() && bounds.getBottom() >= bounds.getTop());
                    TimelineGeometryResult measured = strategy.calculate(new TimelineGeometryInput(config, width, 1000f,
                        Arrays.asList(new TimelineTextMetrics(40, 80), new TimelineTextMetrics(20, 40))));
                    assertEquals(steps.size(), measured.getLayout().getSteps().size());
                    for (int index = 0; index < steps.size(); index++) {
                        TimelineGeometryStep before = result.getLayout().getSteps().get(index);
                        TimelineGeometryStep after = measured.getLayout().getSteps().get(index);
                        assertEquals(steps.get(index), after.getStep());
                        assertEquals(before.getTitleWidth(), after.getTitleWidth());
                        assertEquals(before.getDescriptionWidth(), after.getDescriptionWidth());
                        assertTrue(Float.isFinite(after.getIconX()) && Float.isFinite(after.getIconY()));
                        assertTrue(after.getTitleWidth() > 0 && after.getDescriptionWidth() > 0);
                    }
                    for (TimelinePathCommand command : measured.getCompleted()) assertFinite(command);
                    for (TimelinePathCommand command : measured.getRemaining()) assertFinite(command);
                }
            }
        }
    }

    private void assertFinite(TimelinePathCommand command) {
        if (command instanceof TimelinePathCommand.Move) {
            TimelinePathCommand.Move move = (TimelinePathCommand.Move) command;
            assertTrue(Float.isFinite(move.getX()) && Float.isFinite(move.getY()));
        } else if (command instanceof TimelinePathCommand.Line) {
            TimelinePathCommand.Line line = (TimelinePathCommand.Line) command;
            assertTrue(Float.isFinite(line.getX()) && Float.isFinite(line.getY()));
        } else if (command instanceof TimelinePathCommand.Quad) {
            TimelinePathCommand.Quad quad = (TimelinePathCommand.Quad) command;
            assertTrue(Float.isFinite(quad.getX()) && Float.isFinite(quad.getY())
                && Float.isFinite(quad.getControlX()) && Float.isFinite(quad.getControlY()));
        }
    }
}
