# Changelog

Alle nennenswerten Änderungen an PopEffects. Das Format orientiert sich an
[Keep a Changelog](https://keepachangelog.com/de/1.1.0/), die Versionen folgen
[Semantic Versioning](https://semver.org/lang/de/).

## [1.3.0] - 2026-08-10

### Geändert

- **Das Ausblenden macht die Farbe jetzt wirklich durchsichtig.** Vorher waren
  additives Mischen und der Schein an: additiv addiert die Farbe auf den
  Hintergrund, statt ihn zu überdecken — die Deckkraft steuerte damit die
  Helligkeit und nicht die Durchsichtigkeit. Eine helle Farbe blühte auf
  dunklem Hintergrund auf und der Ring wirkte dick. Beides ist jetzt aus,
  bleibt aber als Schalter im Reiter „Farbe".
- **Der Ring ist wieder eine dünne Linie.** Der Schein zeichnete eine mehrfach
  breitere Kopie darunter — das war der fette Rand.
- Die Deckkraft läuft **linear** auf null statt über eine weiche Kurve. Eine
  weiche Kurve hält den Effekt erst lange fast voll sichtbar und lässt ihn dann
  schnell wegkippen; linear wird er gleichmäßig durchsichtig und ist am Ende
  der eingestellten Zeit exakt weg.
- Der Schein ist schmaler und blasser, falls man ihn einschaltet.
- Bestehende Configs werden auf Version 3 gehoben: Leuchten und Schein gehen
  dabei aus.

## [1.2.0] - 2026-08-10

### Geändert

- **Der Abgang sieht jetzt aus wie ein Abgang.** Bisher benutzte der Radius
  eine Ease-out-Kurve, die schon bei 80 Prozent der Lebenszeit auf 99 Prozent
  der Endgröße war — der Ring stand also still und verschwand dann einfach.
  Jetzt läuft er bis zuletzt weiter nach außen.
- Neu dazu: **Nachdehnen**. Während des Ausblendens legt der Effekt noch
  einstellbar viele Blöcke Radius drauf und wird dabei dünner. Er löst sich
  auf, statt abgeschnitten zu werden.

### Neu

- **Schein**: ein breiter, blasser Halo hinter jeder Form. Macht aus dem
  aufgeklebten Band eine Neonröhre. Pro Effekt abschaltbar.
- **Neuer Stil `crown`**: ein Kranz aus stehenden Zacken, der nach außen läuft.

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

[1.3.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.3.0
[1.2.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.2.0
[1.1.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.1.0
[1.0.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.0.0
