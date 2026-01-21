package com.dmitrypokrasov.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.render.SnakeTimelineUi
import com.dmitrypokrasov.timelineview.strategy.TimelineMathProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.TimelineUiProvider
import com.dmitrypokrasov.timelineview.ui.TimelineView

class TimelineSampleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_timeline_sample, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val timelineView = view.findViewById<TimelineView>(R.id.timeline)
        val sample = requireArguments().getSerializable(ARG_SAMPLE) as TimelineSample

        val steps = TimelineSampleData.buildSteps()
        val mathConfig = TimelineSampleData.buildMathConfig(requireContext(), steps)
        val uiConfig = TimelineSampleData.buildUiConfig(requireContext())

        when (sample) {
            TimelineSample.SNAKE -> {
                timelineView.setMathEngine(SnakeTimelineMath(mathConfig))
                timelineView.setUiRenderer(SnakeTimelineUi(uiConfig))
            }

            TimelineSample.LINEAR_VERTICAL -> {
                timelineView.setMathEngine(
                    LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.VERTICAL)
                )
                timelineView.setUiRenderer(LinearTimelineUi(uiConfig))
            }

            TimelineSample.LINEAR_HORIZONTAL -> {
                timelineView.setMathEngine(
                    LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.HORIZONTAL)
                )
                timelineView.setUiRenderer(LinearTimelineUi(uiConfig))
            }

            TimelineSample.MIXED -> {
                timelineView.setMathEngine(
                    LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.VERTICAL)
                )
                timelineView.setUiRenderer(SnakeTimelineUi(uiConfig))
            }

            TimelineSample.CUSTOM_REGISTRY -> {
                registerCustomStrategies()
                timelineView.setMathEngine(SnakeTimelineMath(mathConfig))
                timelineView.setUiRenderer(LinearTimelineUi(uiConfig))
                timelineView.setStrategy(
                    mathStrategyId = CUSTOM_MATH_ID,
                    uiStrategyId = CUSTOM_UI_ID
                )
            }
        }
    }

    private fun registerCustomStrategies() {
        TimelineStrategyRegistry.registerMath(object : TimelineMathProvider {
            override val id: String = CUSTOM_MATH_ID
            override fun create(config: TimelineMathConfig) = SnakeTimelineMath(config)
        })
        TimelineStrategyRegistry.registerUi(object : TimelineUiProvider {
            override val id: String = CUSTOM_UI_ID
            override fun create(config: TimelineUiConfig) = LinearTimelineUi(config)
        })
    }

    companion object {
        private const val ARG_SAMPLE = "sample"
        private const val CUSTOM_MATH_ID = "custom_snake_math"
        private const val CUSTOM_UI_ID = "custom_linear_ui"

        fun newInstance(sample: TimelineSample): TimelineSampleFragment {
            return TimelineSampleFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SAMPLE, sample)
                }
            }
        }
    }
}
