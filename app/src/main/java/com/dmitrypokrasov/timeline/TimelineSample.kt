package com.dmitrypokrasov.timeline

import androidx.annotation.StringRes

enum class TimelineSample(@StringRes val titleRes: Int) {
    SNAKE(R.string.tab_snake),
    LINEAR_VERTICAL(R.string.tab_linear_vertical),
    LINEAR_HORIZONTAL(R.string.tab_linear_horizontal),
    MIXED(R.string.tab_mixed),
    CUSTOM_REGISTRY(R.string.tab_custom_registry)
}
