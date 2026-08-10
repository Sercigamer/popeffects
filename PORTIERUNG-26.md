# Portierung auf Minecraft 26.x

Arbeitsstand des Zweigs `mc26`. Der Build steht, der Code ist noch der aus dem
1.21er-Zweig und kompiliert nicht.

## Was sich in 26.x geändert hat

### Minecraft wird unobfuskiert ausgeliefert

Das ist die Grundlage für alles Weitere. Im Client-Jar von 26.2 stehen die
echten Namen: `net/minecraft/client/Minecraft.class`,
`net/minecraft/world/entity/LivingEntity.class`. Deshalb

- veröffentlicht Mojang keine `client_mappings` mehr (1.21.11 hatte sie noch),
- gibt es kein Yarn für 26.x — die neueste Yarn-Version ist 1.21.11,
- braucht der Build **keinen `mappings`-Eintrag** und **kein
  `modImplementation`**, sondern schlichtes `implementation`.

Praktischer Nebeneffekt: die gesamte API lässt sich direkt aus dem Jar
auslesen. Nichts muss geraten werden.

### Die Namen sind eine Mischung

Weder durchgängig Mojang- noch Yarn-Namen:

| 1.21.11 (Yarn) | 26.2 |
| --- | --- |
| `MinecraftClient` | `net.minecraft.client.Minecraft` |
| `Text` | `net.minecraft.network.chat.Component` |
| `Formatting` | `net.minecraft.ChatFormatting` |
| `World` / `ClientWorld` | `Level` / `net.minecraft.client.multiplayer.ClientLevel` |
| `Vec3d` | `net.minecraft.world.phys.Vec3` |
| `MathHelper` | `net.minecraft.util.Mth` |
| `ButtonWidget` | `net.minecraft.client.gui.components.Button` |
| `CyclingButtonWidget` | `CycleButton` |
| `SliderWidget` | `AbstractSliderButton` |
| `TextRenderer` | `net.minecraft.client.gui.Font` |
| `KeyBinding` | `net.minecraft.client.KeyMapping` |
| `RenderLayer` | `net.minecraft.client.renderer.rendertype.RenderType` |
| `MatrixStack` | `com.mojang.blaze3d.vertex.PoseStack` |
| `Identifier` | **`net.minecraft.resources.Identifier`** — Mojang hat hier den Yarn-Namen übernommen, nicht `ResourceLocation` |

Fabric API zieht mit: `KeyBindingHelper` heißt jetzt `KeyMappingHelper` im
Paket `keymapping/v1`, `ColorProviderRegistry` heißt `BlockColorRegistry`.

### Zwei Stellen sind ein Umbau, keine Umbenennung

**Das Zeichnen in der Welt.** `MultiBufferSource` gibt es nicht mehr.
`LevelRenderContext` (der Nachfolger von `WorldRenderContext`) liefert
stattdessen einen **`SubmitNodeCollector`** — statt Vertices direkt in einen
Puffer zu schreiben, reicht man Zeichenbefehle ein. Damit ist
`ShapeRenderer` — das Herz der Mod — nicht übersetzbar, sondern neu zu bauen.

**Das HUD.** `GuiGraphics` existiert nicht mehr; es gibt nur noch
`GuiGraphicsExtractor`. Fabrics `HudElement` heißt jetzt
`extractRenderState(GuiGraphicsExtractor, DeltaTracker)`. Die Oberfläche
arbeitet also wie der Weltrenderer mit einer Extraktions-Phase. Pop-Zähler und
vermutlich auch die Menüs müssen entsprechend umgebaut werden.

## Was noch zu tun ist

1. Typen umbenennen (mechanisch, jeder Name am Jar prüfbar).
2. `ShapeRenderer` und `EffectRenderer` auf `SubmitNodeCollector` umbauen.
3. `PopCounterHud` auf die Extraktions-API umbauen.
4. Mixin-Ziele prüfen: die Paketklassen heißen anders
   (`ClientPlayNetworkHandler` → `ClientPacketListener`, die S2C-Pakete
   entsprechend).
5. Eigene Render-Pipelines: prüfen, ob `RenderType` in 26.x noch so gebaut
   werden kann wie in 1.21.11 — davon hängen „Leuchten" und „Durch Wände" ab.
6. Bauen, im Client testen, Selbsttests laufen lassen.

## Nützliche Handgriffe

Die gesamte API steht im Client-Jar. Klassennamen nachschlagen:

```bash
jar tf client.jar | grep -E "/Minecraft\.class$"
```

Signaturen einer Klasse ansehen:

```bash
javap -cp client.jar -public net.minecraft.client.Minecraft
```

Das Jar liegt unter der URL aus dem Versions-Manifest von 26.2 und wird von
Loom ohnehin nach `~/.gradle/caches/fabric-loom/26.2/` geladen.
