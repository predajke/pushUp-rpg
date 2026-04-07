package com.pushupRPG.app.utils

object AppStrings {
    private fun m(en: String, ru: String, es: String, fr: String, de: String, pt: String) =
        mapOf("en" to en, "ru" to ru, "es" to es, "fr" to fr, "de" to de, "pt" to pt)

    private val data = mapOf(
        // --- Экраны / Навигация ---
        "inventory"         to m("Inventory",         "Инвентарь",        "Inventario",          "Inventaire",           "Inventar",              "Inventário"),
        "shop"              to m("Shop",               "Магазин",          "Tienda",              "Boutique",             "Shop",                  "Loja"),
        "settings"          to m("Settings",           "Настройки",        "Ajustes",             "Paramètres",           "Einstellungen",         "Configurações"),
        "statistics"        to m("Statistics",         "Статистика",       "Estadísticas",        "Statistiques",         "Statistiken",           "Estatísticas"),
        "bestiary"          to m("Bestiary",           "Бестиарий",        "Bestiario",           "Bestiaire",            "Bestiarium",            "Bestiário"),
        "achievements"      to m("Achievements",       "Достижения",       "Logros",              "Succès",               "Errungenschaften",      "Conquistas"),
        "progress"          to m("Progress",           "Прогресс",         "Progreso",            "Progrès",              "Fortschritt",           "Progresso"),
        "quests"            to m("Quests",             "Квесты",           "Misiones",            "Quêtes",               "Quests",                "Missões"),
        "battle_log"        to m("Battle Log",         "Журнал боя",       "Registro de Batalla", "Journal de Combat",    "Kampfprotokoll",        "Registro de Batalha"),
        "item_log"          to m("Item Log",           "Лог предметов",    "Reg. de Objetos",     "Journal d'Objets",     "Gegenstandsprotokoll",  "Registro de Itens"),

        // --- Слоты снаряжения ---
        "slot_head"         to m("Head",     "Голова",    "Cabeza",    "Tête",     "Kopf",       "Cabeça"),
        "slot_necklace"     to m("Necklace", "Ожерелие",  "Collar",    "Collier",  "Halskette",  "Colar"),
        "slot_weapon1"      to m("Weapon 1", "Оружие 1",  "Arma 1",    "Arme 1",   "Waffe 1",    "Arma 1"),
        "slot_weapon2"      to m("Weapon 2", "Оружие 2",  "Arma 2",    "Arme 2",   "Waffe 2",    "Arma 2"),
        "slot_pants"        to m("Pants",    "Штаны",     "Pantalones","Pantalon",  "Hose",       "Calças"),
        "slot_boots"        to m("Boots",    "Обувь",     "Botas",     "Bottes",    "Stiefel",    "Botas"),

        // --- Характеристики ---
        "stat_title"        to m("Stats",   "Характеристики", "Estadísticas", "Stats",    "Werte",        "Atributos"),
        "stat_power"        to m("Power",   "Сила",           "Fuerza",       "Force",    "Stärke",       "Força"),
        "stat_armor"        to m("Armor",   "Броня",          "Armadura",     "Armure",   "Rüstung",      "Armadura"),
        "stat_health"       to m("Health",  "Здоровье",       "Salud",        "Santé",    "Gesundheit",   "Saúde"),
        "stat_luck"         to m("Luck",    "Удача",          "Suerte",       "Chance",   "Glück",        "Sorte"),
        "stat_level"        to m("Level",   "Уровень",        "Nivel",        "Niveau",   "Level",        "Nível"),

        // --- Инвентарь ---
        "inventory_empty"   to m(
            "Inventory is empty.\nDefeat monsters to get items!",
            "Инвентарь пуст.\nПобеди монстров чтобы получить предметы!",
            "Inventario vacío.\nDerrota monstruos para obtener objetos!",
            "Inventaire vide.\nVaincre des monstres pour obtenir des objets!",
            "Inventar leer.\nBesiege Monster, um Gegenstände zu erhalten!",
            "Inventário vazio.\nDerrote monstros para obter itens!"
        ),
        "item_select_prompt" to m("Select an item to view info", "Выбери предмет для просмотра", "Selecciona un objeto", "Sélectionne un objet", "Gegenstand wählen", "Selecione um item"),
        "btn_equip"         to m("Equip",   "Надеть",  "Equipar",   "Équiper",   "Ausrüsten", "Equipar"),
        "btn_unequip"       to m("Unequip", "Снять",   "Desequipar","Déséquiper","Ablegen",   "Remover"),
        "btn_sell"          to m("Sell",    "Продать", "Vender",    "Vendre",    "Verkaufen", "Vender"),

        // --- Редкость предметов ---
        "rarity_common"     to m("Common",    "Обычный",   "Común",        "Commun",     "Gewöhnlich",   "Comum"),
        "rarity_uncommon"   to m("Uncommon",  "Необычный", "Poco común",   "Peu commun", "Ungewöhnlich", "Incomum"),
        "rarity_rare"       to m("Rare",      "Редкий",    "Raro",         "Rare",       "Selten",       "Raro"),
        "rarity_epic"       to m("Epic",      "Эпический", "Épico",        "Épique",     "Episch",       "Épico"),
        "rarity_legendary"  to m("Legendary", "Легендарный","Legendario",  "Légendaire", "Legendär",     "Lendário"),
        "status_equipped"   to m("Equipped",  "Надето",    "Equipado",     "Équipé",     "Ausgerüstet",  "Equipado"),

        // --- Бестиарий ---
        "monsters"          to m("Monsters",        "Монстры",          "Monstruos",        "Monstres",         "Monster",              "Monstros"),
        "bosses"            to m("Bosses",           "Боссы",            "Jefes",            "Boss",             "Bosse",                "Chefes"),
        "kills"             to m("kills",            "убийств",          "bajas",            "victimes",         "Kills",                "mortes"),
        "not_encountered"   to m("Not encountered",  "Не встречен",      "No encontrado",    "Pas rencontré",    "Nicht angetroffen",    "Não encontrado"),
        "lv_prefix"         to m("Lv.",              "Ур.",              "Nv.",              "Nv.",              "Lv.",                  "Nv."),

        // --- Достижения ---
        "btn_unselect_all"  to m("Unselect all",  "Снять все",    "Desmarcar todo",        "Tout désélectionner","Alle abwählen",   "Desmarcar tudo"),
        "ach_unique"        to m("Unique",         "Уникальные",   "Único",                 "Unique",             "Einzigartig",     "Único"),
        "ach_progressive"   to m("Progressive",    "Прогрессивные","Progresivo",            "Progressif",         "Progressiv",      "Progressivo"),
        "btn_save"          to m("Save",           "Сохранить",    "Guardar",               "Enregistrer",        "Speichern",       "Salvar"),

        // --- Квесты ---
        "quest_daily"       to m("Daily",          "Ежедневные",       "Diarias",        "Quotidiennes",     "Täglich",      "Diárias"),
        "quest_no_daily"    to m("No daily quests","Нет ежедневных квестов","Sin misiones diarias","Pas de quêtes quotidiennes","Keine täglichen Quests","Sem missões diárias"),
        "quest_weekly"      to m("Weekly",         "Еженедельные",     "Semanales",      "Hebdomadaires",    "Wöchentlich",  "Semanais"),
        "quest_no_weekly"   to m("No weekly quests","Нет еженедельных квестов","Sin misiones semanales","Pas de quêtes hebdomadaires","Keine wöchentlichen Quests","Sem missões semanais"),
        "quest_claimed"     to m("✓ Claimed",      "✓ Получено",       "✓ Reclamado",    "✓ Réclamé",        "✓ Abgeholt",   "✓ Resgatado"),
        "btn_claim"         to m("Claim!",         "Забрать!",         "Reclamar!",     "Réclamer!",        "Abholen!",     "Resgatar!"),

        // --- Главное меню ---
        "counter_today"     to m("Total Push Ups Today", "Отжимания сегодня",    "Flexiones de hoy",      "Pompes aujourd'hui",     "Liegestütze heute",     "Flexões hoje"),
        "btn_reset"         to m("Reset",                "Сброс",                "Reiniciar",             "Réinitialiser",          "Zurücksetzen",          "Reiniciar"),
        "btn_stats"         to m("Stats",                "Статы",                "Stats",                 "Stats",                  "Werte",                 "Stats"),
        "battle_soon"       to m("Battle will begin soon...", "Бой начнётся скоро...", "La batalla comenzará pronto...", "La bataille va commencer bientôt...", "Der Kampf beginnt bald...", "A batalha começará em breve..."),
        "view_all_logs"     to m("→ view all logs",       "→ все логи",           "→ ver todos",           "→ voir tout",            "→ alle Logs",           "→ ver todos"),

        // --- Ежедневная награда ---
        "daily_reward"      to m("🎁 Daily Reward",  "🎁 Ежедневная награда", "🎁 Recompensa diaria",  "🎁 Récompense quotidienne", "🎁 Tägliche Belohnung", "🎁 Recompensa diária"),

        // --- Повышение уровня ---
        "levelup_title"     to m("Level Up!",   "Повышение уровня!", "Subida de nivel!",  "Montée de niveau!", "Level Up!",     "Subiu de nível!"),
        "btn_continue"      to m("Continue",    "Продолжить",        "Continuar",          "Continuer",         "Weiter",        "Continuar"),
        "btn_later"         to m("Later",       "Позже",             "Después",            "Plus tard",         "Später",        "Depois"),

        // --- Настройки — Персонаж ---
        "sec_character"     to m("Character",       "Персонаж",          "Personaje",           "Personnage",          "Charakter",         "Personagem"),
        "char_name"         to m("Character name",  "Имя персонажа",     "Nombre del personaje","Nom du personnage",   "Charaktername",     "Nome do personagem"),
        "hero_avatar"       to m("Hero avatar",     "Аватар героя",      "Avatar del héroe",    "Avatar du héros",     "Helden-Avatar",     "Avatar do herói"),

        // --- Настройки — Язык/Уведомления ---
        "sec_language"      to m("Language",        "Язык",              "Idioma",              "Langue",              "Sprache",           "Idioma"),
        "sec_notifications" to m("Notifications",   "Уведомления",       "Notificaciones",      "Notifications",       "Benachrichtigungen","Notificações"),
        "notif_label"       to m("Notifications",   "Уведомления",       "Notificaciones",      "Notifications",       "Benachrichtigungen","Notificações"),
        "notif_time"        to m("At 10:00 and 20:00","В 10:00 и 20:00", "A las 10:00 y 20:00", "À 10h00 et 20h00",   "Um 10:00 und 20:00","Às 10:00 e 20:00"),
        "btn_on"            to m("On",  "Вкл",  "Sí",  "Oui", "Ein", "Sim"),
        "btn_off"           to m("Off", "Выкл", "No",  "Non", "Aus", "Não"),

        // --- Настройки — Информация ---
        "sec_info"          to m("Information",     "Информация",        "Información",         "Informations",        "Informationen",     "Informações"),
        "info_pushups"      to m("Total push-ups",  "Всего отжиманий",   "Total flexiones",     "Total pompes",        "Ges. Liegestütze",  "Total flexões"),
        "info_monsters"     to m("Monsters killed", "Монстров убито",    "Monstruos matados",   "Monstres tués",       "Besiegte Monster",  "Monstros mortos"),
        "info_version"      to m("Version",         "Версия",            "Versión",             "Version",             "Version",           "Versão"),

        // --- Настройки — Выход / Сброс ---
        "sec_exit"          to m("Exit",                     "Выход",                   "Salir",                       "Quitter",                         "Beenden",                       "Sair"),
        "btn_exit_game"     to m("Exit Game",                "Выйти из игры",           "Salir del juego",             "Quitter le jeu",                  "Spiel beenden",                 "Sair do jogo"),
        "sec_danger"        to m("Danger zone",              "Опасная зона",            "Zona peligrosa",              "Zone dangereuse",                 "Gefahrenzone",                  "Zona de perigo"),
        "btn_reset_progress" to m("Reset progress",           "Сбросить прогресс",       "Reiniciar progreso",          "Réinitialiser la progression",    "Fortschritt zurücksetzen",      "Reiniciar progresso"),
        "confirm_reset_title" to m("Reset progress?",        "Сбросить прогресс?",      "Reiniciar progreso?",        "Réinitialiser?",                  "Zurücksetzen?",                 "Reiniciar progresso?"),
        "confirm_reset_msg" to m(
            "All progress, items and statistics will be permanently deleted.",
            "Весь прогресс, предметы и статистика будут удалены навсегда.",
            "Todo el progreso, objetos y estadísticas se eliminarán permanentemente.",
            "Toute la progression, les objets et les statistiques seront définitivement supprimés.",
            "Alle Fortschritte, Gegenstände und Statistiken werden dauerhaft gelöscht.",
            "Todo o progresso, itens e estatísticas serão apagados permanentemente."
        ),
        "confirm_reset_warn" to m(
            "This action is irreversible. All progress will be lost.",
            "Это действие необратимо. Весь прогресс будет удалён.",
            "Esta acción es irreversible. Todo el progreso se perderá.",
            "Cette action est irréversible. Toute la progression sera perdue.",
            "Diese Aktion ist unwiderruflich. Alle Fortschritte gehen verloren.",
            "Esta ação é irreversível. Todo o progresso será perdido."
        ),
        "btn_cancel"        to m("Cancel",         "Отмена",       "Cancelar",      "Annuler",     "Abbrechen",   "Cancelar"),
        "input_name"        to m("Enter name...",  "Введи имя...", "Ingresa nombre...","Entrez le nom...","Name eingeben...","Digite o nome..."),

        // --- Магазин ---
        "btn_reroll"        to m("Reroll",      "Обновить",     "Renovar",              "Relancer",         "Neu würfeln",          "Renovar"),
        "shop_empty"        to m("Shop is empty","Магазин пуст", "Tienda vacía",         "Boutique vide",    "Shop leer",            "Loja vazia"),
        "btn_buy"           to m("Buy",          "Купить",       "Comprar",              "Acheter",          "Kaufen",               "Comprar"),
        "insufficient_teeth" to m("Not enough teeth!", "Недостаточно зубов!", "No hay suficientes dientes!", "Pas assez de dents!", "Nicht genug Zähne!", "Dentes insuficientes!"),

        // --- Кузница ---
        "forge"             to m("Forge",        "Кузница",      "Forja",                "Forge",            "Schmiede",             "Forja"),
        "btn_merge"         to m("Merge",        "Сплавить",     "Fusionar",             "Fusionner",        "Verschmelzen",         "Fundir"),
        "forge_need_two"    to m("Need 2 items in forge!","Нужно 2 предмета в кузнице!","Necesitas 2 objetos en la forja!","Besoin de 2 objets dans la forge!","Benötige 2 Gegenstände!","Precisa de 2 itens na forja!"),

        // --- Клеверная коробка ---
        "clover_box"        to m("Clover Box",       "Клеверная коробка",  "Caja de trébol",           "Boîte Trèfle",             "Kleeblatt-Box",            "Caixa Trevo"),
        "clover_free_item"  to m("Free item",        "Бесплатный предмет", "Objeto gratis",             "Objet gratuit",            "Gratis-Gegenstand",        "Item grátis"),
        "btn_get"           to m("Get",              "Получить",           "Obtener",                   "Obtenir",                  "Holen",                    "Obter"),
        "clover_free_pts"   to m("Free points",      "Бесплатные очки",    "Puntos gratis",             "Points gratuits",          "Gratis-Punkte",            "Pontos grátis"),
        "clover_bonus"      to m("+2 stat points!",  "+2 очка характеристик!","+2 puntos de estadísticas!","+2 points de stats!",  "+2 Werte-Punkte!",         "+2 pontos de atributos!"),
        "clover_limit"      to m("Used today (max 2)","Использовано на сегодня (макс. 2)","Usado hoy (máx. 2)","Utilisé aujourd'hui (max 2)","Heute verwendet (max 2)","Usado hoje (máx. 2)"),

        // --- Точильный камень ---
        "grindstone"        to m("Grindstone",           "Точильный камень",  "Piedra de afilar",         "Pierre à aiguiser",        "Schleifstein",             "Pedra de afiar"),
        "grindstone_effect" to m("Item +1 to all stats", "Вещь +1 к статам", "Objeto +1 a todos los atrib.","Objet +1 à tous les stats","Gegenstand +1 zu allen Werten","Item +1 em todos os atrib."),
        "btn_enchant"       to m("Enchant",              "Заточить",          "Encantar",                 "Enchanter",                "Verzaubern",               "Encantar"),
        "enchant_max"       to m("Maximum level +9!",    "Максимальный уровень +9!", "Nivel máximo +9!",  "Niveau maximum +9!",       "Maximales Level +9!",      "Nível máximo +9!"),

        // --- Выбор предмета ---
        "item_picker_title" to m("Select item",          "Выбери предмет",    "Seleccionar objeto",       "Sélectionner un objet",    "Gegenstand wählen",        "Selecionar item"),
        "item_picker_empty" to m("No available items",   "Нет доступных предметов","Sin objetos disponibles","Pas d'objets disponibles","Keine Gegenstände verfügbar","Sem itens disponíveis"),

        // --- Статистика ---
        "stats_last_week"   to m("Last week",       "Последняя неделя",     "Última semana",    "Dernière semaine",     "Letzte Woche",     "Última semana"),
        "stats_no_data"     to m("No data for this week","Нет данных за эту неделю","Sin datos esta semana","Pas de données pour cette semaine","Keine Daten für diese Woche","Sem dados desta semana"),
        "period_week"       to m("Last week",       "За последнюю неделю",  "Última semana",    "Dernière semaine",     "Letzte Woche",     "Última semana"),
        "period_month"      to m("Last month",      "За последний месяц",   "Último mes",       "Dernier mois",         "Letzter Monat",    "Último mês"),
        "period_quarter"    to m("Last quarter",    "За последний квартал", "Último trimestre", "Dernier trimestre",    "Letztes Quartal",  "Último trimestre"),
        "period_year"       to m("Last year",       "За последний год",     "Último año",       "Dernière année",       "Letztes Jahr",     "Último ano"),
        "period_all"        to m("Total",           "За всё время",         "Total",            "Total",                "Gesamt",           "Total"),
        "streak_current"    to m("Current Streak",  "Текущий стрик",        "Racha actual",     "Série actuelle",       "Aktuelle Serie",   "Sequência atual"),
        "streak_days"       to m("days",            "дн.",                  "días",             "jours",                "Tage",             "dias"),
        "streak_best"       to m("Longest Streak",  "Лучший стрик",         "Mejor racha",      "Meilleure série",      "Längste Serie",    "Melhor sequência"),
        "rpg_stats"         to m("RPG Stats",       "RPG Статистика",       "Stats RPG",        "Stats RPG",            "RPG-Werte",        "Stats RPG"),
        "items_collected"   to m("Items collected", "Предметов получено",   "Objetos obtenidos","Objets collectés",     "Ges. Gegenstände", "Itens coletados"),
        "dmg_dealt"         to m("DMG dealt",       "Урона нанесено",       "Daño infligido",   "Dégâts infligés",      "Schaden verursacht","Dano causado"),
        "dmg_highest"       to m("Highest DMG",     "Макс. удар",           "Mayor daño",       "Dégâts max",           "Höchster Schaden", "Maior dano"),
        "enemies_killed"    to m("Enemies killed",  "Врагов убито",         "Enemigos eliminados","Ennemis vaincus",    "Besiegte Feinde",  "Inimigos mortos"),
        "char_born"         to m("Character born",  "Дата рождения",        "Personaje creado", "Personnage créé",      "Charakter erstellt","Personagem criado"),
        "unknown"           to m("Unknown",         "Неизвестно",           "Desconocido",      "Inconnu",              "Unbekannt",        "Desconhecido"),
        "best_session"      to m("Best session",    "Рекорд сессии",        "Mejor sesión",     "Meilleure session",    "Beste Session",    "Melhor sessão"),
        "crit_hits"         to m("Critical hits",   "Крит. ударов",         "Golpes críticos",  "Coups critiques",      "Kritische Treffer","Acertos críticos"),
        "enchants_done"     to m("Enchants done",   "Успешных заточек",     "Encantos realizados","Enchantements effectués","Verzauberungen", "Encantos realizados"),
        "items_merged"      to m("Items merged",    "Вещей переработано",   "Objetos fusionados","Objets fusionnés",    "Verschmolzen",     "Itens fundidos"),
        "teeth_spent"       to m("Teeth spent",     "Зубов потрачено",      "Dientes gastados", "Dents dépensées",      "Ausgegebene Zähne","Dentes gastos"),
        "teeth_earned"      to m("Teeth earned",    "Зубов заработано",     "Dientes ganados",  "Dents gagnées",        "Verdiente Zähne",  "Dentes ganhos"),
        "highest_monster"   to m("Highest monster", "Макс. уровень монстра","Mayor monstruo",   "Monstre max",          "Höchster Monster", "Maior monstro"),

        // --- Журнал предметов ---
        "itemlog_empty"     to m("No items collected yet","Предметы ещё не получены","Aún no se han obtenido objetos","Aucun objet collecté","Noch keine Gegenstände","Nenhum item coletado"),

        // --- Журнал боя ---
        "logs_empty"        to m("No events yet.\nStart training!","Событий пока нет.\nНачни тренировку!","Sin eventos aún.\nEmpieza a entrenar!","Aucun événement.\nCommence l'entraînement!","Noch keine Ereignisse.\nFang an zu trainieren!","Sem eventos ainda.\nComece a treinar!"),

        // --- Прогресс ---
        "unlocked"          to m("unlocked",          "открыто",           "desbloqueado",      "débloqué",         "freigeschaltet",    "desbloqueado"),
        "encountered"       to m("monsters encountered","монстров встречено","monstruos encontrados","monstres rencontrés","Monster angetroffen","monstros encontrados"),
        "last_items"        to m("Last",               "Последние",         "Últimos",           "Derniers",         "Letzte",            "Últimos"),
        "items_word"        to m("items",              "предметов",         "objetos",           "objets",           "Gegenstände",       "itens"),
    )

    fun t(lang: String, key: String): String = data[key]?.get(lang) ?: data[key]?.get("en") ?: key
}
