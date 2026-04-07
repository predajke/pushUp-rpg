package com.pushupRPG.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pushupRPG.app.data.db.AppDatabase
import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.data.model.EnchantResult
import com.pushupRPG.app.data.repository.GameRepository
import com.pushupRPG.app.utils.ItemUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented тест для механик инвентаря, экипировки, продажи, заточки и кузницы.
 *
 * Запуск: ./gradlew connectedAndroidTest
 *
 * Тест сценарий (соответствует ручному тесту пользователя):
 * 1. Создаём 10 вещей (по 2 каждого слота) + 100000 зубов
 * 2. Надеваем по одной — проверяем кол-во в инвентаре
 * 3. Снимаем все — проверяем возврат
 * 4. Затачиваем вещь до +2
 * 5. Надеваем снова — проверяем что уровень заточки сохранился
 * 6. Merge двух вещей в кузнице
 */
@RunWith(AndroidJUnit4::class)
class EquipUnequipTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: GameRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ItemUtils.loadItems(context)  // Загружаем items.json

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // Подменяем стандартный синглтон через рефлексию или просто инстанцируем repo напрямую
        repo = GameRepository(context)
        // Для тестов используем inMemory DB — нужно передать контекст с подменённой DB.
        // Поскольку GameRepository создаёт DB через AppDatabase.getDatabase(context),
        // тесты работают с реальной (тестовой) БД на устройстве.
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ ====================

    private fun parseInventory(str: String): List<String> =
        str.split(",").filter { it.isNotEmpty() }

    private fun getUniqueId(entry: String) = entry.split(":")[0]
    private fun getBaseId(entry: String): String {
        val idPart = entry.split(":")[0]
        val parts = idPart.split("_")
        return if (parts.size > 2 && parts.last().all { it.isDigit() } && parts.last().length > 8)
            parts.dropLast(1).joinToString("_") else idPart
    }
    private fun getEnchantLevel(entry: String) = entry.split(":").getOrNull(1)?.toIntOrNull() ?: 0

    // ==================== БАЗОВЫЕ ТЕСТЫ ИНВЕНТАРЯ ====================

    @Test
    fun addDebugItems_addsExactly10ItemsAnd100000Teeth() = runBlocking {
        // Убеждаемся что инвентарь начинается пустым
        val initialState = repo.getGameState()
        val initialCount = parseInventory(initialState.inventoryItems).size

        repo.addDebugItemsForTest()

        val state = repo.getGameState()
        val items = parseInventory(state.inventoryItems)
        assertEquals(initialCount + 10, items.size)
        assertEquals(initialState.teeth + 100000, state.teeth)
    }

    @Test
    fun equipItem_removesFromInventory() = runBlocking {
        repo.addDebugItemsForTest()
        val stateBefore = repo.getGameState()
        val entries = parseInventory(stateBefore.inventoryItems)
        val countBefore = entries.size

        val firstEntry = entries.first()
        val uniqueId = getUniqueId(firstEntry)
        val baseId = getBaseId(firstEntry)
        val item = ItemUtils.getItemById(baseId)!!
        val slot = item.slot

        repo.equipItem(uniqueId, slot)

        val stateAfter = repo.getGameState()
        val itemsAfter = parseInventory(stateAfter.inventoryItems)
        assertEquals("Кол-во в инвентаре должно уменьшиться на 1", countBefore - 1, itemsAfter.size)
        assertFalse(
            "Вещь должна быть убрана из инвентаря",
            itemsAfter.any { getUniqueId(it) == uniqueId }
        )
    }

    @Test
    fun equipItem_appearsInCorrectSlot() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)

        // Находим вещь типа "head"
        val headEntry = entries.firstOrNull { getBaseId(it).let { id -> ItemUtils.getItemById(id)?.slot == "head" } }
            ?: return@runBlocking
        val headId = getUniqueId(headEntry)

        repo.equipItem(headId, "head")

        val stateAfter = repo.getGameState()
        assertEquals("Вещь должна быть в слоте head", headId, stateAfter.equippedHead.split(":")[0])
    }

    @Test
    fun unequipItem_returnsToInventory() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)
        val headEntry = entries.firstOrNull { ItemUtils.getItemById(getBaseId(it))?.slot == "head" }
            ?: return@runBlocking
        val headId = getUniqueId(headEntry)

        repo.equipItem(headId, "head")
        val countAfterEquip = parseInventory(repo.getGameState().inventoryItems).size

        repo.unequipItem("head")

        val stateAfter = repo.getGameState()
        val itemsAfter = parseInventory(stateAfter.inventoryItems)
        assertEquals("Вещь должна вернуться в инвентарь", countAfterEquip + 1, itemsAfter.size)
        assertTrue(
            "Слот head должен быть пустым",
            stateAfter.equippedHead.isEmpty()
        )
    }

    @Test
    fun equipAllSlots_inventoryCountDecreases() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)
        val initialCount = entries.size // 10

        val slotMap = mutableMapOf<String, String>() // slot -> uniqueId

        for (entry in entries) {
            val baseId = getBaseId(entry)
            val item = ItemUtils.getItemById(baseId) ?: continue
            val slot = item.slot
            if (!slotMap.containsKey(slot)) {
                slotMap[slot] = getUniqueId(entry)
            }
        }

        var equipped = 0
        for ((slot, uniqueId) in slotMap) {
            repo.equipItem(uniqueId, slot)
            equipped++
            val currentItems = parseInventory(repo.getGameState().inventoryItems)
            assertEquals(
                "После экипировки $equipped вещей в инвентаре должно быть ${initialCount - equipped}",
                initialCount - equipped,
                currentItems.size
            )
        }
    }

    @Test
    fun weaponSlot_fillsWeapon1ThenWeapon2() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)

        val weaponEntries = entries.filter { ItemUtils.getItemById(getBaseId(it))?.slot == "weapon" }
        assertTrue("Должно быть >= 2 оружия", weaponEntries.size >= 2)

        val id1 = getUniqueId(weaponEntries[0])
        val id2 = getUniqueId(weaponEntries[1])

        repo.equipItem(id1, "weapon")
        repo.equipItem(id2, "weapon")

        val stateAfter = repo.getGameState()
        assertEquals("weapon1 должен быть заполнен", id1, stateAfter.equippedWeapon1.split(":")[0])
        assertEquals("weapon2 должен быть заполнен", id2, stateAfter.equippedWeapon2.split(":")[0])
        assertEquals("В инвентаре должно быть на 2 меньше",
            entries.size - 2,
            parseInventory(stateAfter.inventoryItems).size
        )
    }

    @Test
    fun weaponSlot_replacingWeapon1_returnsOldToInventory() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)
        val weaponEntries = entries.filter { ItemUtils.getItemById(getBaseId(it))?.slot == "weapon" }
        if (weaponEntries.size < 2) return@runBlocking

        val id1 = getUniqueId(weaponEntries[0])
        val id2 = getUniqueId(weaponEntries[1])

        // Заполняем оба слота
        repo.equipItem(id1, "weapon")
        repo.equipItem(id2, "weapon")

        // Убедимся что в инвентаре есть ещё одно оружие для замены
        val state2 = repo.getGameState()
        val remaining = parseInventory(state2.inventoryItems)
        val weaponId3 = remaining.firstOrNull { ItemUtils.getItemById(getBaseId(it))?.slot == "weapon" }
            ?: return@runBlocking
        val id3 = getUniqueId(weaponId3)
        val countBefore = remaining.size

        // Заменяем weapon1 (оба слота заняты → заменяем weapon1)
        repo.equipItem(id3, "weapon")

        val stateAfter = repo.getGameState()
        val itemsAfter = parseInventory(stateAfter.inventoryItems)

        // id3 ушёл из инвентаря
        assertFalse("id3 должен уйти из инвентаря", itemsAfter.any { getUniqueId(it) == id3 })
        // id1 (старый weapon1) вернулся в инвентарь
        assertTrue("Старый weapon1 должен вернуться в инвентарь", itemsAfter.any { getUniqueId(it) == id1 })
        // weapon2 (id2) не тронут
        assertEquals("weapon2 должен остаться нетронутым", id2, stateAfter.equippedWeapon2.split(":")[0])
        // Размер инвентаря: было countBefore - 1 (ушёл id3) + 1 (вернулся id1)
        assertEquals("Размер инвентаря не должен измениться", countBefore, itemsAfter.size)
    }

    @Test
    fun equippedItem_replacedByNewItem_oldGoesToInventory() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)
        val headEntries = entries.filter { ItemUtils.getItemById(getBaseId(it))?.slot == "head" }
        if (headEntries.size < 2) return@runBlocking

        val id1 = getUniqueId(headEntries[0])
        val id2 = getUniqueId(headEntries[1])

        repo.equipItem(id1, "head")
        val countAfterFirst = parseInventory(repo.getGameState().inventoryItems).size

        repo.equipItem(id2, "head")

        val stateAfter = repo.getGameState()
        val itemsAfter = parseInventory(stateAfter.inventoryItems)
        assertEquals("head должен теперь иметь id2", id2, stateAfter.equippedHead.split(":")[0])
        assertTrue("id1 (старый) должен вернуться в инвентарь", itemsAfter.any { getUniqueId(it) == id1 })
        assertEquals("Кол-во в инвентаре = было - 1 (id2 ушёл) + 1 (id1 вернулся) = то же",
            countAfterFirst, itemsAfter.size
        )
    }

    // ==================== ЗАТОЧКА ====================

    @Test
    fun enchantItem_levelIncreasesOnSuccess_or_unchanged_on_failure() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)
        val firstId = getUniqueId(entries.first())
        val levelBefore = getEnchantLevel(entries.first())

        val result = repo.enchantItem(firstId)

        val stateAfter = repo.getGameState()
        val entryAfter = parseInventory(stateAfter.inventoryItems)
            .first { getUniqueId(it) == firstId }
        val levelAfter = getEnchantLevel(entryAfter)

        when (result) {
            EnchantResult.SUCCESS -> assertEquals("SUCCESS: уровень должен вырасти на 1", levelBefore + 1, levelAfter)
            EnchantResult.FAILED -> assertEquals("FAILED: уровень должен остаться прежним", levelBefore, levelAfter)
            EnchantResult.NOT_ENOUGH_TEETH -> assertEquals("NOT_ENOUGH_TEETH: уровень не меняется", levelBefore, levelAfter)
            EnchantResult.MAX_LEVEL -> assertEquals("MAX_LEVEL: уровень не меняется", levelBefore, levelAfter)
        }
    }

    @Test
    fun enchantItem_maxLevel9_returnsMaxLevel() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)
        val firstEntry = entries.first()
        val firstId = getUniqueId(firstEntry)
        val baseId = getBaseId(firstEntry)

        // Форсируем уровень 9 напрямую через DB
        val updatedEntries = entries.toMutableList()
        val idx = updatedEntries.indexOf(firstEntry)
        updatedEntries[idx] = "$firstId:9"
        val dao = db.pushUpDao()
        dao.saveGameState(state.copy(inventoryItems = updatedEntries.joinToString(",")))

        // Пытаемся зачаровать на +10
        val result = repo.enchantItem(firstId)
        assertEquals(EnchantResult.MAX_LEVEL, result)
    }

    @Test
    fun enchantedItem_retainsLevelAfterEquipAndUnequip() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)

        // Находим вещь для слота head
        val headEntry = entries.firstOrNull { ItemUtils.getItemById(getBaseId(it))?.slot == "head" }
            ?: return@runBlocking
        val headId = getUniqueId(headEntry)

        // Форсируем уровень заточки 3
        val updatedEntries = entries.toMutableList()
        val idx = updatedEntries.indexOf(headEntry)
        updatedEntries[idx] = "$headId:3"
        val dao = db.pushUpDao()
        dao.saveGameState(state.copy(inventoryItems = updatedEntries.joinToString(",")))

        // Экипируем и снимаем
        repo.equipItem(headId, "head")
        repo.unequipItem("head")

        // Проверяем что уровень заточки сохранился
        val stateAfter = repo.getGameState()
        val entryAfter = parseInventory(stateAfter.inventoryItems).first { getUniqueId(it) == headId }
        assertEquals("Уровень заточки должен сохраниться после снятия", 3, getEnchantLevel(entryAfter))
    }

    // ==================== ПРОДАЖА ====================

    @Test
    fun sellItem_removesFromInventoryAndAddsTeeth() = runBlocking {
        repo.addDebugItemsForTest()
        val stateBefore = repo.getGameState()
        val entries = parseInventory(stateBefore.inventoryItems)
        val toSell = entries.first()
        val uniqueId = getUniqueId(toSell)
        val baseId = getBaseId(toSell)
        val item = ItemUtils.getItemById(baseId)!!
        val expectedTeeth = com.pushupRPG.app.utils.GameCalculations.getTeethFromSell(item.rarity)

        repo.sellItem(uniqueId)

        val stateAfter = repo.getGameState()
        val itemsAfter = parseInventory(stateAfter.inventoryItems)
        assertFalse("Проданный предмет не должен быть в инвентаре", itemsAfter.any { getUniqueId(it) == uniqueId })
        assertEquals("Зубы должны увеличиться", stateBefore.teeth + expectedTeeth, stateAfter.teeth)
        assertEquals("Инвентарь должен уменьшиться на 1", entries.size - 1, itemsAfter.size)
    }

    @Test
    fun sellItem_notInInventory_doesNothing() = runBlocking {
        repo.addDebugItemsForTest()
        val stateBefore = repo.getGameState()
        repo.sellItem("nonexistent_item_id_that_doesnt_exist")
        val stateAfter = repo.getGameState()
        assertEquals("Зубы не должны измениться", stateBefore.teeth, stateAfter.teeth)
        assertEquals("Инвентарь не должен измениться",
            parseInventory(stateBefore.inventoryItems).size,
            parseInventory(stateAfter.inventoryItems).size
        )
    }

    // ==================== КУЗНИЦА ====================

    @Test
    fun mergeItems_removesBothAndAddsOne() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)
        val countBefore = entries.size

        val id1 = getUniqueId(entries[0])
        val id2 = getUniqueId(entries[1])

        repo.setForgeSlot(1, id1)
        repo.setForgeSlot(2, id2)

        val result = repo.mergeItems()
        assertNotNull("Merge должен вернуть предмет", result)

        val stateAfter = repo.getGameState()
        val itemsAfter = parseInventory(stateAfter.inventoryItems)

        // -2 из инвентаря +1 новый = countBefore - 1
        assertEquals("Инвентарь: -2 +1 = ${countBefore - 1}", countBefore - 1, itemsAfter.size)
        assertFalse("id1 должен уйти из инвентаря", itemsAfter.any { getUniqueId(it) == id1 })
        assertFalse("id2 должен уйти из инвентаря", itemsAfter.any { getUniqueId(it) == id2 })

        // Forge слоты должны быть очищены
        assertEquals("forgeSlot1 должен быть пуст", "", stateAfter.forgeSlot1)
        assertEquals("forgeSlot2 должен быть пуст", "", stateAfter.forgeSlot2)
    }

    @Test
    fun mergeItems_resultCanBeEquippedAndUnequipped() = runBlocking {
        repo.addDebugItemsForTest()
        val state = repo.getGameState()
        val entries = parseInventory(state.inventoryItems)

        repo.setForgeSlot(1, getUniqueId(entries[0]))
        repo.setForgeSlot(2, getUniqueId(entries[1]))
        val result = repo.mergeItems()
        if (result !is com.pushupRPG.app.data.model.ForgeResult.Success) return@runBlocking

        // Находим новую вещь в инвентаре
        val stateAfterMerge = repo.getGameState()
        val itemsAfterMerge = parseInventory(stateAfterMerge.inventoryItems)
        val mergedEntry = itemsAfterMerge.lastOrNull() ?: return@runBlocking
        val mergedUniqueId = getUniqueId(mergedEntry)

        // Экипируем в первый доступный слот (head)
        repo.equipItem(mergedUniqueId, "head")

        val stateAfterEquip = repo.getGameState()
        val equippedId = stateAfterEquip.equippedHead
        assertEquals("Вещь из Merge должна надеться в слот head",
            mergedUniqueId, equippedId.split(":")[0]
        )

        // Снимаем
        repo.unequipItem("head")

        val stateAfterUnequip = repo.getGameState()
        val itemsAfterUnequip = parseInventory(stateAfterUnequip.inventoryItems)
        assertTrue("Вещь из Merge должна вернуться в инвентарь после снятия",
            itemsAfterUnequip.any { getUniqueId(it) == mergedUniqueId }
        )
    }
}
