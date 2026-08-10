# Changelog

Alle nennenswerten Änderungen an PopEffects. Das Format orientiert sich an
[Keep a Changelog](https://keepachangelog.com/de/1.1.0/), die Versionen folgen
[Semantic Versioning](https://semver.org/lang/de/).

## [1.1.0] - 2026-08-10

### Neu

- **Ein- und Ausblenden ist jetzt einstellbar.** Pro Effekt lässt sich getrennt
  festlegen, über wie viele Ticks er auf- und wieder abgeblendet wird. Vorher
  steckte beides fest verdrahtet im Renderer.
- Gerechnet wird in **Ticks statt in Prozent**: stellt man die Dauer länger,
  bleibt das Ausblenden genau so lang wie vorher — der Effekt steht dann
  einfach länger, bevor er weggeht.
- Bei der Schockwelle blendet jeder Ring auf seiner eigenen Uhr aus.
- Neue Befehle `/pe <effekt> fadein <ticks>` und `fadeout <ticks>`.

### Geändert

- **Farben stehen in der Config-Datei jetzt als `"#FFD54A"`** statt als
  Dezimalzahl `16766282`. Dateien aus 1.0.0 werden beim ersten Start
  automatisch umgeschrieben, die Einstellungen bleiben erhalten.
- Die Felder in der Config-Datei sind nach Themen sortiert: erst was, dann
  Farbe, dann Zeit, dann Größe, zuletzt Ton und Partikel.
- **Der Effekt-Editor hat vier Reiter statt drei** — Form, Farbe, Ablauf und
  Ton. Jeder Reiter hat damit ein klares Thema, keiner ist mehr überfüllt, und
  „Effekt an/aus" steht nicht länger zwischen Sound-Reglern.
- Der Selbsttest macht zwei Bilder je Stil, eins früh und eins tief im
  Ausblenden, dazu ein Vergleichsbild der leeren Szene. Erst am Paar sieht man,
  ob das Ausblenden wirklich greift.

## [1.0.0] - 2026-08-10

Erste Veröffentlichung.

### Neu

- **Vier Auslöser**, jeder einzeln ein- und ausschaltbar und getrennt
  einstellbar: Totem-Pop, Großer Schaden, Kill und Eigener Schaden.
- **Acht Effekt-Stile**: Ring, Schockwelle, Scheibe, Kuppel, Kugel, Säule,
  Spirale und Strahlen.
- **Pro Effekt rund 25 Einstellungen**: zwei Farben mit Verlauf, Regenbogen,
  Deckkraft, additives Leuchten, Sichtbarkeit durch Wände, Dauer, Start- und
  Endradius, Höhe, Dicke, Ecken, Ringanzahl, Drehgeschwindigkeit,
  Höhenversatz, "folgt dem Ziel", Auslöseschwelle, Größe je Schaden, Sound
  (Klang, Lautstärke, Tonhöhe) und Partikel (Art und Anzahl).
- **Menü mit drei Reitern** (Form, Farbe, Extras) samt Farbreglern mit
  Hex-Anzeige und einem **Vorschau-Knopf**, der den Effekt sieben Blöcke vor
  dir abspielt — dadurch sieht man jede Änderung sofort.
- **Größe je Schaden**: Ein harter Treffer erzeugt einen größeren Effekt als
  ein Kratzer.
- **Pop-Zähler** als kleine Liste im HUD: wer hat gerade wie oft gepoppt.
- **Nur eigene Treffer** als Option — dann erscheinen Schadens-Effekte nur bei
  Gegnern, die du selbst getroffen hast.
- Filter für dich selbst, andere Spieler und Mobs, dazu Reichweite und eine
  Obergrenze für gleichzeitig laufende Effekte.
- Befehle `/popeffects` und `/pe` mit einem Unterbaum je Effekt.
- Tastenbelegung: `P` öffnet das Menü, zwei weitere Tasten sind frei belegbar.
- Deutsche und englische Sprachdatei, Mod-Menu-Integration.

[1.1.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.1.0
[1.0.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.0.0
