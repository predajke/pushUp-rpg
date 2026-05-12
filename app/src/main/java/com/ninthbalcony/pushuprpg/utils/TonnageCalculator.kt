package com.ninthbalcony.pushuprpg.utils

/**
 * Считает «поднятые тонны» и калории на базе массы тела игрока.
 *
 * Формулы:
 * - tonnage_kg = pushups × bodyWeightKg × 0.65
 *   (0.65 — стандартная доля массы тела, которую поднимают руки в обычном
 *   push-up'е; источник: ACE Fitness biomechanics paper, EMG analysis)
 * - calories = pushups × bodyWeightKg × 0.005
 *   (приближение: MET=4.5 для отжиманий средней интенсивности, темп ~1
 *   повтор в 2 секунды; формула kcal = MET × weight × hours)
 *
 * При [bodyWeightKg] == 0 возвращает 0 — UI должен показать prompt
 * «Введите вес в Settings» вместо метрик.
 */
object TonnageCalculator {

    private const val ARM_LOAD_FACTOR = 0.65f
    private const val KCAL_PER_PUSHUP_PER_KG = 0.005f

    /** Поднятая масса в килограммах. */
    fun tonnageKg(pushups: Int, bodyWeightKg: Float): Float {
        if (bodyWeightKg <= 0f || pushups <= 0) return 0f
        return pushups * bodyWeightKg * ARM_LOAD_FACTOR
    }

    /** Сожжённые калории (приблизительно). */
    fun calories(pushups: Int, bodyWeightKg: Float): Int {
        if (bodyWeightKg <= 0f || pushups <= 0) return 0
        return (pushups * bodyWeightKg * KCAL_PER_PUSHUP_PER_KG).toInt()
    }

    /**
     * Локализованное форматирование массы:
     *   < 1000 кг → "850 kg" / "850 кг"
     *   ≥ 1000 кг → "1.5 t" / "1.5 т" с одним знаком после запятой
     */
    fun formatMass(kg: Float, language: String): String {
        val ru = language == "ru"
        return if (kg >= 1000f) {
            val tons = kg / 1000f
            val formatted = "%.1f".format(tons).replace(',', '.')
            if (ru) "$formatted т" else "$formatted t"
        } else {
            val rounded = kg.toInt()
            if (ru) "$rounded кг" else "$rounded kg"
        }
    }
}
