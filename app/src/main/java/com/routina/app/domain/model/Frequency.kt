package com.routina.app.domain.model

/** The interval between scheduled routine dates, expressed in calendar days. */
sealed interface Frequency {
    val intervalDays: Long

    data object Daily : Frequency {
        override val intervalDays: Long = 1
    }

    data object EveryThreeDays : Frequency {
        override val intervalDays: Long = 3
    }

    data object Weekly : Frequency {
        override val intervalDays: Long = 7
    }

    data class EveryDays(override val intervalDays: Long) : Frequency {
        init {
            require(intervalDays >= 1) { "intervalDays must be at least 1" }
        }
    }
}
