package com.dmitrypokrasov.timeline

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.render.SnakeTimelineUi
import com.dmitrypokrasov.timelineview.strategy.TimelineMathProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistryContract
import com.dmitrypokrasov.timelineview.strategy.TimelineUiProvider
import com.dmitrypokrasov.timelineview.ui.TimelineView

class TimelineSampleFragment : Fragment() {
    private var steps: List<TimelineStepData> = emptyList()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("timeline_progress", steps.map { it.progress }.toIntArray())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_timeline_sample, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        val timelineView = view.findViewById<TimelineView>(R.id.timeline)
        val sample = requireSample()

        val savedProgress = savedInstanceState?.getIntArray("timeline_progress")
        steps =
            TimelineSampleData.buildSteps(requireContext()).mapIndexed { index, step ->
                step.copy(id = "sample-$index", timestampMillis = index.toLong() * index * 60_000L, progress = savedProgress?.getOrNull(index)?.coerceIn(0, 100) ?: step.progress)
            }
        val mathConfig = TimelineSampleData.buildMathConfig(requireContext(), steps)
        val uiConfig = TimelineSampleData.buildUiConfig(requireContext())

        timelineView.replaceSteps(steps)

        timelineView.setOnStepClickListener { index, _ ->
            steps =
                steps.mapIndexed { stepIndex, step ->
                    if (stepIndex != index) {
                        step
                    } else {
                        val updatedProgress = (step.progress + 10).coerceAtMost(100)
                        val justCompleted = step.progress < 100 && updatedProgress == 100
                        step.copy(
                            progress = updatedProgress,
                            badgeAnimation =
                                if (justCompleted) {
                                    TimelineSampleData.buildCompletionBadgeAnimation()
                                } else {
                                    step.badgeAnimation
                                },
                        )
                    }
                }
            timelineView.replaceSteps(steps)
        }
        timelineView.setOnProgressIconClickListener {
            val progressIndex = resolveProgressStepIndex(steps)
            Toast.makeText(requireContext(), "Progress index: $progressIndex", Toast.LENGTH_SHORT)
                .show()
        }

        when (sample) {
            TimelineSample.ALTERNATING, TimelineSample.ADAPTIVE_GRID, TimelineSample.TIME_SCALED -> {
                val strategy =
                    when (sample) {
                        TimelineSample.ALTERNATING -> com.dmitrypokrasov.timelineview.config.TimelineMathStrategy.Alternating
                        TimelineSample.ADAPTIVE_GRID -> com.dmitrypokrasov.timelineview.config.TimelineMathStrategy.AdaptiveGrid
                        else -> com.dmitrypokrasov.timelineview.config.TimelineMathStrategy.TimeScaled
                    }
                timelineView.setConfig(mathConfig.copy(minCellWidth = 160f * resources.displayMetrics.density), uiConfig)
                timelineView.setStrategy(strategy, com.dmitrypokrasov.timelineview.config.TimelineUiStrategy.Linear)
            }

            TimelineSample.SNAKE -> {
                timelineView.setMathEngine(SnakeTimelineMath(mathConfig))
                timelineView.setUiRenderer(SnakeTimelineUi(uiConfig))
            }

            TimelineSample.LINEAR_VERTICAL -> {
                timelineView.setMathEngine(
                    LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.VERTICAL),
                )
                timelineView.setUiRenderer(LinearTimelineUi(uiConfig))
            }

            TimelineSample.LINEAR_HORIZONTAL -> {
                timelineView.setMathEngine(
                    LinearTimelineMath(mathConfig.copy(horizontalLayout = TimelineMathConfig.HorizontalLayout.WRAP, minCellWidth = 160f * resources.displayMetrics.density), LinearTimelineMath.Orientation.HORIZONTAL),
                )
                timelineView.setUiRenderer(LinearTimelineUi(uiConfig))
            }

            TimelineSample.MIXED -> {
                timelineView.setMathEngine(
                    LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.VERTICAL),
                )
                timelineView.setUiRenderer(SnakeTimelineUi(uiConfig))
            }

            TimelineSample.CUSTOM_REGISTRY -> {
                timelineView.setStrategyRegistry {
                    registerCustomStrategies(this)
                }
                timelineView.setMathEngine(SnakeTimelineMath(mathConfig))
                timelineView.setUiRenderer(LinearTimelineUi(uiConfig))
                timelineView.setStrategy(
                    mathStrategyKey = StrategyKey(CUSTOM_MATH_ID),
                    uiStrategyKey = StrategyKey(CUSTOM_UI_ID),
                )
            }
        }
    }

    private fun resolveProgressStepIndex(steps: List<TimelineStepData>): Int {
        return steps.indexOfFirst { it.progress < 100 }
    }

    private fun registerCustomStrategies(registry: TimelineStrategyRegistryContract) {
        registry.registerMath(
            object : TimelineMathProvider {
                override val key: StrategyKey = StrategyKey(CUSTOM_MATH_ID)

                override fun create(config: TimelineMathConfig) = SnakeTimelineMath(config)
            },
        )
        registry.registerUi(
            object : TimelineUiProvider {
                override val key: StrategyKey = StrategyKey(CUSTOM_UI_ID)

                override fun create(config: TimelineUiConfig) = LinearTimelineUi(config)
            },
        )
    }

    @Suppress("DEPRECATION")
    private fun requireSample(): TimelineSample {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireNotNull(
                requireArguments().getSerializable(ARG_SAMPLE, TimelineSample::class.java),
            )
        } else {
            requireNotNull(requireArguments().getSerializable(ARG_SAMPLE) as? TimelineSample)
        }
    }

    companion object {
        private const val ARG_SAMPLE = "sample"
        private const val CUSTOM_MATH_ID = "custom_snake_math"
        private const val CUSTOM_UI_ID = "custom_linear_ui"

        fun newInstance(sample: TimelineSample): TimelineSampleFragment {
            return TimelineSampleFragment().apply {
                arguments =
                    Bundle().apply {
                        putSerializable(ARG_SAMPLE, sample)
                    }
            }
        }
    }
}
