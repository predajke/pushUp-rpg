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

        // --- Онбординг (7 шагов) ---
        "onboard_step_title_0" to m("Welcome to PushUpRPG!", "Добро пожаловать в PushUpRPG!", "¡Bienvenido a PushUpRPG!", "Bienvenue dans PushUpRPG!", "Willkommen bei PushUpRPG!", "Bem-vindo ao PushUpRPG!"),
        "onboard_step_desc_0" to m(
            "Hey there! This app is a push-up counter with a twist. Here you enter your push-ups: +1 or +10. Know math? Good! And don't forget to press Save.",
            "Приветствую тебя, как ты догадался это приложение ничто иное как счётчик для твоих отжиманий, но с некоторыми... вообщем тут ты вводишь свои отжимания, +1 -1, математику же знаешь? Ну и Save не забудь нажать.",
            "¡Hola! Esta aplicación es un contador de flexiones con un giro. Aquí ingresas tus flexiones: +1 o +10. ¿Sabes matemáticas? ¡Bien! Y no olvides presionar Guardar.",
            "Salut! Cette application est un compteur de pompes avec une touche. Ici tu entres tes pompes: +1 ou +10. Tu connais les maths? Bien! Et n'oublie pas d'appuyer sur Enregistrer.",
            "Hallo! Diese App ist ein Liegestütz-Zähler mit einem Twist. Hier gibst du deine Liegestütze ein: +1 oder +10. Kennst du Mathe? Gut! Und vergiss nicht, Speichern zu drücken.",
            "Olá! Este aplicativo é um contador de flexões com uma reviravolta. Aqui você insere seus flexões: +1 ou +10. Você sabe matemática? Ótimo! E não esqueça de pressionar Salvar."
        ),

        "onboard_step_title_1" to m("Your Wardrobe", "Твой гардероб", "Tu guardarropa", "Ta garde-robe", "Deine Garderobe", "Seu guarda-roupa"),
        "onboard_step_desc_1" to m(
            "This little panel opens the door to other worlds. Equip yourself with cool gear and level up your stats!",
            "Эта маленькая панелька - открывает дверь в мир других, короче там ты одеваешься, если есть во что, и качаешь поинты",
            "Este pequeño panel abre la puerta a otros mundos. ¡Equípate con cosas geniales y sube de nivel tus estadísticas!",
            "Ce petit panneau ouvre la porte à d'autres mondes. Équipe-toi avec des trucs cool et monte tes statistiques!",
            "Dieses kleine Panel öffnet die Tür zu anderen Welten. Rüste dich mit coolen Sachen aus und levele deine Werte!",
            "Este pequeno painel abre a porta para outros mundos. Equipe-se com coisas legais e suba suas estatísticas!"
        ),

        "onboard_step_title_2" to m("The Bazaar", "Базар", "El Bazar", "Le Bazar", "Der Basar", "O Bazar"),
        "onboard_step_desc_2" to m(
            "This is not just a shop, it's a BAZAAR! Got everything: items, forge, enchanters, and FREE GIFTS! 😉",
            "Это не просто магазин, это просто БАЗАР, там есть всё, и вещички, и кузница и маги зачаровщики, а и БЕСПЛАТНЫЕ ПОДАРОЧКИ ;)",
            "¡Esto no es solo una tienda, es un BAZAR! Tiene todo: artículos, forja, encantadores, ¡y REGALOS GRATIS! 😉",
            "Ce n'est pas juste une boutique, c'est un BAZAR! Il y a tout: objets, forge, enchanteurs, et CADEAUX GRATUITS! 😉",
            "Das ist nicht nur ein Shop, das ist ein BASAR! Alles drin: Gegenstände, Schmiede, Verzauberer und KOSTENLOSE GESCHENKE! 😉",
            "Isto não é apenas uma loja, é um BAZAR! Tem tudo: itens, forja, encantadores, e PRESENTES GRÁTIS! 😉"
        ),

        "onboard_step_title_3" to m("Endless Battle", "Бесконечная битва", "Batalla infinita", "Bataille infinie", "Endlose Schlacht", "Batalha infinita"),
        "onboard_step_desc_3" to m(
            "Here's an endless battle: your push-ups vs. your laziness. Ha! You're on the left, and looks like you got some problems. 😄",
            "Тут идёт бесконечная битва, между твоми отжиманиями и твоей ленью, ХА! (ты слева если что и похоже у тебя проблемы)",
            "Aquí hay una batalla infinita: tus flexiones vs. tu pereza. ¡Ja! Estás a la izquierda, ¡y parece que tienes problemas!",
            "Il y a une bataille infinie ici: tes pompes vs. ta paresse. Ha! Tu es à gauche, et on dirait que tu as des problèmes!",
            "Hier ist eine endlose Schlacht: deine Liegestütze gegen deine Faulheit. Ha! Du bist links, und es sieht aus, als hättest du Probleme!",
            "Aqui há uma batalha infinita: suas flexões vs. sua preguiça. Ha! Você está à esquerda, e parece que tem problemas!"
        ),

        "onboard_step_title_4" to m("Battle Logs", "Журнал боя", "Registro de batalla", "Journal de combat", "Kampfprotokoll", "Registro de batalha"),
        "onboard_step_desc_4" to m(
            "Here are the logs showing how many times you got beaten, and maybe won a few (probably not). Battle runs automatically even when you're offline, so don't worry!",
            "Тут логи, где показано сколько раз ты получил по лицу, ну и возможно победил кого то (вряд ли), бой идёт в автоматическом режиме даже когда ты не в приложение, так что не беспокойся.",
            "Aquí están los registros mostrando cuántas veces te golpearon, y tal vez ganaste unos pocos (probablemente no). La batalla se ejecuta automáticamente incluso cuando estás fuera de línea.",
            "Voici les journaux montrant combien de fois tu t'es fait battre, et peut-être que tu as gagné quelques fois (probablement pas). La bataille s'exécute automatiquement même hors ligne.",
            "Hier sind die Logs, die zeigen, wie oft du geschlagen wurdest, und vielleicht hast du ein paar mal gewonnen (wahrscheinlich nicht). Der Kampf läuft automatisch, auch wenn du offline bist.",
            "Aqui estão os registros mostrando quantas vezes você foi derrotado, e talvez venceu alguns (provavelmente não). A batalha é executada automaticamente mesmo quando você está offline."
        ),

        "onboard_step_title_5" to m("Quests & Achievements", "Квесты и ачивки", "Misiones y logros", "Quêtes et réalisations", "Quests und Erfolge", "Missões e conquistas"),
        "onboard_step_desc_5" to m(
            "Here are tabs with daily quests—get rewards, unlock achievements. Good luck out there, and let's do those push-ups! I'm watching. 💪",
            "Тут вкладки с ежедневными квестами, опять же за вознограждение ну и все ачивки которые ты смог открыть. Удачи тебе и давай отжимайся, я слежу.",
            "Aquí hay pestañas con misiones diarias: obtén recompensas, desbloquea logros. Buena suerte, ¡y hagamos esas flexiones! Estoy mirando. 💪",
            "Voici des onglets avec des quêtes quotidiennes: obtiens des récompenses, débloque les réalisations. Bonne chance, et faisons ces pompes! Je regarde. 💪",
            "Hier sind Registerkarten mit täglichen Quests: Belohnungen erhalten, Erfolge freischalten. Viel Glück, und lass uns diese Liegestütze machen! Ich beobachte. 💪",
            "Aqui estão abas com missões diárias: ganhe recompensas, desbloqueie conquistas. Boa sorte, e vamos fazer esses flexões! Estou observando. 💪"
        ),

        "onboard_skip"      to m("Skip Tutorial", "Пропустить", "Saltar", "Passer", "Überspringen", "Pular"),
        "onboard_next"      to m("Next", "Дальше", "Siguiente", "Suivant", "Weiter", "Próximo"),

        // Ad & Anti-Cheat
        "ad_title"          to m("Watch Ad", "Смотреть Рекламу", "Ver Anuncio", "Regarder Pub", "Anzeige Ansehen", "Assistir Anúncio"),
        "ad_description_cheat" to m("Slow down! You're doing great! 💪\n\nWatch an ad to continue training...", "Медленнее! Ты отлично работаешь! 💪\n\nПосмотри рекламу и продолжи тренировку...", "¡Más lento! ¡Lo estás haciendo genial! 💪\n\nMira un anuncio para continuar entrenando...", "Ralentir! Tu fais du super travail! 💪\n\nRegarde une pub pour continuer l'entraînement...", "Verlangsam! Du machst es großartig! 💪\n\nSchau dir eine Anzeige an und trainiere weiter...", "Mais devagar! Você está indo muito bem! 💪\n\nAssista a um anúncio para continuar treinando..."),
        "ad_button_watch"   to m("Watch Ad (10s skip)", "Смотреть (skip через 10s)", "Ver Anuncio (skip en 10s)", "Regarder (skip dans 10s)", "Anzeige (skip in 10s)", "Assistir (skip em 10s)"),
        "ad_button_skip"    to m("Skip Ad", "Пропустить", "Saltar", "Passer", "Überspringen", "Pular"),
    )

    fun t(lang: String, key: String): String = data[key]?.get(lang) ?: data[key]?.get("en") ?: key
}
