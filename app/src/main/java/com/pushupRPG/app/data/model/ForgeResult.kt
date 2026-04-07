package com.pushupRPG.app.data.model

sealed class ForgeResult {
    data class Success(val item: Item) : ForgeResult()
    object Fail    : ForgeResult()   // оба предмета сгорают
    object NoItems : ForgeResult()   // слоты пусты — попап не нужен
}
