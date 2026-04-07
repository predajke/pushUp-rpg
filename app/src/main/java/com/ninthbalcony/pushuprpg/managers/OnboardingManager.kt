package com.ninthbalcony.pushuprpg.managers

import android.util.Log

/**
 * OnboardingManager controls the onboarding flow with TourGuide.
 *
 * Steps:
 * 1. Central total (total push-ups display)
 * 2. +1/+10 buttons
 * 3. SAVE button
 * 4. Battle logs
 * 5. Quests tab
 * 6. Inventory tab
 * 7. Shop tab
 */
class OnboardingManager {
    companion object {
        private const val TAG = "OnboardingManager"

        enum class OnboardingStep {
            STEP_1_TOTAL,
            STEP_2_BUTTONS,
            STEP_3_SAVE,
            STEP_4_LOGS,
            STEP_5_QUESTS,
            STEP_6_INVENTORY,
            STEP_7_SHOP,
            COMPLETED
        }
    }

    private var currentStep = OnboardingStep.STEP_1_TOTAL
    private var isOnboardingActive = true

    /**
     * Initialize onboarding (call when app starts).
     */
    fun initialize(isFirstLaunch: Boolean) {
        isOnboardingActive = isFirstLaunch
        currentStep = OnboardingStep.STEP_1_TOTAL
        Log.d(TAG, "Onboarding initialized: active=$isFirstLaunch")
    }

    /**
     * Get current onboarding step.
     */
    fun getCurrentStep(): OnboardingStep {
        return currentStep
    }

    /**
     * Move to next step.
     */
    fun nextStep() {
        currentStep = when (currentStep) {
            OnboardingStep.STEP_1_TOTAL -> OnboardingStep.STEP_2_BUTTONS
            OnboardingStep.STEP_2_BUTTONS -> OnboardingStep.STEP_3_SAVE
            OnboardingStep.STEP_3_SAVE -> OnboardingStep.STEP_4_LOGS
            OnboardingStep.STEP_4_LOGS -> OnboardingStep.STEP_5_QUESTS
            OnboardingStep.STEP_5_QUESTS -> OnboardingStep.STEP_6_INVENTORY
            OnboardingStep.STEP_6_INVENTORY -> OnboardingStep.STEP_7_SHOP
            OnboardingStep.STEP_7_SHOP -> {
                isOnboardingActive = false
                Log.d(TAG, "Onboarding completed")
                OnboardingStep.COMPLETED
            }
            OnboardingStep.COMPLETED -> OnboardingStep.COMPLETED
        }
        Log.d(TAG, "Step advanced to: $currentStep")
    }

    /**
     * Skip onboarding.
     */
    fun skipOnboarding() {
        isOnboardingActive = false
        currentStep = OnboardingStep.COMPLETED
        Log.d(TAG, "Onboarding skipped by user")
    }

    /**
     * Check if onboarding is active.
     */
    fun isActive(): Boolean {
        return isOnboardingActive
    }

    /**
     * Check if onboarding is completed.
     */
    fun isCompleted(): Boolean {
        return !isOnboardingActive && currentStep == OnboardingStep.COMPLETED
    }

    /**
     * Get step description for UI (English & Russian).
     */
    fun getStepDescription(): Pair<String, String> {
        return when (currentStep) {
            OnboardingStep.STEP_1_TOTAL -> Pair(
                "Your total push-ups counter. Tap + buttons to add push-ups.",
                "Ваш общий счетчик отжиманий. Нажимайте кнопки + для добавления отжиманий."
            )
            OnboardingStep.STEP_2_BUTTONS -> Pair(
                "+1 for single push-up, +10 for quick count.",
                "+1 для одного отжимания, +10 для быстрого счета."
            )
            OnboardingStep.STEP_3_SAVE -> Pair(
                "SAVE button stores your progress and gives rewards.",
                "Кнопка SAVE сохраняет прогресс и дает награды."
            )
            OnboardingStep.STEP_4_LOGS -> Pair(
                "Battle logs show your fight history.",
                "Логи боя показывают историю ваших боев."
            )
            OnboardingStep.STEP_5_QUESTS -> Pair(
                "Daily quests give extra rewards.",
                "Ежедневные квесты дают дополнительные награды."
            )
            OnboardingStep.STEP_6_INVENTORY -> Pair(
                "Inventory stores your items and equipment.",
                "Инвентарь хранит ваши предметы и экипировку."
            )
            OnboardingStep.STEP_7_SHOP -> Pair(
                "Shop has items, enchants, and forge combinations.",
                "Магазин содержит предметы, зачарования и комбинации кузницы."
            )
            OnboardingStep.COMPLETED -> Pair(
                "You're all set!",
                "Вы готовы играть!"
            )
        }
    }

    /**
     * Get progress percentage (0-100).
     */
    fun getProgressPercentage(): Int {
        return when (currentStep) {
            OnboardingStep.STEP_1_TOTAL -> 14
            OnboardingStep.STEP_2_BUTTONS -> 28
            OnboardingStep.STEP_3_SAVE -> 42
            OnboardingStep.STEP_4_LOGS -> 57
            OnboardingStep.STEP_5_QUESTS -> 71
            OnboardingStep.STEP_6_INVENTORY -> 85
            OnboardingStep.STEP_7_SHOP -> 100
            OnboardingStep.COMPLETED -> 100
        }
    }
}
