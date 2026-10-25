package com.dmitrypokrasov.timelineview.strategy

/** Гарантии детерминизма provider, фиксируемые при разрешении пары стратегий. */
enum class TimelineCachePolicy {
    /** Все измерительные callbacks выполняются при каждом измерении. */
    NONE,

    /** Одинаковые конфигурация, ширина, RTL и ресурсы разрешают повторное использование frame. */
    CONFIGURATION,

    /** Изменение только прогресса также разрешает повторное использование измеренного текста. */
    CONFIGURATION_WITH_PROGRESS_REUSE,
}

/** Необязательный opt-in provider; оба участника пары должны разрешить кэширование. */
interface TimelineCachePolicyProvider {
    /** Provider гарантирует политику для всех создаваемых экземпляров; значение снимается до create. */
    val cachePolicy: TimelineCachePolicy
}

/** Результат разрешения с уже снятой политикой; прямые экземпляры всегда консервативны. */
internal data class TimelineResolvedStrategy<T>(val instance: T, val policy: TimelineCachePolicy)

internal data class TimelineResolvedStrategies(
    val math: com.dmitrypokrasov.timelineview.math.TimelineMathEngine,
    val ui: com.dmitrypokrasov.timelineview.render.TimelineUiRenderer,
    val policy: TimelineCachePolicy,
)
