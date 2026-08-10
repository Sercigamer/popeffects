> Clientseitige Fabric-Mod für Minecraft **1.21.10** und **1.21.11**.
> Effekt-Kreise um Gegner, die ein Totem poppen, viel Schaden fressen oder sterben.

{{CHANGELOG}}

## Welches Jar?

Für jede Minecraft-Version gibt es ein eigenes Jar — die Version steht im Dateinamen:

| Minecraft | Datei |
| --- | --- |
| 1.21.11 | `popeffects-{{VERSION}}+1.21.11.jar` |
| 1.21.10 | `popeffects-{{VERSION}}+1.21.10.jar` |

Auf 1.21.10 fehlen zwei optionale Sachen, weil Minecraft dort die nötigen Render-Bausteine noch nicht herausgibt: **Leuchten** (additives Mischen) und **Durch Wände**. Beide sind ohnehin standardmäßig aus; im Menü sind die Schalter dort ausgegraut. Alles andere ist identisch.

## Installation

1. [Fabric Loader](https://fabricmc.net/use/installer/) für deine Version installieren
2. [Fabric API](https://modrinth.com/mod/fabric-api) in den `mods`-Ordner legen — **zwingend nötig**
3. Das passende Jar von oben daneben legen
4. Optional: [Mod Menu](https://modrinth.com/mod/modmenu) für den Zahnrad-Knopf im Mod-Menü

**Lunar Client:** Launcher öffnen → links die Version wählen → unten rechts auf *Einstellungen* → Reiter *Mods* → beide Jars ins Fenster ziehen. Danach Lunar neu starten.

## Erste Schritte

| Aktion | Wie |
| --- | --- |
| Menü öffnen | Taste `P` oder `/pe config` |
| Effekt ausprobieren | Im Menü auf *Vorschau* oder `/pe totem preview` |
| Farbe ändern | `/pe totem color start #55FFFF` |
| Form ändern | `/pe kill style sphere` |
| Ausblenden einstellen | `/pe totem fadeout 20` |
| Alle Befehle | `/pe help` |

## Die neun Stile

`ring` · `shockwave` · `disc` · `dome` · `sphere` · `pillar` · `helix` · `burst` · `crown`

Jeder Effekt hat seine eigenen Farben, Größen, Dauer, Sounds und Partikel — die vier Auslöser können also völlig unterschiedlich aussehen.

## Wenn nichts passiert

Fast immer fehlt **Fabric API** im `mods`-Ordner. Ansonsten: `/pe status` zeigt, ob die Mod und die einzelnen Effekte an sind.

---

Volle Anleitung im [README](https://github.com/{{REPO_FULL}}#readme) · [Changelog](https://github.com/{{REPO_FULL}}/blob/main/CHANGELOG.md)
