package femboy.monolith.ui;

import java.util.Map;

public final class GuiLang {
    private static final Map<String, Map<String, String>> PACKS = Map.of(
            "Deutsch", Map.ofEntries(
                    Map.entry("Search", "Suche"), Map.entry("client", "Client"), Map.entry("modules", "Module"),
                    Map.entry("Main", "Haupt"), Map.entry("Combat", "Kampf"), Map.entry("Movement", "Bewegung"),
                    Map.entry("Utility", "Nützlich"), Map.entry("Visuals", "Visuell"), Map.entry("Friends", "Freunde"),
                    Map.entry("Scripts", "Skripte"), Map.entry("Configs", "Konfigs"), Map.entry("Menu", "Menü"),
                    Map.entry("Language", "Sprache"), Map.entry("Font", "Schrift"), Map.entry("Glass", "Glas"),
                    Map.entry("Sounds", "Töne"), Map.entry("Blur", "Unschärfe"), Map.entry("Stream proof", "Stream-Schutz"),
                    Map.entry("GUI scale", "GUI-Größe"), Map.entry("On", "An"), Map.entry("Off", "Aus")
            ),
            "Español", Map.ofEntries(
                    Map.entry("Search", "Buscar"), Map.entry("client", "cliente"), Map.entry("modules", "módulos"),
                    Map.entry("Main", "Principal"), Map.entry("Combat", "Combate"), Map.entry("Movement", "Movimiento"),
                    Map.entry("Utility", "Utilidad"), Map.entry("Visuals", "Visual"), Map.entry("Friends", "Amigos"),
                    Map.entry("Menu", "Menú"), Map.entry("Language", "Idioma"), Map.entry("Font", "Fuente"),
                    Map.entry("Sounds", "Sonidos"), Map.entry("Blur", "Desenfoque"), Map.entry("On", "Sí"), Map.entry("Off", "No")
            ),
            "Français", Map.ofEntries(
                    Map.entry("Search", "Chercher"), Map.entry("client", "client"), Map.entry("modules", "modules"),
                    Map.entry("Combat", "Combat"), Map.entry("Movement", "Mouvement"), Map.entry("Friends", "Amis"),
                    Map.entry("Language", "Langue"), Map.entry("Font", "Police"), Map.entry("Sounds", "Sons"),
                    Map.entry("Blur", "Flou"), Map.entry("On", "Oui"), Map.entry("Off", "Non")
            ),
            "Русский", Map.ofEntries(
                    Map.entry("Search", "Поиск"), Map.entry("client", "клиент"), Map.entry("modules", "модулей"),
                    Map.entry("Main", "Основное"), Map.entry("Combat", "Бой"), Map.entry("Movement", "Движение"),
                    Map.entry("Utility", "Полезное"), Map.entry("Visuals", "Визуал"), Map.entry("Friends", "Друзья"),
                    Map.entry("Menu", "Меню"), Map.entry("Language", "Язык"), Map.entry("Font", "Шрифт"),
                    Map.entry("Sounds", "Звуки"), Map.entry("Blur", "Размытие"), Map.entry("On", "Вкл"), Map.entry("Off", "Выкл")
            )
    );

    private GuiLang() {
    }

    public static String text(String lang, String value) {
        Map<String, String> pack = PACKS.get(lang);
        if (pack == null) {
            return value;
        }
        return pack.getOrDefault(value, value);
    }
}
