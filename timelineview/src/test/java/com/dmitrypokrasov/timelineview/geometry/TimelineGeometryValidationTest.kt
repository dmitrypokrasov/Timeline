package com.dmitrypokrasov.timelineview.geometry

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.testing.TimelineGeometryContractTest
import org.junit.Assert.assertThrows
import org.junit.Test

class TimelineGeometryValidationTest {
    private val strategy =
        TimelineGeometryStrategy { input ->
            val steps = input.config.steps.map { TimelineGeometryStep(it, 0f, 0f, 1, 0f, 0f, 1, 0f, 0f, TimelineTextAlignment.LEFT) }
            TimelineGeometryResult(TimelineGeometryLayout(steps, null, null), emptyList(), emptyList(), TimelineGeometryBounds(0f, 0f, 1f, 1f))
        }

    @Test
    fun `contract fixture accepts valid strategy and detects intentionally broken strategies`() {
        val contract = object : TimelineGeometryContractTest(strategy) {}
        contract.deterministicEmptyAndNarrowInputs()
        contract.populatedInputsPreserveLayoutAndMeasuredWidths()
        contract.singletonAndLegacyDuplicatesWithLargeIcons()
        val broken =
            listOf<(TimelineGeometryResult) -> TimelineGeometryResult>(
                { it.copy(layout = it.layout.copy(steps = it.layout.steps.reversed())) },
                { it.copy(layout = it.layout.copy(progressStepIndex = -1)) },
                { it.copy(layout = it.layout.copy(steps = it.layout.steps.map { step -> step.copy(descriptionY = Float.NaN) })) },
                { it.copy(remaining = listOf(TimelinePathCommand.Line(0f, 0f))) },
                { it.copy(bounds = TimelineGeometryBounds(1f, 0f, 0f, 1f)) },
            )
        broken.forEach { corrupt ->
            val invalid = object : TimelineGeometryContractTest(TimelineGeometryStrategy { corrupt(strategy.calculate(it)) }) {}
            assertThrows(AssertionError::class.java) { invalid.populatedInputsPreserveLayoutAndMeasuredWidths() }
        }
    }

    @Test
    fun `runtime validator rejects malformed results before Android adaptation`() {
        val input = TimelineGeometryInput(TimelineMathConfig(steps = listOf(TimelineStepData(progress = 0, id = "one"))), 10)
        val valid = strategy.calculate(input)
        val invalid =
            listOf(
                valid.copy(layout = valid.layout.copy(steps = emptyList())),
                valid.copy(layout = valid.layout.copy(progressStepIndex = 1)),
                valid.copy(layout = valid.layout.copy(progressIcon = TimelineGeometryProgressIcon(0f, Float.NaN))),
                valid.copy(layout = valid.layout.copy(steps = valid.layout.steps.map { it.copy(titleWidth = 0) })),
                valid.copy(layout = valid.layout.copy(steps = valid.layout.steps.map { it.copy(descriptionWidth = 0) })),
                valid.copy(layout = valid.layout.copy(steps = valid.layout.steps.map { it.copy(iconY = Float.POSITIVE_INFINITY) })),
                valid.copy(completed = listOf(TimelinePathCommand.Line(0f, 0f))),
                valid.copy(remaining = listOf(TimelinePathCommand.Move(0f, 0f), TimelinePathCommand.Quad(0f, Float.NaN, 0f, 0f))),
                valid.copy(bounds = TimelineGeometryBounds(0f, 1f, 0f, 0f)),
            )
        invalid.forEach { assertThrows(IllegalArgumentException::class.java) { TimelineGeometryValidation.validate(input, it, null) } }
    }
}
