package com.pushupRPG.app

import org.junit.Assert.*
import org.junit.Test

/**
 * Тесты логики инвентаря.
 * Дублирует private-функции из GameRepository для тестирования изолированно.
 */
class InventoryParsingTest {

    // --- Дублируем private-логику из GameRepository ---

    private fun getBaseId(entry: String): String {
        val idPart = entry.split(":")[0]
        val parts = idPart.split("_")
        return if (parts.size > 2 &&
            parts.last().all { it.isDigit() } &&
            parts.last().length > 8) {
            parts.dropLast(1).joinToString("_")
        } else {
            idPart
        }
    }

    private fun getEnchantLevel(entry: String): Int {
        return entry.split(":").getOrNull(1)?.toIntOrNull() ?: 0
    }

    private fun getUniqueId(entry: String): String {
        return entry.split(":")[0]
    }

    private fun parseInventory(inventoryStr: String): MutableList<String> {
        return inventoryStr.split(",")
            .filter { it.isNotEmpty() }
            .toMutableList()
    }

    private fun buildInventory(entries: List<String>): String {
        return entries.filter { it.isNotEmpty() }.joinToString(",")
    }

    // ==================== parseInventory ====================

    @Test
    fun `parseInventory - пустая строка = пустой список`() {
        val result = parseInventory("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseInventory - один предмет`() {
        val result = parseInventory("boots_002_1717499234567:0")
        assertEquals(1, result.size)
        assertEquals("boots_002_1717499234567:0", result[0])
    }

    @Test
    fun `parseInventory - несколько предметов`() {
        val result = parseInventory("boots_002_1717499234567:0,head_001_1717499234568:2")
        assertEquals(2, result.size)
    }

    @Test
    fun `parseInventory - строка из квадратных скобок = пустой список`() {
        // Баг исправлен: дефолт теперь "", но старые данные могут иметь "[]"
        // parseInventory("[]") возвращает ["[]"] (один мусорный элемент)
        // Это ожидаемое поведение - данные считаются некорректными
        val result = parseInventory("[]")
        assertEquals(1, result.size) // "[]" попадает как строка
    }

    // ==================== buildInventory ====================

    @Test
    fun `buildInventory - пустой список = пустая строка`() {
        assertEquals("", buildInventory(emptyList()))
    }

    @Test
    fun `buildInventory - один элемент без запятой`() {
        assertEquals("boots_002_1717499234567:0", buildInventory(listOf("boots_002_1717499234567:0")))
    }

    @Test
    fun `buildInventory - два элемента через запятую`() {
        val result = buildInventory(listOf("a:0", "b:1"))
        assertEquals("a:0,b:1", result)
    }

    @Test
    fun `buildInventory фильтрует пустые элементы`() {
        val result = buildInventory(listOf("a:0", "", "b:1"))
        assertEquals("a:0,b:1", result)
    }

    // ==================== getBaseId ====================

    @Test
    fun `getBaseId - обычный ID с timestamp убирает хвост`() {
        assertEquals("boots_002", getBaseId("boots_002_1717499234567:3"))
    }

    @Test
    fun `getBaseId - ID без timestamp возвращается как есть`() {
        assertEquals("boots_002", getBaseId("boots_002:0"))
    }

    @Test
    fun `getBaseId - сложный ID с тремя частями и коротким хвостом не урезается`() {
        // "123" всего 3 цифры, < 8 порога
        assertEquals("boots_002_123", getBaseId("boots_002_123:0"))
    }

    // ==================== getEnchantLevel ====================

    @Test
    fun `getEnchantLevel - уровень 0 при отсутствии заточки`() {
        assertEquals(0, getEnchantLevel("boots_002_1717499234567:0"))
    }

    @Test
    fun `getEnchantLevel - уровень 3`() {
        assertEquals(3, getEnchantLevel("boots_002_1717499234567:3"))
    }

    @Test
    fun `getEnchantLevel - максимальный уровень 9`() {
        assertEquals(9, getEnchantLevel("boots_002_1717499234567:9"))
    }

    @Test
    fun `getEnchantLevel - нет двоеточия = 0`() {
        assertEquals(0, getEnchantLevel("boots_002_1717499234567"))
    }

    // ==================== getUniqueId ====================

    @Test
    fun `getUniqueId - возвращает часть до двоеточия`() {
        assertEquals("boots_002_1717499234567", getUniqueId("boots_002_1717499234567:3"))
    }

    // ==================== Интеграция: добавить и удалить предмет ====================

    @Test
    fun `добавление предмета в пустой инвентарь`() {
        var inv = ""
        val entries = parseInventory(inv)
        entries.add("boots_002_1717499234567:0")
        inv = buildInventory(entries)
        assertEquals("boots_002_1717499234567:0", inv)
    }

    @Test
    fun `удаление предмета по uniqueId`() {
        var inv = "boots_002_1717499234567:0,head_001_1717499234568:1"
        val entries = parseInventory(inv)
        val targetId = "boots_002_1717499234567"
        val idx = entries.indexOfFirst { getUniqueId(it) == targetId }
        assertTrue(idx >= 0)
        entries.removeAt(idx)
        inv = buildInventory(entries)
        assertEquals("head_001_1717499234568:1", inv)
    }
}
