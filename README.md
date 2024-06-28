# Timeline

## Quick Setup

### 1. Include library

**Using Gradle**

``` gradle
dependencies {
    implementation 'com.github.dmitrypokrasov:timelineview:x.x.x'
}
```

### 2. Usage

* In XML Layout :

``` java
    <com.sample.timelineview.TimeLineView
        android:id="@+id/timeline"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:timeline_description_size="12sp"
        app:timeline_disable_icon="@drawable/ic_tobacco_unactive"
        app:timeline_icon_progress_size="28dp"
        app:timeline_image_lvl_size="48dp"
        app:timeline_margin_horizontal_image="16dp"
        app:timeline_margin_horizontal_stroke="40dp"
        app:timeline_margin_horizontal_text="80dp"
        app:timeline_start_position="CENTER"
        app:timeline_margin_top_description="4dp"
        app:timeline_margin_top_progress_icon="6dp"
        app:timeline_margin_top_title="52dp"
        app:timeline_progress_icon="@drawable/ic_progress_time_line"
        app:timeline_radius_size="48dp"
        app:timeline_step_y_first_size="20dp"
        app:timeline_step_y_size="80dp"
        app:timeline_stroke_size="6dp"
        app:timeline_title_size="12sp"
        app:layout_constraintTop_toTopOf="parent" />
```

* Configure using xml attributes or setters in code:

    <table>
    <th>Attribute Name</th>
    <th>Default Value</th>
    <th>Description</th>
    <tr>
        <td>app:timeline_description_size="12sp"</td>
        <td>0dp</td>
        <td>sets description text size</td>
    </tr>
    </table>


* In onCreate():

``` java

     val timeLineView = findViewById<TimeLineView>(R.id.timeline)
        timeLineView.replaceLevels(
            ArrayList(
                listOf(
                    TimelineLevel(
                        title = R.string.title_1_lvl,
                        description = R.string.description_1_9_steps,
                        icon = R.drawable.ic_tobacco_active,
                        count = 9,
                        maxCount = 9
                    ), TimelineLevel(
                        title = R.string.title_2_lvl,
                        description = R.string.description_10_99_steps,
                        icon = R.drawable.ic_tobacco_active,
                        count = 50,
                        maxCount = 99
                    ), TimelineLevel(
                        title = R.string.title_3_lvl,
                        description = R.string.description_100_999_steps,
                        icon = R.drawable.ic_tobacco_active,
                        maxCount = 999
                    ), TimelineLevel(
                        title = R.string.title_4_lvl,
                        description = R.string.description_1000_9999_steps,
                        icon = R.drawable.ic_tobacco_active,
                        maxCount = 9999
                    ), TimelineLevel(
                        title = R.string.title_5_lvl,
                        description = R.string.description_10000_99999_steps,
                        icon = R.drawable.ic_tobacco_unactive,
                        maxCount = 99999
                    )
                )
            )
        )

```
