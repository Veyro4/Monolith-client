package femboy.monolith.ui;

import java.util.Map;

public final class GuiLang {
    private static final Map<String, Map<String, String>> PACKS = Map.of(
            "Deutsch", de(),
            "Español", es(),
            "Français", fr(),
            "Русский", ru()
    );

    private GuiLang() {
    }

    public static String text(String lang, String value) {
        Map<String, String> pack = PACKS.get(lang);
        if (pack == null || value == null) {
            return value;
        }
        return pack.getOrDefault(value, value);
    }

    private static Map<String, String> de() {
        return Map.ofEntries(
                e("Search", "Suche"), e("client", "Client"), e("modules", "Module"),
                e("Main", "Haupt"), e("Combat", "Kampf"), e("Movement", "Bewegung"),
                e("Utility", "Nützlich"), e("Visuals", "Visuell"), e("HUD", "HUD"),
                e("Server", "Server"), e("Social", "Sozial"), e("Friends", "Freunde"),
                e("Menu", "Menü"), e("Scripts", "Skripte"), e("Configs", "Konfigs"),
                e("Themes", "Themen"), e("Client", "Client"), e("Donut SMP", "Donut SMP"),
                e("Language", "Sprache"), e("GUI scale", "GUI-Größe"), e("Glass", "Glas"),
                e("Font", "Schrift"), e("Stream proof", "Stream-Schutz"), e("Sounds", "Töne"),
                e("Blur", "Hintergrund"), e("On", "An"), e("Off", "Aus"),
                e("Aim Assist", "Zielhilfe"), e("Trigger Bot", "Triggerbot"), e("Velocity", "Rückstoß"),
                e("Auto Shield", "Auto-Schild"), e("Shield Break", "Schildbruch"), e("Hit Web", "Netz"),
                e("Lunge", "Ausfall"), e("Fall Smash", "Fallschlag"), e("Combo", "Kombo"),
                e("Pearl Catch", "Perlenfang"), e("Keep Sprint", "Sprint halten"), e("Hit Select", "Trefferwahl"),
                e("Reach", "Reichweite"), e("Sprint", "Sprint"), e("Flight", "Flug"),
                e("Speed", "Tempo"), e("Strafe", "Strafe"), e("Timer", "Timer"),
                e("No Fall", "Kein Fall"), e("Spider", "Spinne"), e("High Jump", "Hochsprung"),
                e("Jesus", "Wasserlauf"), e("Water Speed", "Wassertempo"), e("No Slow", "Keine Bremse"),
                e("No Web", "Kein Netz"), e("Range", "Reichweite"), e("Delay", "Verzögerung"),
                e("Bind", "Taste"), e("Mode", "Modus"), e("Chance", "Chance")
        );
    }

    private static Map<String, String> es() {
        return Map.ofEntries(
                e("Search", "Buscar"), e("client", "cliente"), e("modules", "módulos"),
                e("Main", "Principal"), e("Combat", "Combate"), e("Movement", "Movimiento"),
                e("Utility", "Utilidad"), e("Visuals", "Visual"), e("HUD", "HUD"),
                e("Server", "Servidor"), e("Social", "Social"), e("Friends", "Amigos"),
                e("Menu", "Menú"), e("Scripts", "Scripts"), e("Configs", "Configs"),
                e("Themes", "Temas"), e("Client", "Cliente"),
                e("Language", "Idioma"), e("GUI scale", "Escala"), e("Glass", "Cristal"),
                e("Font", "Fuente"), e("Stream proof", "Modo stream"), e("Sounds", "Sonidos"),
                e("Blur", "Fondo"), e("On", "Sí"), e("Off", "No"),
                e("Aim Assist", "Asistencia"), e("Trigger Bot", "Disparo auto"), e("Velocity", "Retroceso"),
                e("Auto Shield", "Escudo auto"), e("Flight", "Vuelo"), e("Speed", "Velocidad"),
                e("Sprint", "Correr"), e("No Fall", "Sin caída"), e("Reach", "Alcance"),
                e("Range", "Alcance"), e("Delay", "Retraso"), e("Bind", "Tecla"), e("Mode", "Modo")
        );
    }

    private static Map<String, String> fr() {
        return Map.ofEntries(
                e("Search", "Chercher"), e("client", "client"), e("modules", "modules"),
                e("Main", "Principal"), e("Combat", "Combat"), e("Movement", "Mouvement"),
                e("Utility", "Utile"), e("Visuals", "Visuel"), e("Friends", "Amis"),
                e("Menu", "Menu"), e("Scripts", "Scripts"), e("Configs", "Configs"),
                e("Themes", "Thèmes"), e("Client", "Client"),
                e("Language", "Langue"), e("GUI scale", "Échelle"), e("Glass", "Verre"),
                e("Font", "Police"), e("Stream proof", "Mode stream"), e("Sounds", "Sons"),
                e("Blur", "Arrière-plan"), e("On", "Oui"), e("Off", "Non"),
                e("Aim Assist", "Aide visée"), e("Trigger Bot", "Tir auto"), e("Velocity", "Recul"),
                e("Flight", "Vol"), e("Speed", "Vitesse"), e("Sprint", "Sprint"),
                e("No Fall", "Sans chute"), e("Reach", "Portée"),
                e("Range", "Portée"), e("Delay", "Délai"), e("Bind", "Touche"), e("Mode", "Mode")
        );
    }

    private static Map<String, String> ru() {
        return Map.ofEntries(
                e("Search", "Поиск"), e("client", "клиент"), e("modules", "модулей"),
                e("Main", "Основное"), e("Combat", "Бой"), e("Movement", "Движение"),
                e("Utility", "Полезное"), e("Visuals", "Визуал"), e("HUD", "HUD"),
                e("Server", "Сервер"), e("Social", "Соц"), e("Friends", "Друзья"),
                e("Menu", "Меню"), e("Scripts", "Скрипты"), e("Configs", "Конфиги"),
                e("Themes", "Темы"), e("Client", "Клиент"),
                e("Language", "Язык"), e("GUI scale", "Масштаб"), e("Glass", "Стекло"),
                e("Font", "Шрифт"), e("Stream proof", "Стрим"), e("Sounds", "Звуки"),
                e("Blur", "Фон"), e("On", "Вкл"), e("Off", "Выкл"),
                e("Aim Assist", "Наводка"), e("Trigger Bot", "Триггер"), e("Velocity", "Отдача"),
                e("Auto Shield", "Автощит"), e("Shield Break", "Лом щита"), e("Flight", "Полёт"),
                e("Speed", "Скорость"), e("Sprint", "Бег"), e("No Fall", "Без урона"),
                e("Reach", "Дальность"), e("Spider", "Паук"), e("High Jump", "Высокий прыжок"),
                e("Range", "Дальность"), e("Delay", "Задержка"), e("Bind", "Клавиша"),
                e("Mode", "Режим"), e("Chance", "Шанс")
        );
    }

    private static Map.Entry<String, String> e(String key, String value) {
        return Map.entry(key, value);
    }
}
