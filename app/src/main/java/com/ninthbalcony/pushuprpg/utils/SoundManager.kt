package com.ninthbalcony.pushuprpg.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * Звуковые файлы (OGG) в app/src/main/res/raw/:
 *   sound_punch.ogg, sound_save.ogg, sound_merge.ogg, sound_enchant.ogg
 *   sound_spin.ogg        — кнопка Spin
 *   sound_spin_loop.ogg   — ~7 сек, зацикливается во время анимации ленты
 *   music_main.ogg, music_shop.ogg, music_levelup.ogg (~3-4 сек, фанфар)
 */
object SoundManager {

    private var soundPool: SoundPool? = null
    private var soundPunch = 0
    private var soundSave = 0
    private var soundMerge = 0
    private var soundEnchant = 0
    private var soundSpin = 0
    private var soundSpinLoop = 0
    private var spinLoopStreamId = 0

    private var mediaPlayer: MediaPlayer? = null
    private var currentMusicName: String? = null
    private var initialized = false

    private val fadeHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var fadeRunnable: Runnable? = null
    private val targetVolume = 0.35f

    private fun cancelFade() {
        fadeRunnable?.let { fadeHandler.removeCallbacks(it) }
        fadeRunnable = null
    }

    fun pauseWithFade() {
        cancelFade()
        val mp = mediaPlayer ?: return
        var vol = targetVolume
        val step = targetVolume / 10
        val runnable = object : Runnable {
            override fun run() {
                vol -= step
                if (vol <= 0f) {
                    mp.setVolume(0f, 0f)
                    if (mp.isPlaying) mp.pause()
                } else {
                    mp.setVolume(vol, vol)
                    fadeHandler.postDelayed(this, 30)
                }
            }
        }
        fadeRunnable = runnable
        fadeHandler.post(runnable)
    }

    fun resumeWithFade() {
        cancelFade()
        val mp = mediaPlayer ?: return
        mp.setVolume(0f, 0f)
        if (!mp.isPlaying) mp.start()
        var vol = 0f
        val step = targetVolume / 10
        val runnable = object : Runnable {
            override fun run() {
                vol += step
                if (vol >= targetVolume) {
                    mp.setVolume(targetVolume, targetVolume)
                } else {
                    mp.setVolume(vol, vol)
                    fadeHandler.postDelayed(this, 30)
                }
            }
        }
        fadeRunnable = runnable
        fadeHandler.post(runnable)
    }

    fun init(context: Context) {
        if (initialized) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build()

        loadSound(context, "sound_punch")     { soundPunch     = it }
        loadSound(context, "sound_save")      { soundSave      = it }
        loadSound(context, "sound_merge")     { soundMerge     = it }
        loadSound(context, "sound_enchant")   { soundEnchant   = it }
        loadSound(context, "sound_spin")      { soundSpin      = it }
        loadSound(context, "sound_spin_loop") { soundSpinLoop  = it }
        initialized = true
    }

    private fun loadSound(context: Context, name: String, onLoaded: (Int) -> Unit) {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId != 0) onLoaded(soundPool!!.load(context, resId, 1))
    }

    fun playPunch(enabled: Boolean) {
        if (!enabled || soundPunch == 0) return
        soundPool?.play(soundPunch, 1f, 1f, 0, 0, 1f)
    }

    fun playSave(enabled: Boolean) {
        if (!enabled || soundSave == 0) return
        soundPool?.play(soundSave, 1f, 1f, 0, 0, 1f)
    }

    fun playMerge(enabled: Boolean) {
        if (!enabled || soundMerge == 0) return
        soundPool?.play(soundMerge, 1f, 1f, 0, 0, 1f)
    }

    fun playEnchant(enabled: Boolean) {
        if (!enabled || soundEnchant == 0) return
        soundPool?.play(soundEnchant, 1f, 1f, 0, 0, 1f)
    }

    fun playSpin(enabled: Boolean) {
        if (!enabled || soundSpin == 0) return
        soundPool?.play(soundSpin, 1f, 1f, 0, 0, 1f)
    }

    fun playSpinLoop(enabled: Boolean) {
        stopSpinLoop()
        if (!enabled || soundSpinLoop == 0) return
        spinLoopStreamId = soundPool?.play(soundSpinLoop, 0.8f, 0.8f, 1, -1, 1f) ?: 0
    }

    fun stopSpinLoop() {
        if (spinLoopStreamId != 0) {
            soundPool?.stop(spinLoopStreamId)
            spinLoopStreamId = 0
        }
    }

    /** Воспроизводит короткий фанфар level-up поверх основной музыки (без замены BGM). */
    fun playLevelUp(context: Context, enabled: Boolean) {
        if (!enabled) return
        val resId = context.resources.getIdentifier("music_levelup", "raw", context.packageName)
        if (resId == 0) return
        val fanfare = MediaPlayer.create(context, resId) ?: return
        fanfare.setVolume(0.75f, 0.75f)
        fanfare.start()
        fanfare.setOnCompletionListener { it.release() }
    }

    fun playMusic(context: Context, name: String, enabled: Boolean) {
        if (!enabled) { stopMusic(); return }
        if (currentMusicName == name && mediaPlayer?.isPlaying == true) return
        stopMusic()
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId == 0) return
        mediaPlayer = MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            setVolume(targetVolume, targetVolume)
            start()
        }
        currentMusicName = name
    }

    fun stopMusic() {
        cancelFade()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentMusicName = null
    }

    fun release() {
        cancelFade()
        stopSpinLoop()
        soundPool?.release()
        soundPool = null
        stopMusic()
        initialized = false
    }
}
