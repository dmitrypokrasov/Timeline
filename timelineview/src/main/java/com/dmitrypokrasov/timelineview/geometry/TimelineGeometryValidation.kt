package com.dmitrypokrasov.timelineview.geometry

/** Проверяет границу стратегии до попадания результата в кэш или Android Path. */
internal object TimelineGeometryValidation {
    fun validate(
        input: TimelineGeometryInput,
        result: TimelineGeometryResult,
        initial: TimelineGeometryResult?,
    ) {
        val steps = result.layout.steps
        require(steps.size == input.config.steps.size) { "layout.steps.size must match config.steps.size" }
        finite("startPosition", result.startPosition)
        with(result.bounds) {
            finite("bounds.left", left)
            finite("bounds.top", top)
            finite("bounds.right", right)
            finite("bounds.bottom", bottom)
            require(left <= right && top <= bottom) { "bounds must be ordered" }
        }
        result.layout.progressStepIndex?.let {
            require(it in steps.indices) { "layout.progressStepIndex must address a step" }
        }
        result.layout.progressIcon?.let {
            finite("layout.progressIcon.left", it.left)
            finite("layout.progressIcon.top", it.top)
        }
        steps.forEachIndexed { index, step ->
            val field = "layout.steps[$index]"
            require(step.step == input.config.steps[index]) { "$field.step must preserve input order and data" }
            require(step.titleWidth > 0) { "$field.titleWidth must be positive" }
            require(step.descriptionWidth > 0) { "$field.descriptionWidth must be positive" }
            finite("$field.titleX", step.titleX)
            finite("$field.titleY", step.titleY)
            finite("$field.descriptionX", step.descriptionX)
            finite("$field.descriptionY", step.descriptionY)
            finite("$field.iconX", step.iconX)
            finite("$field.iconY", step.iconY)
            initial?.layout?.steps?.get(index)?.let { before ->
                require(step.titleWidth == before.titleWidth) { "$field.titleWidth changed between text passes" }
                require(step.descriptionWidth == before.descriptionWidth) { "$field.descriptionWidth changed between text passes" }
            }
        }
        commands("completed", result.completed)
        commands("remaining", result.remaining)
    }

    private fun commands(
        field: String,
        commands: List<TimelinePathCommand>,
    ) {
        require(commands.isEmpty() || commands.first() is TimelinePathCommand.Move) { "$field[0] must be Move" }
        commands.forEachIndexed { index, command ->
            when (command) {
                is TimelinePathCommand.Move -> {
                    finite("$field[$index].x", command.x)
                    finite("$field[$index].y", command.y)
                }
                is TimelinePathCommand.Line -> {
                    finite("$field[$index].x", command.x)
                    finite("$field[$index].y", command.y)
                }
                is TimelinePathCommand.Quad -> {
                    finite("$field[$index].x", command.x)
                    finite("$field[$index].y", command.y)
                    finite("$field[$index].controlX", command.controlX)
                    finite("$field[$index].controlY", command.controlY)
                }
            }
        }
    }

    private fun finite(
        field: String,
        value: Float,
    ) {
        require(value.isFinite()) { "$field must be finite" }
    }
}
