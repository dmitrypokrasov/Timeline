package com.dmitrypokrasov.timelineview.math

/** Optional geometry capability for connectors that pass above a row's labels. */
interface TimelineTextBoundary {
    /** Minimum title top in local coordinates, before stroke half-width and text clearance. */
    fun getTextTopBoundary(index: Int): Float
}
