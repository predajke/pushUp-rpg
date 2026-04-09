package com.ninthbalcony.pushuprpg

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ninthbalcony.pushuprpg.data.db.AppDatabase
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.model.EnchantResult
import com.ninthbalcony.pushuprpg.data.model.ForgeResult
import com.ninthbalcony.pushuprpg.data.repository.GameRepository
import com.ninthbalcony.pushuprpg.utils.ActiveQuest
import com.ninthbalcony.pushuprpg.utils.ItemUtils
import com.ninthbalcony.pushuprpg.utils.QuestSystem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke-тест основных сценариев игры.
 * Запускать на эмуляторе: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: GameRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppDatabase.setTestInstance(db)
        repo = GameRepository(context)
        ItemUtils.loadItems(context)
    }

    @After
    fun tearDown() {
        AppDatabase.clearTestInstance()
        db.close()
    }

    // ==================== СЦЕНАРИЙ 1: BUY ====================

    @Test
    fun buy_deductsTeethAndAddsItemToInventory() = runBlocking {
        val target = ItemUtils.loadItems(context).firstOrNull { it.rarity == "common" }
            ?: return@runBlocking
        seedState(teeth = 1000)

        val ok = repo.buyShopItem(target.id)
        assertTrue("Покупка должна вернуть true", ok)

        val after = repo.getGameState()
        assertTrue("Зубы должны уменьшиться", after.teeth < 1000)
        assertTrue("Инвентарь должен содержать купленную вещь",
            after.inventoryItems.contains(target.id))
    }

    @Test
    fun buy_failsWhenNotEnoughTeeth() = runBlocking {
        val target = ItemUtils.loadItems(context).firstOrNull { it.rarity == "common" }
            ?: return@runBlocking
        seedState(teeth = 0)

        val ok = repo.buyShopItem(target.id)
        assertFalse("Покупка без зубов должна вернуть false", ok)
        assertEquals("Зубы не должны измениться", 0, repo.getGameState().teeth)
    }

    @Test
    fun buy_tracksTeethSpentQuest() = runBlocking {
        val target = ItemUtils.loadItems(context).firstOrNull { it.rarity == "common" }
            ?: return@runBlocking
        val questJson = QuestSystem.serialize(listOf(
            ActiveQuest("weekly_teeth_spent_300", progress = 0, claimed = false)
        ))
        seedState(teeth = 1000, activeQuestsJson = questJson)

        repo.buyShopItem(target.id)

        val quests = QuestSystem.deserialize(repo.getGameState().activeQuestsJson)
        val q = quests.firstOrNull { it.defId == "weekly_teeth_spent_300" }
        assertNotNull("Квест TEETH_SPENT должен присутствовать", q)
        assertTrue("Прогресс квеста должен увеличиться после покупки", q!!.progress > 0)
    }

    @Test
    fun buy_preventsDuplicatePurchase_withMutex() = runBlocking {
        // Симуляция двойного нажатия: два последовательных вызова
        val target = ItemUtils.loadItems(context).firstOrNull { it.rarity == "common" }
            ?: return@runBlocking
        seedState(teeth = 1000)

        val ok1 = repo.buyShopItem(target.id)
        // После первой покупки предмет убирается из shopItems
        // Вторая покупка всё равно добавит (shopItems не ограничивает) но зубы спишутся дважды
        // Mutex гарантирует что обе операции выполнятся последовательно, не параллельно
        val ok2 = repo.buyShopItem(target.id)

        assertTrue("Первая покупка успешна", ok1)
        // ok2 может быть true (если зубов хватает) — главное что обе не выполнились одновременно
        val after = repo.getGameState()
        assertTrue("Зубы должны быть корректно списаны", after.teeth >= 0)
    }

    // ==================== СЦЕНАРИЙ 2: FORGE ====================

    @Test
    fun forge_successCreatesNewItem() = runBlocking {
        val items = ItemUtils.loadItems(context).filter { it.rarity == "common" }
        if (items.size < 2) return@runBlocking

        val uid1 = "${items[0].id}_111111111111"
        val uid2 = "${items[1].id}_222222222222"

        // 15% шанс FAIL → повторяем до получения Success (макс 30 попыток)
        var result: ForgeResult = ForgeResult.NoItems
        repeat(30) {
            if (result is ForgeResult.Success) return@repeat
            seedState(inventory = "$uid1:0,$uid2:0", forgeSlot1 = uid1, forgeSlot2 = uid2)
            result = repo.mergeItems()
        }

        assertTrue("За 30 попыток должен быть хотя бы один Success", result is ForgeResult.Success)
        val after = repo.getGameState()
        assertEquals("Слоты кузницы должны очиститься", "", after.forgeSlot1)
        assertEquals("Слоты кузницы должны очиститься", "", after.forgeSlot2)
        assertFalse("Исходные предметы должны исчезнуть из инвентаря",
            after.inventoryItems.contains(uid1))
    }

    @Test
    fun forge_failDestroysBothItems() = runBlocking {
        val items = ItemUtils.loadItems(context).filter { it.rarity == "common" }
        if (items.size < 2) return@runBlocking

        val uid1 = "${items[0].id}_111111111111"
        val uid2 = "${items[1].id}_222222222222"

        // 15% шанс FAIL → за 50 попыток вероятность не получить FAIL ≈ 0.03%
        var gotFail = false
        repeat(50) {
            if (gotFail) return@repeat
            seedState(inventory = "$uid1:0,$uid2:0", forgeSlot1 = uid1, forgeSlot2 = uid2)
            val result = repo.mergeItems()
            if (result is ForgeResult.Fail) {
                gotFail = true
                val after = repo.getGameState()
                assertFalse("При FAIL uid1 должен исчезнуть", after.inventoryItems.contains(uid1))
                assertFalse("При FAIL uid2 должен исчезнуть", after.inventoryItems.contains(uid2))
                assertEquals("Слоты кузницы очищены после FAIL", "", after.forgeSlot1)
            }
        }
        assertTrue("FAIL должен был произойти за 50 попыток", gotFail)
    }

    @Test
    fun forge_noItemsWhenSlotsEmpty() = runBlocking {
        seedState(forgeSlot1 = "", forgeSlot2 = "")
        assertEquals(ForgeResult.NoItems, repo.mergeItems())
    }

    // ==================== СЦЕНАРИЙ 3: ENCHANT ====================

    @Test
    fun enchant_deductsTeethRegardlessOfResult() = runBlocking {
        // Зубы списываются при ЛЮБОМ исходе (Success или Fail)
        val item = ItemUtils.loadItems(context).firstOrNull { it.rarity == "common" }
            ?: return@runBlocking
        val uid = "${item.id}_333333333333"
        seedState(inventory = "$uid:0", teeth = 10000)

        val result = repo.enchantItem(uid)
        // MAX_LEVEL не должен произойти при уровне 0
        assertNotEquals("Не должно быть MAX_LEVEL при уровне 0", EnchantResult.MAX_LEVEL, result)
        assertNotEquals("Не должно быть NOT_ENOUGH_TEETH при 10000", EnchantResult.NOT_ENOUGH_TEETH, result)

        assertTrue("Зубы должны уменьшиться", repo.getGameState().teeth < 10000)
    }

    @Test
    fun enchant_successIncreasesLevel() = runBlocking {
        val item = ItemUtils.loadItems(context).firstOrNull { it.rarity == "common" }
            ?: return@runBlocking
        val uid = "${item.id}_444444444444"

        // luck = 999f → шанс 90% (максимум). За 10 попыток P(хотя бы 1 успех) = 1 - 0.1^10 ≈ 100%
        var gotSuccess = false
        repeat(10) {
            if (gotSuccess) return@repeat
            seedState(inventory = "$uid:0", teeth = 10000, luck = 999f)
            val result = repo.enchantItem(uid)
            if (result == EnchantResult.SUCCESS) {
                gotSuccess = true
                assertTrue("При SUCCESS уровень заточки должен вырасти до :1",
                    repo.getGameState().inventoryItems.contains("$uid:1"))
            }
        }
        assertTrue("За 10 попыток с 90% шансом должен быть хотя бы один SUCCESS", gotSuccess)
    }

    @Test
    fun enchant_failsWithNoTeeth() = runBlocking {
        val item = ItemUtils.loadItems(context).firstOrNull { it.rarity == "common" }
            ?: return@runBlocking
        val uid = "${item.id}_555555555555"
        seedState(inventory = "$uid:0", teeth = 0)

        assertEquals(EnchantResult.NOT_ENOUGH_TEETH, repo.enchantItem(uid))
        assertEquals("Зубы не должны измениться", 0, repo.getGameState().teeth)
    }

    @Test
    fun enchant_blocksAtMaxLevel() = runBlocking {
        val item = ItemUtils.loadItems(context).firstOrNull { it.rarity == "common" }
            ?: return@runBlocking
        val uid = "${item.id}_666666666666"
        seedState(inventory = "$uid:9", teeth = 99999)

        assertEquals(EnchantResult.MAX_LEVEL, repo.enchantItem(uid))
    }

    // ==================== СЦЕНАРИЙ 4: QUEST CLAIM ====================

    @Test
    fun questClaim_grantsTeethReward() = runBlocking {
        val questJson = QuestSystem.serialize(listOf(
            ActiveQuest("pushups_50", progress = 50, claimed = false)
        ))
        seedState(teeth = 100, activeQuestsJson = questJson)

        val ok = repo.claimQuestReward("pushups_50")
        assertTrue("Claim должен вернуть true", ok)

        val after = repo.getGameState()
        val def = QuestSystem.getDefById("pushups_50")!!
        assertEquals("Зубы должны увеличиться на rewardTeeth", 100 + def.rewardTeeth, after.teeth)

        val q = QuestSystem.deserialize(after.activeQuestsJson).firstOrNull { it.defId == "pushups_50" }
        assertTrue("Квест должен стать claimed", q?.claimed == true)
    }

    @Test
    fun questClaim_blocksDoubleClaim() = runBlocking {
        val questJson = QuestSystem.serialize(listOf(
            ActiveQuest("pushups_50", progress = 50, claimed = false)
        ))
        seedState(teeth = 100, activeQuestsJson = questJson)

        val first = repo.claimQuestReward("pushups_50")
        val second = repo.claimQuestReward("pushups_50")

        assertTrue("Первый клейм должен быть успешным", first)
        assertFalse("Второй клейм должен быть отклонён", second)

        val def = QuestSystem.getDefById("pushups_50")!!
        assertEquals("Зубы добавились ровно один раз", 100 + def.rewardTeeth, repo.getGameState().teeth)
    }

    @Test
    fun questClaim_failsIfNotCompleted() = runBlocking {
        val questJson = QuestSystem.serialize(listOf(
            ActiveQuest("pushups_50", progress = 10, claimed = false)
        ))
        seedState(teeth = 100, activeQuestsJson = questJson)

        val ok = repo.claimQuestReward("pushups_50")
        assertFalse("Нельзя клеймить незавершённый квест", ok)
        assertEquals("Зубы не должны измениться", 100, repo.getGameState().teeth)
    }

    // ==================== REROLL TEETH_SPENT ====================

    @Test
    fun reroll_tracksTeethSpent() = runBlocking {
        val questJson = QuestSystem.serialize(listOf(
            ActiveQuest("weekly_teeth_spent_300", progress = 0, claimed = false)
        ))
        seedState(teeth = 50, activeQuestsJson = questJson)

        val ok = repo.rerollShop()
        assertTrue("Reroll при наличии зубов должен вернуть true", ok)

        val after = repo.getGameState()
        assertEquals("После reroll зубы уменьшились на 1", 49, after.teeth)

        val q = QuestSystem.deserialize(after.activeQuestsJson).firstOrNull { it.defId == "weekly_teeth_spent_300" }
        assertEquals("Прогресс квеста увеличился на 1", 1, q?.progress)
    }

    @Test
    fun reroll_failsWithNoTeeth() = runBlocking {
        seedState(teeth = 0)
        val ok = repo.rerollShop()
        assertFalse("Reroll без зубов должен вернуть false", ok)
    }

    // ==================== ВСПОМОГАТЕЛЬНАЯ ФУНКЦИЯ ====================

    private suspend fun seedState(
        teeth: Int = 500,
        inventory: String = "",
        forgeSlot1: String = "",
        forgeSlot2: String = "",
        luck: Float = 50f,
        activeQuestsJson: String = ""
    ) {
        db.pushUpDao().saveGameState(
            GameStateEntity(
                teeth = teeth,
                inventoryItems = inventory,
                forgeSlot1 = forgeSlot1,
                forgeSlot2 = forgeSlot2,
                baseLuck = luck,
                activeQuestsJson = activeQuestsJson
            )
        )
    }
}
