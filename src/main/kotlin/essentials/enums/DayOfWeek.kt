package essentials.enums

enum class DayOfWeek(
    val isWeekend: Boolean,
    val dayName: String,
) {
    MONDAY(false, "Monday"),
    TUESDAY(false, "Tuesday"),
    WEDNESDAY(false, "Wednesday"),
    THURSDAY(false, "Thursday"),
    FRIDAY(false, "Friday"),
    SATURDAY(true, "Saturday"),
    SUNDAY(true, "Sunday");

    fun nextDay(): DayOfWeek {
        val days = DayOfWeek.entries //or DayOfWeek.values()
        val currentIndex = days.indexOf(this)
        val nextIndex = (currentIndex + 1) % days.size
        return days[nextIndex]
    }
}

fun main() {
    val friday: DayOfWeek = DayOfWeek.FRIDAY
    println(friday.dayName) // Friday
    println(friday.isWeekend) // false
    val saturday: DayOfWeek = friday.nextDay()
    println(saturday.dayName) // Saturday
    println(saturday.isWeekend) // true
}
