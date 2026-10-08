package com.example.feedblocker

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

/**
 * Счётчик «экранного времени» — времени, проведённого в ленте.
 *
 * Местный аналог «localStorage» на Android — SharedPreferences
 * (постоянное хранилище на устройстве, переживает перезагрузку).
 *
 * Ведёт четыре периода: день, неделя, месяц, год. Накопление происходит
 * только одним действием: когда пользователь включает ленту на 5 минут,
 * к счётчику добавляется ровно 5 минут (PauseController.start).
 *
 * Сброс — строгий, по смене ключа периода в местном времени (без
 * фоновых процессов; при первом чтении/записи после границы значение
 * уже считается нулевым):
 *  - день   — ключ "2025-03-10"    → сброс каждую ночь в 0:00;
 *  - неделя — ключ "2025-W10"      → сброс в ночь вс→пн в 0:00
 *    (ISO-неделя начинается с понедельника);
 *  - месяц  — ключ "2025-03"       → сброс 1-го числа в 0:00;
 *  - год    — ключ "2025"          → сброс 1 января в 0:00.
 */
object ScreenTimeCounter {

    private const val PREFS_NAME = "screen_time_prefs"

    /** Снимок значений счётчиков по всем четырём периодам. */
    data class Snapshot(
        val dayMillis: Long,
        val weekMillis: Long,
        val monthMillis: Long,
        val yearMillis: Long
    )

    private enum class Period(
        val valueKey: String,
        val periodKey: String,
        private val keyFor: (LocalDate) -> String
    ) {
        DAY("st_day_value", "st_day_period", { it.toString() }),
        WEEK("st_week_value", "st_week_period", { weekKey(it) }),
        MONTH("st_month_value", "st_month_period", { it.format(monthFormatter) }),
        YEAR("st_year_value", "st_year_period", { it.year.toString() });

        fun currentKey(date: LocalDate): String = keyFor(date)
    }

    /**
     * Добавить время к счётчикам всех периодов. Если период уже сменился
     * (ключ не совпадает с текущим) — старое значение отбрасывается
     * («строгий сброс») и накопление начинается с переданного значения.
     */
    fun add(context: Context, millis: Long) {
        val date = LocalDate.now()
        val prefs = prefs(context)
        val editor = prefs.edit()
        for (period in Period.entries) {
            val current = period.currentKey(date)
            val stored = prefs.getString(period.periodKey, null)
            val base = if (stored == current) prefs.getLong(period.valueKey, 0L) else 0L
            editor.putString(period.periodKey, current)
            editor.putLong(period.valueKey, base + millis)
        }
        editor.apply()
    }

    /** Текущие значения счётчиков с учётом сброса по смене периода. */
    fun snapshot(context: Context): Snapshot {
        val date = LocalDate.now()
        val prefs = prefs(context)

        fun valueFor(period: Period): Long {
            val stored = prefs.getString(period.periodKey, null)
            return if (stored == period.currentKey(date)) {
                prefs.getLong(period.valueKey, 0L)
            } else {
                0L
            }
        }

        return Snapshot(
            dayMillis = valueFor(Period.DAY),
            weekMillis = valueFor(Period.WEEK),
            monthMillis = valueFor(Period.MONTH),
            yearMillis = valueFor(Period.YEAR)
        )
    }

    /**
     * Красивый вывод в часах и минутах, как на системных экранах Apple:
     * "5 ч", "1 ч 25 мин", "45 мин", "0 мин".
     */
    fun format(ms: Long): String {
        val totalMin = (ms / 60_000L).coerceAtLeast(0L)
        val h = totalMin / 60L
        val m = totalMin % 60L
        return when {
            h > 0L && m > 0L -> "%d ч %d мин".format(h, m)
            h > 0L -> "%d ч".format(h)
            m > 0L -> "%d мин".format(m)
            else -> "0 мин"
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val monthFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM")

    /** ISO-ключ недели, начинающейся с понедельника: "2025-W10". */
    private fun weekKey(date: LocalDate): String {
        val isoWeek = WeekFields.ISO
        val weekBasedYear = date.get(isoWeek.weekBasedYear())
        val week = date.get(isoWeek.weekOfWeekBasedYear())
        return "%d-W%02d".format(weekBasedYear, week)
    }
}
