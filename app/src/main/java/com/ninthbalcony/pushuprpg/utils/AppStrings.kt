package com.ninthbalcony.pushuprpg.utils

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
        "btn_delete_account" to m("Delete account & data",    "Удалить аккаунт и данные","Eliminar cuenta y datos",     "Supprimer compte et données",     "Konto und Daten löschen",       "Excluir conta e dados"),
        "delete_account_warn" to m("Permanently removes cloud save, leaderboard entry, friend code, and Firebase account.", "Безвозвратно удаляет облачное сохранение, запись в лидерборде, friend code и Firebase-аккаунт.", "Elimina permanentemente el guardado en la nube, la entrada del ranking, el código de amigo y la cuenta de Firebase.", "Supprime définitivement la sauvegarde cloud, le classement, le code ami et le compte Firebase.", "Löscht dauerhaft Cloud-Speicher, Bestenliste, Freundescode und Firebase-Konto.", "Remove permanentemente o salvamento na nuvem, classificação, código de amigo e conta do Firebase."),
        "confirm_delete_title" to m("Delete account?",        "Удалить аккаунт?",        "¿Eliminar cuenta?",           "Supprimer le compte?",            "Konto löschen?",                "Excluir conta?"),
        "confirm_delete_msg" to m("This action is irreversible. All cloud data, leaderboard position, and friend code will be permanently deleted.", "Действие необратимо. Все данные в облаке, позиция в лидерборде и friend code будут удалены навсегда.", "Esta acción es irreversible. Todos los datos en la nube, la posición en el ranking y el código de amigo se eliminarán permanentemente.", "Cette action est irréversible. Toutes les données cloud, le classement et le code ami seront supprimés définitivement.", "Diese Aktion ist unwiderruflich. Alle Cloud-Daten, Bestenlisten-Position und Freundescode werden dauerhaft gelöscht.", "Esta ação é irreversível. Todos os dados na nuvem, posição na classificação e código de amigo serão excluídos permanentemente."),
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
        "stats_last_year"   to m("Last year",        "Последний год",        "Último año",       "Dernière année",       "Letztes Jahr",     "Último ano"),
        "stats_no_data"     to m("No data for this week","Нет данных за эту неделю","Sin datos esta semana","Pas de données pour cette semaine","Keine Daten für diese Woche","Sem dados desta semana"),
        "stats_this_week"   to m("This week",        "Эта неделя",           "Esta semana",      "Cette semaine",        "Diese Woche",      "Esta semana"),
        "stats_this_year"   to m("This year",        "Этот год",             "Este año",         "Cette année",          "Dieses Jahr",      "Este ano"),
        "stats_daily_avg"   to m("Daily avg",        "Среднее/день",         "Promedio/día",     "Moy./jour",            "Ø/Tag",            "Média/dia"),
        "stats_best_day"    to m("Best day",         "Лучший день",          "Mejor día",        "Meilleur jour",        "Bester Tag",       "Melhor dia"),
        "stats_best_month"  to m("Best month",       "Лучший месяц",         "Mejor mes",        "Meilleur mois",        "Bester Monat",     "Melhor mês"),
        "period_week"       to m("Last week",       "За последнюю неделю",  "Última semana",    "Dernière semaine",     "Letzte Woche",     "Última semana"),
        "period_month"      to m("Last month",      "За последний месяц",   "Último mes",       "Dernier mois",         "Letzter Monat",    "Último mês"),
        "period_quarter"    to m("Last quarter",    "За последний квартал", "Último trimestre", "Dernier trimestre",    "Letztes Quartal",  "Último trimestre"),
        "period_year"       to m("Last year",       "За последний год",     "Último año",       "Dernière année",       "Letztes Jahr",     "Último ano"),
        "period_all"        to m("Total",           "За всё время",         "Total",            "Total",                "Gesamt",           "Total"),
        "streak_current"    to m("Current Streak",  "Текущий стрик",        "Racha actual",     "Série actuelle",       "Aktuelle Serie",   "Sequência atual"),
        "streak_days"       to m("days",            "дн.",                  "días",             "jours",                "Tage",             "dias"),
        "streak_best"       to m("Longest Streak",  "Лучший стрик",         "Mejor racha",      "Meilleure série",      "Längste Serie",    "Melhor sequência"),
        "rpg_stats"         to m("RPG Stats",       "RPG Статистика",       "Stats RPG",        "Stats RPG",            "RPG-Werte",        "Stats RPG"),
        "tonnage_stats"     to m("Tonnage & Calories","Тоннаж и калории",   "Tonelaje y calorías","Tonnage et calories", "Tonnage & Kalorien","Tonelagem e calorias"),
        "tonnage_total"     to m("Total lifted",     "Поднято всего",        "Total levantado",  "Total soulevé",        "Insgesamt gehoben","Total levantado"),
        "tonnage_today"     to m("Lifted today",     "За сегодня",           "Hoy",              "Aujourd'hui",          "Heute",            "Hoje"),
        "tonnage_week"      to m("Last 7 days",      "За 7 дней",            "Últimos 7 días",   "7 derniers jours",     "Letzte 7 Tage",    "Últimos 7 dias"),
        "calories_total"    to m("Calories burned",  "Калорий сожжено",      "Calorías quemadas","Calories brûlées",     "Kalorien verbrannt","Calorias queimadas"),
        "set_weight_prompt" to m("Set body weight in Settings to see tonnage", "Укажите вес тела в Settings, чтобы видеть тоннаж", "Configura tu peso en Ajustes para ver el tonelaje", "Définissez votre poids dans Paramètres pour voir le tonnage", "Körpergewicht in Einstellungen festlegen für Tonnage", "Defina o peso corporal em Configurações para ver a tonelagem"),
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
        "ad_reward_title"   to m("Watch Ad", "Смотреть рекламу", "Ver anuncio", "Regarder pub", "Werbung sehen", "Ver anúncio"),
        "ad_reward_desc"    to m("Watch a short ad to earn bonus teeth!", "Посмотри рекламу и получи бонусные зубы!", "¡Mira un anuncio y gana dientes!", "Regarde une pub et gagne des dents!", "Schau eine Anzeige und verdiene Zähne!", "Assista a um anúncio e ganhe dentes!"),
        "btn_watch_ad"      to m("Watch", "Смотреть", "Ver", "Regarder", "Ansehen", "Assistir"),

        // Rate Us
        "rate_us_title"     to m("Enjoying PushUpRPG?", "Нравится PushUpRPG?", "¿Te gusta PushUpRPG?", "Aimez-vous PushUpRPG?", "Gefällt dir PushUpRPG?", "Está gostando do PushUpRPG?"),
        "rate_us_description" to m("If you're having a great time, please take a moment to rate the app. Your feedback helps us improve!", "Если тебе нравится, оцени приложение. Твой отзыв помогает нам улучшаться!", "Si lo estás disfrutando, tómate un momento para calificar la app. ¡Tu opinión nos ayuda a mejorar!", "Si tu t'amuses, prends un moment pour évaluer l'app. Ton avis nous aide à nous améliorer!", "Wenn dir das gefällt, nimm dir einen Moment Zeit, um die App zu bewerten. Dein Feedback hilft uns zu verbessern!", "Se você está se divertindo, avalie o app. Seu feedback nos ajuda a melhorar!"),
        "btn_rate_now"      to m("Rate Now", "Оценить", "Calificar Ahora", "Évaluer Maintenant", "Jetzt Bewerten", "Avaliar Agora"),
        "btn_remind_later"  to m("Remind Later", "Напомнить Позже", "Recordar Después", "Me Rappeler Plus Tard", "Später Erinnern", "Lembrar Depois"),
        "btn_never_ask"     to m("Never Ask", "Больше не спрашивать", "Nunca Preguntar", "Ne Pas Demander", "Nicht Mehr Fragen", "Nunca Perguntar"),

        // --- Common buttons ---
        "close"             to m("Close",       "Закрыть",      "Cerrar",   "Fermer",   "Schließen",    "Fechar"),
        "btn_ok"            to m("OK",           "ОК",           "OK",       "OK",       "OK",           "OK"),
        "btn_skip"          to m("Skip",         "Пропустить",   "Saltar",   "Passer",   "Überspringen", "Pular"),
        "btn_well"          to m("Well",         "Ну",           "Bueno",    "Bon",      "Na ja",        "Bem"),
        "copied"            to m("Copied",       "Скопировано",  "Copiado",  "Copié",    "Kopiert",      "Copiado"),
        "btn_copy"          to m("Copy",         "Копировать",   "Copiar",   "Copier",   "Kopieren",     "Copiar"),
        "reward_label"      to m("Reward:",      "Награда:",     "Recompensa:","Récompense:","Belohnung:", "Recompensa:"),

        // --- Pickers ---
        "choose_country"    to m("Choose country",  "Выберите страну",  "Elegir país",      "Choisir un pays",  "Land auswählen",   "Escolher país"),
        "choose_avatar"     to m("Choose avatar",   "Выбор аватара",    "Elegir avatar",    "Choisir avatar",   "Avatar wählen",    "Escolher avatar"),
        "search_hint"       to m("Search…",         "Поиск…",           "Buscar…",          "Rechercher…",      "Suchen…",          "Pesquisar…"),

        // --- Streak dialogs ---
        "streak_in_row"     to m("in a row",        "подряд",           "seguidos",         "d'affilée",        "in Folge",         "seguidos"),
        "streak_reward_ready" to m("reward ready!", "награда готова!",  "¡recompensa lista!","récompense prête!","Belohnung bereit!","recompensa pronta!"),
        "streak_all_claimed" to m("All rewards claimed! Incredible streak.", "Все награды получены! Невероятный streak.", "¡Todas las recompensas obtenidas! Racha increíble.", "Toutes les récompenses obtenues ! Série incroyable.", "Alle Belohnungen erhalten! Unglaubliche Serie.", "Todas as recompensas obtidas! Sequência incrível."),
        "streak_day_unlocked" to m("unlocked!",     "разблокирован!",   "¡desbloqueado!",   "débloqué !",       "freigeschaltet!",  "desbloqueado!"),

        // --- Anti-cheat dialog ---
        "anticheat_title"   to m("Wait a Moment",   "Подождите",        "Espera un momento",    "Attendez un moment",   "Einen Moment bitte",   "Aguarde um momento"),
        "anticheat_body"    to m(
            "We detected a rapid save. This is likely an error. Please wait before saving again.",
            "Обнаружено быстрое сохранение. Вероятно, это ошибка. Подождите перед следующим сохранением.",
            "Detectamos un guardado rápido. Probablemente es un error. Espera antes de guardar.",
            "Nous avons détecté une sauvegarde rapide. Veuillez patienter avant de sauvegarder.",
            "Schnelles Speichern erkannt. Bitte warte vor dem nächsten Speichern.",
            "Detectamos um salvamento rápido. Aguarde antes de salvar novamente."
        ),
        "anticheat_footer"  to m(
            "Rapid saves are prevented to ensure fair gameplay.",
            "Быстрые сохранения запрещены для честной игры.",
            "Los guardados rápidos están prohibidos para garantizar un juego justo.",
            "Les sauvegardes rapides sont interdites pour un jeu équitable.",
            "Schnelles Speichern gesperrt für faires Spielen.",
            "Salvamentos rápidos impedidos para jogo justo."
        ),
        "anticheat_cooldown" to m("Cooldown:", "Перезарядка:", "Enfriamiento:", "Recharge:", "Abklingzeit:", "Recarga:"),

        // --- Shop dialogs ---
        "no_teeth_title"    to m("Come back later",     "Загляни позже",        "Vuelve más tarde",     "Reviens plus tard",    "Komm später wieder",   "Volte mais tarde"),
        "no_teeth_body"     to m("Not enough teeth...", "Не хватает зубов...",  "No hay suficientes dientes...", "Pas assez de dents...", "Nicht genug Zähne...", "Dentes insuficientes..."),

        // --- Cloud restore ---
        "cloud_restored"    to m("Progress restored from cloud", "Прогресс восстановлен из облака", "Progreso restaurado de la nube", "Progression restaurée depuis le cloud", "Fortschritt aus der Cloud wiederhergestellt", "Progresso restaurado da nuvem"),
        "push_ups_short"    to m("push-ups", "отж.", "flexiones", "pompes", "Liegestütze", "flexões"),

        // --- Navigation ---
        "leaderboard"       to m("Leaderboard",     "Таблица лидеров",  "Marcador",             "Classement",           "Bestenliste",          "Placar"),

        // --- Main Menu extras ---
        "next_attack"       to m("Next attack:",    "След. атака:",     "Próx. ataque:",        "Proch. attaque:",      "Nächster Angriff:",    "Próx. ataque:"),
        "golden_goblin"     to m("Golden Goblin",   "Золотой Гоблин",   "Duende Dorado",        "Gobelin d'Or",         "Goldener Kobold",      "Goblin Dourado"),
        "goblin_escaped"    to m("Goblin escaped!", "Гоблин сбежал!",   "¡El duende escapó!",   "Le gobelin s'est enfui!","Kobold entkommen!",   "Goblin escapou!"),
        "teeth_earned_popup" to m("You earned",     "Ты получил",       "Ganaste",              "Tu as gagné",          "Du hast verdient",     "Você ganhou"),
        "awesome"           to m("Awesome!",        "Отлично!",         "¡Genial!",             "Super!",               "Toll!",                "Incrível!"),
        "day_label"         to m("Day",             "День",             "Día",                  "Jour",                 "Tag",                  "Dia"),
        "next_reward"       to m("Next reward:",    "Следующая награда:","Próxima recompensa:",  "Prochaine récompense:","Nächste Belohnung:",   "Próxima recompensa:"),
        "days_to_go"        to m("days to go",      "дн. осталось",     "días restantes",       "jours restants",       "Tage verbleiben",      "dias restantes"),
        "stat_points_label" to m("Stat points:",    "Очков:",           "Puntos:",              "Points:",              "Punkte:",              "Pontos:"),
        "item_rarity_label" to m("item",            "предмет",          "objeto",               "objet",                "Gegenstand",           "item"),

        // --- Bestiary ---
        "damage_label"      to m("Damage",          "Урон",             "Daño",                 "Dégâts",               "Schaden",              "Dano"),
        "drop_label"        to m("Drop",            "Дроп",             "Botín",                "Butin",                "Beute",                "Loot"),

        // --- Item Log ---
        "sets_label"        to m("Sets",            "Сеты",             "Sets",                 "Sets",                 "Sets",                 "Sets"),

        // --- Leaderboard screen ---
        "lb_global"         to m("Global",          "Глобально",        "Global",               "Mondial",              "Global",               "Global"),
        "lb_country"        to m("Country",         "Страна",           "País",                 "Pays",                 "Land",                 "País"),
        "lb_friends"        to m("Friends",         "Друзья",           "Amigos",               "Amis",                 "Freunde",              "Amigos"),
        "lb_period_day"     to m("Day",             "День",             "Día",                  "Jour",                 "Tag",                  "Dia"),
        "lb_period_week"    to m("Week",            "Неделя",           "Semana",               "Semaine",              "Woche",                "Semana"),
        "lb_period_month"   to m("Month",           "Месяц",            "Mes",                  "Mois",                 "Monat",                "Mês"),
        "lb_period_all"     to m("All Time",        "За всё",           "Todo el tiempo",       "Depuis toujours",      "Gesamte Zeit",         "Todo o tempo"),
        "lb_searching"      to m("Searching players...", "Ищем игроков...", "Buscando jugadores...", "Recherche joueurs...", "Spieler suchen...", "Procurando jogadores..."),
        "lb_filter_name"    to m("Filter by name…", "Фильтр по имени…", "Filtrar por nombre…",  "Filtrer par nom…",     "Nach Name filtern…",   "Filtrar por nome…"),
        "lb_empty"          to m("Be first! Leaderboard is empty.", "Будь первым! Лидерборд пока пуст.", "¡Sé el primero! Marcador vacío.", "Sois le premier! Classement vide.", "Sei der Erste! Bestenliste leer.", "Seja o primeiro! Placar vazio."),
        "lb_push_ups_header" to m("PUSH-UPS",       "ОТЖИМАНИЯ",        "FLEXIONES",            "POMPES",               "LIEGESTÜTZE",          "FLEXÕES"),
        "lb_streak_header"  to m("LONGEST STREAK",  "ДЛИННЫЙ СТРИК",    "RACHA MÁS LARGA",      "PLUS LONGUE SÉRIE",    "LÄNGSTE SERIE",        "MAIOR SEQUÊNCIA"),
        "lb_teeth_header"   to m("TOTAL TEETH",     "ВСЕГО ЗУБОВ",      "TOTAL DIENTES",        "TOTAL DENTS",          "GESAMTE ZÄHNE",        "TOTAL DENTES"),
        "lb_code"           to m("Code:",           "Код:",             "Código:",              "Code:",                "Code:",                "Código:"),
        "lb_already_friend" to m("✓ Already friend","✓ В друзьях",      "✓ Ya amigo",           "✓ Déjà ami",           "✓ Bereits Freund",     "✓ Já amigo"),
        "lb_add_friend"     to m("+ Add friend",    "+ Добавить",       "+ Añadir amigo",       "+ Ajouter ami",        "+ Freund hinzufügen",  "+ Adicionar amigo"),
        "lb_prestige"       to m("Prestige",        "Ресет",            "Prestigio",            "Prestige",             "Prestige",             "Prestígio"),

        // --- Inventory extras ---
        "reset_bonus_label" to m("🔄 Reset Bonus:",    "🔄 Бонус ресета:",       "🔄 Bono Reinicio:",     "🔄 Bonus Réinit.:",    "🔄 Reset-Bonus:",          "🔄 Bônus Reinício:"),
        "achievement_boosts_label" to m("Achievement Boosts:", "Бонусы достижений:", "Mejoras Logros:", "Boosts Succès:", "Erfolgs-Boni:", "Bônus Conquistas:"),
        "set_modifiers_label" to m("Set Modifiers:", "Модификаторы наборов:", "Modificadores Set:", "Modificateurs Set:", "Set-Modifikatoren:", "Modificadores Set:"),
        "stat_points_dist"  to m("Stat points to distribute:", "Очков для распределения:", "Puntos para distribuir:", "Points à distribuer:", "Punkte zu verteilen:", "Pontos a distribuir:"),
        "tonnage_label"     to m("Tonnage",         "Тоннаж",           "Tonelaje",             "Tonnage",              "Tonnage",              "Tonelagem"),

        // --- Profile ---
        "play_games_not_connected" to m("Play Games — not connected", "Play Games — не подключено", "Play Games — no conectado", "Play Games — non connecté", "Play Games — nicht verbunden", "Play Games — não conectado"),
        "save_progress_desc" to m("Save progress across reinstalls", "Сохрани прогресс между переустановками", "Guarda progreso entre reinstalaciones", "Sauvegarde entre réinstallations", "Fortschritt bei Neuinstallation sichern", "Salva progresso entre reinstalações"),
        "sign_out"          to m("Sign out",        "Выйти",            "Cerrar sesión",        "Se déconnecter",       "Abmelden",             "Sair"),
        "sign_in"           to m("Sign in",         "Войти",            "Iniciar sesión",       "Se connecter",         "Anmelden",             "Entrar"),
        "change_avatar_btn" to m("Change Avatar →", "Сменить аватар →", "Cambiar avatar →",     "Changer avatar →",     "Avatar ändern →",      "Mudar avatar →"),
        "your_code"         to m("Your code:",      "Мой код:",         "Tu código:",           "Ton code:",            "Dein Code:",           "Seu código:"),
        "friend_code_label" to m("Friend code",     "Код друга",        "Código de amigo",      "Code d'ami",           "Freundescode",         "Código de amigo"),
        "btn_add"           to m("Add",             "Добавить",         "Añadir",               "Ajouter",              "Hinzufügen",           "Adicionar"),
        "friends_header"    to m("Friends",         "Друзья",           "Amigos",               "Amis",                 "Freunde",              "Amigos"),
        "friends_empty"     to m("List is empty. Share your code or add a friend by their code.", "Список пуст. Поделись своим кодом или добавь друга по коду.", "Lista vacía. Comparte tu código.", "Liste vide. Partage ton code.", "Liste leer. Teile deinen Code.", "Lista vazia. Compartilhe seu código."),
        "color_label"       to m("Color:",          "Цвет:",            "Color:",               "Couleur:",             "Farbe:",               "Cor:"),

        // --- Settings extras ---
        "profile_tab"       to m("Profile",         "Профиль",          "Perfil",               "Profil",               "Profil",               "Perfil"),
        "sound_vibration_section" to m("🔊 Sound & Vibration", "🔊 Звук и вибрация", "🔊 Sonido y vibración", "🔊 Son & Vibration", "🔊 Ton & Vibration", "🔊 Som e vibração"),
        "sound_music_label" to m("Sound & Music",   "Звуки и музыка",   "Sonido y música",      "Son & Musique",        "Ton & Musik",          "Som e música"),
        "vibration_label"   to m("Vibration",       "Вибрация",         "Vibración",            "Vibration",            "Vibration",            "Vibração"),
        "stats_section_label" to m("⚖️ Stats",      "⚖️ Параметры",     "⚖️ Stats",             "⚖️ Stats",             "⚖️ Werte",             "⚖️ Atributos"),
        "body_weight_label" to m("Body weight (kg):","Вес тела (кг):",  "Peso corporal (kg):",  "Poids corporel (kg):", "Körpergewicht (kg):",  "Peso corporal (kg):"),

        // --- Shop extras ---
        "night_mode_max"    to m("Maximum night level +25!", "Максимальный ночной уровень +25!", "¡Nivel nocturno máximo +25!", "Niveau nocturne maximum +25!", "Maximales Nachtlevel +25!", "Nível noturno máximo +25!"),
        "unlocks_at_level"  to m("Unlocks at level", "Открывается на уровне", "Se desbloquea en nivel", "Se débloque au niveau", "Freigabe ab Level", "Libera no nível"),
        "shop_update_label" to m("Update:",         "Обновление:",      "Actualización:",       "Mise à jour:",         "Aktualisierung:",      "Atualização:"),
        "reroll_reset_in"   to m("Cost resets in:", "Сброс стоимости через:", "Costo se reinicia en:", "Coût réinitialisé dans:", "Kosten zurückgesetzt in:", "Custo reiniciado em:"),
        "purchased_label"   to m("Purchased!",      "Куплено!",         "¡Comprado!",           "Acheté!",              "Gekauft!",             "Comprado!"),
        "free_item_label"   to m("Free Item!",      "Бесплатный предмет!", "¡Objeto gratis!",   "Objet gratuit!",       "Gratis-Gegenstand!",   "Item grátis!"),
        "free_points_label" to m("Free Points!",    "Бесплатные очки!", "¡Puntos gratis!",      "Points gratuits!",     "Gratis-Punkte!",       "Pontos grátis!"),
        "watch_ad_btn"      to m("Watch AD",        "Смотреть рекламу", "Ver anuncio",          "Regarder pub",         "Werbung sehen",        "Ver anúncio"),
        "you_won_label"     to m("You Won!",        "Ты выиграл!",      "¡Ganaste!",            "Tu as gagné!",         "Du hast gewonnen!",    "Você ganhou!"),
        "night_mode_enchant" to m("🌙 Night Mode · Max +", "🌙 Ночной режим · Макс +", "🌙 Modo nocturno · Máx +", "🌙 Mode nuit · Max +", "🌙 Nachtmodus · Max +", "🌙 Modo noturno · Máx +"),

        // --- Quests extras ---
        "reroll_quest_desc" to m("Get 3 new random daily quests!", "Получите 3 новых случайных ежедневных задания!", "¡Obtén 3 nuevas misiones aleatorias!", "Obtenez 3 nouvelles quêtes aléatoires!", "Erhalte 3 neue zufällige Tagesquests!", "Obtenha 3 novas missões aleatórias!"),
        "reroll_quest_reward" to m("3 new quests",  "3 новых задания",  "3 misiones nuevas",    "3 nouvelles quêtes",   "3 neue Quests",        "3 novas missões"),
        "reroll_quests_used" to m("Reroll Quests (used)", "Перебросить задания (исп.)", "Nuevas misiones (usadas)", "Relancer quêtes (utilisé)", "Quests neu würfeln (verbraucht)", "Novas missões (usado)"),
        "reroll_quests_btn" to m("Watch Ad to Reroll Daily", "Перебросить ежедневные задания", "Ver anuncio para renovar", "Regarder pub pour relancer", "Anzeige für neue Tagesquests", "Ver anúncio para renovar"),

        // --- Statistics extras ---
        "daily_spin_section" to m("Daily Spin",  "Вращение ленты","Giro diario",       "Tour quotidien",    "Tägliches Drehen",  "Giro diário"),
        "teeth_from_spin"   to m("Teeth from spin", "Зубы с ленты",     "Dientes del giro",     "Dents du tour",        "Zähne vom Drehen",     "Dentes do giro"),
        "items_from_spin"   to m("Items from spin", "Вещи с ленты",     "Objetos del giro",     "Objets du tour",       "Gegenstände vom Drehen","Itens do giro"),
        "teeth_sources_section" to m("Teeth sources", "Источники зубов", "Fuentes de dientes", "Sources de dents", "Zähne-Quellen", "Fontes de dentes"),
        "from_quests"       to m("From quests",     "С квестов",        "De misiones",          "Des quêtes",           "Von Quests",           "De missões"),
        "from_ads"          to m("From ads",        "С рекламы",        "De anuncios",          "Des publicités",       "Von Anzeigen",         "De anúncios"),
        "successful_merges" to m("Successful merges","Успешных merge",   "Fusiones exitosas",    "Fusions réussies",     "Erfolgreiche Verschmelzungen","Fusões bem-sucedidas"),
        "failed_merges"     to m("Failed merges",   "Неудачных merge",  "Fusiones fallidas",    "Fusions échouées",     "Fehlgeschlagene Verschmelzungen","Fusões fracassadas"),
        "successful_enchants" to m("Successful enchants","Успешных enchant","Encantos exitosos", "Enchantements réussis","Erfolgreiche Verzauberungen","Encantos bem-sucedidos"),
        "failed_enchants"   to m("Failed enchants", "Неудачных enchant","Encantos fallidos",    "Enchantements échoués","Fehlgeschlagene Verzauberungen","Encantos fracassados"),
        "total_label"       to m("total",           "всего",            "total",                "total",                "gesamt",               "total"),
    )

    fun t(lang: String, key: String): String = data[key]?.get(lang) ?: data[key]?.get("en") ?: key
}
