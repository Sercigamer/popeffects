# Changelog

Alle nennenswerten Änderungen an PopEffects. Das Format orientiert sich an
[Keep a Changelog](https://keepachangelog.com/de/1.1.0/), die Versionen folgen
[Semantic Versioning](https://semver.org/lang/de/).

## [2.0.0] - 2026-08-11

Portierung auf **Minecraft 26.2**. Für 1.21.10 und 1.21.11 gibt es weiterhin
die 1.4.x-Reihe aus dem Zweig `main`.

### Geändert

- **Minecraft 26.x wird unobfuskiert ausgeliefert.** Mojang veröffentlicht
  deshalb keine Mappings mehr und Fabric kein Yarn; der Build braucht weder
  einen `mappings`-Eintrag noch `modImplementation`. Dafür ist **Java 25**
  Pflicht.
- Jeder Minecraft-Typ heißt anders. Die Namen sind eine Mischung:
  `MinecraftClient` wird zu `Minecraft`, `Text` zu `Component` — der ID-Typ
  heißt aber `Identifier`, also ausgerechnet wie in Yarn.
- **Der Weltrenderer war ein Umbau, keine Übersetzung.** Statt selbst in einen
  Puffer zu schreiben, reicht die Mod ihre Geometrie über
  `submitCustomGeometry` bei einem `SubmitNodeCollector` ein. Die Formen selbst
  konnten unverändert bleiben.
- Oberflächen arbeiten jetzt wie der Weltrenderer mit einer Extraktions-Phase:
  aus `DrawContext` wurde `GuiGraphicsExtractor`, aus `render` wird
  `extractRenderState`.

### Nicht dabei auf 26.x

- **Leuchten** und **Durch Wände**: die Bausteine für eigene Render-Pipelines
  sind nicht erreichbar. Die Mod nutzt den Vanilla-Typ `debugQuads`; die beiden
  Schalter sind im Menü ausgegraut statt wirkungslos. Beide sind ohnehin
  standardmäßig aus, der Rest ist identisch.
- Der Selbsttest legt Screenshots nur noch unter dem automatischen
  Zeitstempel-Namen ab — an das Render-Ziel kommt man nicht mehr ohne Weiteres
  heran. Der gewünschte Name steht im Log, die Reihenfolge stimmt.
- Mod Menu ist vorerst nicht eingebunden.

## [1.4.0] - 2026-08-11

### Neu

- **Läuft jetzt auch auf Minecraft 1.21.10.** Es gibt ab sofort ein Jar je
  Version, die Version steht im Dateinamen: `popeffects-1.4.0+1.21.11.jar`
  und `popeffects-1.4.0+1.21.10.jar`.
- Auf 1.21.10 fehlen **Leuchten** und **Durch Wände**: die Bausteine, aus
  denen sich in 1.21.11 eigene Render-Pipelines bauen lassen, sind dort alle
  paketprivat. Statt sie über eine Kette von Zugriffs-Hacks zu erzwingen,
  nutzt die Mod dort den passenden Vanilla-Layer. Die beiden Schalter sind im
  Menü ausgegraut statt wirkungslos — der Rest ist identisch, und beide
  Optionen sind ohnehin standardmäßig aus.
- Die versionsabhängigen Stellen stecken in einer schmalen Schicht unter
  `src/mc<version>`; der restliche Code ist für alle Versionen derselbe.
- Der Entwicklungs-Client benutzt pro Minecraft-Version einen eigenen
  Laufordner. Sonst versucht ein älterer Client, eine mit einer neueren
  Version erstellte Welt zu öffnen — das verweigert Minecraft wortkarg.

### Nicht dabei

- **1.21.9** ist bewusst außen vor: Fabric API hatte in dieser Version gar
  keine World-Render-Events (die Klassen fehlen dort komplett und kamen erst
  mit 1.21.10 zurück). Dafür wäre ein eigener `WorldRenderer`-Mixin nötig.
- **26.1 / 26.2** brauchen eine eigene Codebasis: für 26.x gibt es keine
  Yarn-Mappings mehr, Fabric ist dort auf Mojang-Namen umgestiegen. Damit
  heißt praktisch jede Minecraft-Klasse anders.

## [1.3.1] - 2026-08-11

### Behoben

- **Der Schadens-Effekt hat nie ausgelöst.** Der Einhängepunkt zum Messen des
  Lebensverlusts saß auf `Entity.onDataTrackerUpdate` — die Methode wird zwar
  aufgerufen, aber erst, wenn alle neuen Werte schon geschrieben sind. Der
  „vorher"-Wert war also bereits der „nachher"-Wert und die Differenz immer
  null. Jetzt wird der zuletzt gesehene Lebensstand in `onTrackedDataSet`
  mitgeführt. Betrifft alle Versionen bis einschließlich 1.3.0.

### Neu

- **Auslöser-Selbsttest** mit `-Dpopeffects.triggertest=true`: setzt im
  Einzelspieler über den eingebauten Server echte Befehle ab — Totem in die
  Hand, tödlicher Schaden, Zombie herbeirufen, verletzen, töten — und prüft
  danach, ob der passende Effekt entstanden ist. Genau dieser Test hat den
  Fehler oben gefunden.

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

[2.0.0]: https://github.com/Sercigamer/popeffects/releases/tag/v2.0.0
[1.4.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.4.0
[1.3.1]: https://github.com/Sercigamer/popeffects/releases/tag/v1.3.1
[1.3.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.3.0
[1.2.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.2.0
[1.1.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.1.0
[1.0.0]: https://github.com/Sercigamer/popeffects/releases/tag/v1.0.0
