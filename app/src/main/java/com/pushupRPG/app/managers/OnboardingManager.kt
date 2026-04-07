package com.pushupRPG.app.managers

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingManager {
    companion object {
        private const val TAG = "OnboardingManager"

        // Onboarding steps
        const val STEP_TOTAL_PUSHUPS = 0
        const val STEP_INCREMENT_BUTTONS = 1
        const val STEP_SAVE_BUTTON = 2
        const val STEP_LOGS = 3
        const val STEP_QUESTS = 4
        const val STEP_INVENTORY = 5
        const val STEP_SHOP = 6
        const val TOTAL_STEPS = 7
    }

    private val _currentStep = MutableStateFlow(STEP_TOTAL_PUSHUPS)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _isOnboardingComplete = MutableStateFlow(false)
    val isOnboardingComplete: StateFlow<Boolean> = _isOnboardingComplete.asStateFlow()

    fun startOnboarding() {
        Log.d(TAG, "Starting onboarding flow")
        _currentStep.value = STEP_TOTAL_PUSHUPS
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

    fun skipToStep(step: Int) {
        if (step in 0 until TOTAL_STEPS) {
            _currentStep.value = step
            Log.d(TAG, "Jumped to step $step")
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

    fun getStepTitle(step: Int): String {
        return when (step) {
            STEP_TOTAL_PUSHUPS -> "Total Push-ups"
            STEP_INCREMENT_BUTTONS -> "Increment Buttons"
            STEP_SAVE_BUTTON -> "Save Your Progress"
            STEP_LOGS -> "View Your Logs"
            STEP_QUESTS -> "Complete Quests"
            STEP_INVENTORY -> "Manage Inventory"
            STEP_SHOP -> "Visit the Shop"
            else -> "Unknown Step"
        }
    }

    fun getStepDescription(step: Int): String {
        return when (step) {
            STEP_TOTAL_PUSHUPS -> "This shows your total push-ups. Keep improving!"
            STEP_INCREMENT_BUTTONS -> "Use +1 for single push-ups or +10 for multiple. Choose what's fastest for you!"
            STEP_SAVE_BUTTON -> "Press SAVE to record your progress. This also stores your data in the cloud."
            STEP_LOGS -> "Check your logs to track your push-up history and stats over time."
            STEP_QUESTS -> "Complete daily and weekly quests to earn rewards and level up!"
            STEP_INVENTORY -> "Collect items and manage your equipment here."
            STEP_SHOP -> "Spend your earned currency to buy new items and upgrades."
            else -> ""
        }
    }

    fun reset() {
        Log.d(TAG, "Resetting onboarding state")
        _currentStep.value = STEP_TOTAL_PUSHUPS
        _isOnboardingComplete.value = false
    }
}
