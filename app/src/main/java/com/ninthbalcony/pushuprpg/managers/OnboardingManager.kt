package com.ninthbalcony.pushuprpg.managers

import android.util.Log
import com.ninthbalcony.pushuprpg.utils.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingManager {
    companion object {
        private const val TAG = "OnboardingManager"

        // Onboarding steps — visual top-to-bottom order on MainMenuScreen
        const val STEP_BATTLE = 0               // BattleArena   (top)
        const val STEP_WELCOME = 1              // PushUpCounter
        const val STEP_SHOP = 2                 // Shop button (inside PushUpCounter)
        const val STEP_INVENTORY = 3            // StatsPanel / Inventory
        const val STEP_LOGS = 4                 // MiniLog
        const val STEP_QUESTS = 5               // QuestShortcutButton (bottom)
        const val TOTAL_STEPS = 6
    }

    private val _currentStep = MutableStateFlow(STEP_WELCOME)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _isOnboardingComplete = MutableStateFlow(false)
    val isOnboardingComplete: StateFlow<Boolean> = _isOnboardingComplete.asStateFlow()

    fun startOnboarding() {
        Log.d(TAG, "Starting onboarding flow")
        _currentStep.value = STEP_WELCOME
        _isOnboardingComplete.value = false
    }

    fun nextStep() {
        val nextStep = _currentStep.value + 1
        if (nextStep < TOTAL_STEPS) {
            _currentStep.value = nextStep
            Log.d(TAG, "Advanced to step $nextStep")
        } else {
            completeOnboarding()
        }
    }

    fun completeOnboarding() {
        Log.d(TAG, "Onboarding completed")
        _isOnboardingComplete.value = true
        _currentStep.value = TOTAL_STEPS
    }

    fun skipOnboarding() {
        Log.d(TAG, "Onboarding skipped by user")
        _isOnboardingComplete.value = true
        _currentStep.value = TOTAL_STEPS
    }

    fun getStepTitleKey(step: Int): String {
        return when (step) {
            STEP_BATTLE    -> "onboard_step_title_3"
            STEP_WELCOME   -> "onboard_step_title_0"
            STEP_SHOP      -> "onboard_step_title_2"
            STEP_INVENTORY -> "onboard_step_title_1"
            STEP_LOGS      -> "onboard_step_title_4"
            STEP_QUESTS    -> "onboard_step_title_5"
            else           -> "onboard_step_title_0"
        }
    }

    fun getStepDescriptionKey(step: Int): String {
        return when (step) {
            STEP_BATTLE    -> "onboard_step_desc_3"
            STEP_WELCOME   -> "onboard_step_desc_0"
            STEP_SHOP      -> "onboard_step_desc_2"
            STEP_INVENTORY -> "onboard_step_desc_1"
            STEP_LOGS      -> "onboard_step_desc_4"
            STEP_QUESTS    -> "onboard_step_desc_5"
            else           -> "onboard_step_desc_0"
        }
    }

    fun getStepTitle(step: Int, language: String): String {
        return AppStrings.t(language, getStepTitleKey(step))
    }

    fun getStepDescription(step: Int, language: String): String {
        return AppStrings.t(language, getStepDescriptionKey(step))
    }

    fun reset() {
        Log.d(TAG, "Resetting onboarding state")
        _currentStep.value = STEP_WELCOME
        _isOnboardingComplete.value = false
    }
}
