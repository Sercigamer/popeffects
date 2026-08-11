> Clientseitige Fabric-Mod für **Minecraft 26.2**.
> Effekt-Kreise um Gegner, die ein Totem poppen, viel Schaden fressen oder sterben.

{{CHANGELOG}}

## Installation

1. [Fabric Loader](https://fabricmc.net/use/installer/) für **26.2** installieren
2. [Fabric API](https://modrinth.com/mod/fabric-api) für 26.2 in den `mods`-Ordner legen — **zwingend nötig**
3. `popeffects-{{VERSION}}+26.2.jar` daneben legen

Es braucht **Java 25** — das setzt Minecraft 26.x selbst voraus.

**Lunar Client:** Launcher öffnen → links die Version **26.2** wählen → unten rechts auf *Einstellungen* → Reiter *Mods* → beide Jars ins Fenster ziehen. Danach Lunar neu starten.

Für **Minecraft 1.21.10 und 1.21.11** gibt es die 1.4.x-Reihe aus dem Zweig `main`.

## Erste Schritte

| Aktion | Wie |
| --- | --- |
| Menü öffnen | Taste `P` oder `/pe config` |
| Effekt ausprobieren | Im Menü auf *Vorschau* oder `/pe totem preview` |
| Farbe ändern | `/pe totem color start #55FFFF` |
| Ausblenden einstellen | `/pe totem fadeout 20` |
| Alle Befehle | `/pe help` |

## Die neun Stile

`ring` · `shockwave` · `disc` · `dome` · `sphere` · `pillar` · `helix` · `burst` · `crown`

## Wenn nichts passiert

Fast immer fehlt **Fabric API** im `mods`-Ordner. Ansonsten: `/pe status` zeigt, ob die Mod und die einzelnen Effekte an sind.

---

Volle Anleitung im [README](https://github.com/{{REPO_FULL}}#readme) · [Changelog](https://github.com/{{REPO_FULL}}/blob/main/CHANGELOG.md)
