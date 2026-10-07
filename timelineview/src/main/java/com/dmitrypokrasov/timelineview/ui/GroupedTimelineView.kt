package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.text.Spanned
import android.text.SpannedString
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.core.view.ViewCompat
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineConfigParser
import com.dmitrypokrasov.timelineview.math.TimelineMathFactory
import com.dmitrypokrasov.timelineview.model.TimelineSection
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import kotlin.math.roundToInt

/**
 * Bounded groups of built-in timelines, each with an accessible heading and independent progress.
 * The host owns grouping, date formatting/time zones and persistence. Use a recycling container
 * for unbounded feeds. Existing section views are retained by ID during updates.
 */
@MainThread
class GroupedTimelineView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {
        private data class SectionViews(val container: LinearLayout, val heading: TextView, val timeline: TimelineView)

        private var config = TimelineConfigParser(context).parse(attrs, defStyleAttr)
        private var sections = emptyList<TimelineSection>()
        private val views = linkedMapOf<String, SectionViews>()
        private var listener: ((sectionId: String, index: Int, step: TimelineStepData) -> Unit)? = null

        init {
            orientation = VERTICAL
        }

        /** Returns a snapshot of the displayed sections; date grouping is never inferred. */
        fun getSections(): List<TimelineSection> = snapshot(sections)

        /**
         * Installs ordered sections with unique section IDs and a shared built-in configuration.
         * [configuration]'s steps are replaced by each section's steps. Empty sections retain headings.
         * Repeating a step ID in different sections is allowed. Custom strategy keys are not supported.
         */
        fun setSections(
            sections: List<TimelineSection>,
            configuration: TimelineConfig,
        ) {
            require(configuration.mathStrategyKey == null && configuration.uiStrategyKey == null) { "GroupedTimelineView supports built-in strategies; use separate TimelineViews for custom registries" }
            require(sections.map { it.id }.distinct().size == sections.size) { "Section IDs must be unique" }
            val candidate = snapshot(sections)
            val configs = candidate.map { configuration.copy(math = configuration.math.copy(steps = it.steps)) }
            // Validate every group's math (including sorted timestamps) before changing the view tree.
            configs.forEach { TimelineMathFactory.create(it.mathStrategy, it.math) }
            val desired = candidate.map { section -> views[section.id] ?: createSection(section.id) }
            desired.forEachIndexed { index, sectionViews ->
                val section = candidate[index]
                val next = configs[index]
                val previous = sectionViews.timeline.getConfig()
                if (previous != next) {
                    if (previous.copy(math = previous.math.copy(steps = next.math.steps)) == next) {
                        sectionViews.timeline.replaceSteps(next.math.steps)
                    } else {
                        sectionViews.timeline.setConfig(next)
                    }
                }
                sectionViews.heading.text = section.title
                sectionViews.heading.setTextColor(next.ui.colors.colorTitle)
                sectionViews.heading.setTextSize(TypedValue.COMPLEX_UNIT_PX, next.ui.textSizes.sizeTitle)
                bindListener(section.id, sectionViews.timeline)
            }
            val ids = candidate.map { it.id }.toSet()
            views.keys.filter { it !in ids }.forEach { id -> removeView(views.remove(id)?.container) }
            desired.forEachIndexed { index, sectionViews ->
                if (getChildAt(index) !== sectionViews.container) {
                    removeView(sectionViews.container)
                    addView(sectionViews.container, index)
                }
                views[candidate[index].id] = sectionViews
            }
            this.sections = candidate
            config = configuration.copy(math = configuration.math.copy(steps = emptyList()))
        }

        /** Updates sections while retaining the last shared configuration. */
        fun replaceSections(sections: List<TimelineSection>) = setSections(sections, config)

        /** Clicks identify both the section and the step's current index within that section. */
        fun setOnStepClickListener(listener: ((sectionId: String, index: Int, step: TimelineStepData) -> Unit)?) {
            this.listener = listener
            views.forEach { (id, sectionViews) -> bindListener(id, sectionViews.timeline) }
        }

        private fun bindListener(
            id: String,
            timeline: TimelineView,
        ) {
            timeline.setOnStepClickListener(if (listener == null) null else { index, step -> listener?.invoke(id, index, step) })
        }

        private fun createSection(id: String): SectionViews {
            val heading =
                TextView(context).apply {
                    ViewCompat.setAccessibilityHeading(this, true)
                    val gap = (8f * resources.displayMetrics.density).roundToInt()
                    setPaddingRelative(0, gap, 0, gap)
                }
            val timeline =
                TimelineView(context).apply {
                    this.id = View.generateViewId()
                    setStrategyRegistry(TimelineStrategyRegistry.createLocalRegistry())
                }
            val container =
                LinearLayout(context).apply {
                    orientation = VERTICAL
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    addView(heading, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
                    addView(timeline, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
                }
            bindListener(id, timeline)
            return SectionViews(container, heading, timeline)
        }

        private fun text(value: CharSequence): CharSequence = if (value is Spanned) SpannedString(value) else value.toString()

        private fun snapshot(sections: List<TimelineSection>): List<TimelineSection> =
            sections.map { section ->
                section.copy(title = text(section.title), steps = section.steps.map { it.copy(title = it.title?.let(::text), description = it.description?.let(::text)) })
            }
    }
