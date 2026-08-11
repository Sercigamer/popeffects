# PopEffects

**Configurable pop effects.** When someone burns a totem, takes a heavy hit, or
goes down, PopEffects draws a glowing shape around them — and it looks exactly
the way *you* set it up.

Think MassEffects, but every trigger gets its own shape, its own colours, its
own size, its own sound. All configurable in game, with a live preview.

- **Client-side only.** The server never sees it, other players see nothing.
- **No shader files, no textures.** Every shape is built from geometry at
  runtime.
- Works on plain Fabric and on **Lunar Client**.
- German and English translations.

---

## Four triggers, configured separately

Each one can be switched off on its own, and each has its own complete set of
settings.

| Trigger | Fires when | Default look |
| --- | --- | --- |
| **Totem pop** | Someone burns a Totem of Undying | Golden shockwave, three rings, beacon sound |
| **Big damage** | A living entity loses enough health in one hit | Red ring, threshold 6 damage, silent |
| **Kill** | A living entity dies | Purple dome that stays where they fell |
| **Damage taken** | You take a heavy hit yourself | Red pillar — **off by default** |

Global filters let you turn effects off for yourself, for other players, or for
mobs independently. **Own hits only** restricts damage effects to enemies *you*
hit.

---

## Nine styles

| Style | Looks like |
| --- | --- |
| `ring` | A flat ring on the ground travelling outwards |
| `shockwave` | Several rings one after another — the classic |
| `disc` | A filled disc fading towards the edge |
| `dome` | Half a sphere of rings above their head |
| `sphere` | A full sphere of horizontal rings |
| `pillar` | A vertical cylinder around the target |
| `helix` | A spiral running upwards |
| `burst` | Rays shooting outwards like a star |
| `crown` | A wreath of standing spikes travelling outwards |

Rings are drawn with soft edges — the colour falls off to nothing on the inside
and the outside, which reads far better than a hard cut.

---

## The fade actually fades

This is the part most effect mods get wrong. The effect keeps **expanding
outwards while it becomes transparent**, and thins out as it goes — so it
dissolves instead of freezing in place and then vanishing.

Fade times are set in **ticks, not percent**: make the effect last longer and
the fade stays exactly as long as before. At the end of the time you set, the
effect is exactly gone.

---

## Every setting

The editor has four tabs. By default each tab shows only the settings you
actually reach for often; the **All settings** button at the bottom brings the
rest back. Nothing is removed — it just isn't all on screen at once.

### Shape

| Setting | Meaning |
| --- | --- |
| Style | One of the nine shapes |
| Start radius / End radius | The effect grows from one to the other |
| Thickness | Width of the bands, in blocks |
| Height | For dome, sphere, pillar, helix and crown |
| Height offset | Shift upwards, measured from their feet |
| Corners | Ring resolution — rounder, but more triangles |
| Rings | Ring count for shockwave, dome and sphere; turns for the helix |
| Spin | Degrees per second, negative spins the other way |
| Follow target | On: the effect moves with them. Off: it stays where it happened |

### Colour

| Setting | Meaning |
| --- | --- |
| Start colour / End colour | Three sliders each for red, green, blue, with a hex readout |
| Gradient | Fades from the start colour to the end colour over its lifetime |
| Rainbow | Ignores both colours and cycles the colour wheel |
| Opacity | 10 to 255 |
| Glow | Additive blending — punchier, but opacity then controls brightness instead of transparency |
| Halo | A wide, faint copy behind the shape. Makes it visibly thicker |
| Through walls | Stays visible behind blocks |

**Glow** and **Halo** are off by default: both work against a clean fade.
Without them the ring stays a thin line that simply becomes transparent.

### Timing

| Setting | Meaning |
| --- | --- |
| Effect | Switch this trigger on or off |
| Duration | Lifetime in ticks (20 ticks = 1 second) |
| Fade in | Ticks spent fading in. 0 = fully there immediately |
| Fade out | Ticks spent fading out. 0 = hard cut |
| Drift | How many blocks the effect keeps travelling outwards while it fades |
| Threshold | How much damage it takes to fire (damage triggers only) |
| Size by damage | A heavy hit makes a bigger effect, up to three times the size |

### Sound

| Setting | Meaning |
| --- | --- |
| Play sound, Sound, Volume, Pitch | Eight suggestions; any ID works via the config file |
| Particles, Particle, Particle count | Vanilla particles in a circle around the effect |

The main menu adds **Range** (how far away effects still show) and **Max
effects** (a brake against effect spam in big fights — when the limit is full,
the oldest one goes).

---

## Preview button

Plays the effect seven blocks in front of you, in the direction you are
looking. You see every change instantly, without having to go and hit
somebody. The menu deliberately does not pause the game, so the animation runs
in singleplayer too.

---

## Controls and commands

| Key | Function |
| --- | --- |
| `P` | Open the PopEffects menu |
| *unbound* | Toggle PopEffects entirely |
| *unbound* | Preview the totem effect |

All three live under **Options → Controls → Key Binds → Miscellaneous**. The
lower two are deliberately unbound so the mod does not steal a key from you.

`/popeffects`, short `/pe`. Runs entirely client-side.

| Command | Effect |
| --- | --- |
| `/pe` | Short status report |
| `/pe config` | Open the menu |
| `/pe help` | List every command |
| `/pe on` · `/pe off` | Turn the mod on and off |
| `/pe reset` | Everything back to defaults |

Plus a subtree per effect, where `<effect>` is `totem`, `damage`, `kill` or
`self`:

| Command | Effect |
| --- | --- |
| `/pe <effect>` | Status of that effect |
| `/pe <effect> on` · `off` | Toggle just that one |
| `/pe <effect> preview` | Play it in front of you |
| `/pe <effect> style <style>` | Pick a shape |
| `/pe <effect> color start <#RRGGBB>` | Start colour |
| `/pe <effect> color end <#RRGGBB>` | End colour |
| `/pe <effect> duration <ticks>` | Lifetime |
| `/pe <effect> fadein <ticks>` · `fadeout <ticks>` | Fade times |
| `/pe <effect> radius <blocks>` | End radius |
| `/pe <effect> threshold <damage>` | When it fires |
| `/pe <effect> reset` | Reset just that one |

---

## Extras

**Pop counter.** A small list in the HUD showing who popped and how often —
exactly the information that gets lost in a fight.

**Config file.** Everything lands in `config/popeffects.json`, formatted
readably and sorted by topic, so you can edit it by hand or share it with
friends. Colours are stored as hex codes, the same as in the menu:

```json
"colorStart": "#FFD54A",
"colorEnd": "#37D67A",
"durationTicks": 28,
"fadeInTicks": 2,
"fadeOutTicks": 18,
```

The file is checked on load: values outside the sensible range get nudged back,
missing blocks are filled with defaults. A broken file cannot break your game.

---

## How the damage is measured

The server never sends a damage number to the client, only the new health
value. PopEffects remembers the last value it saw and subtracts — the
difference is the damage.

A real hit always arrives together with the server's damage packet, and
PopEffects requires that packet before it shows anything. Without that check a
wounded mob simply coming into view looked like a hit: the client creates it at
full health first and gets the real value a moment later. That packet also says
*who* dealt the damage, which is what makes **Own hits only** possible.

A lethal hit shows the kill effect, not the damage effect on top of it.

---

## Performance

The effects are plain coloured quads with no texture. A ring at the default 48
corners is 96 quads — nothing next to a normal Minecraft scene. **Range** and
**Max effects** are there as brakes; on a weak machine, turn **Corners** down
first.

---

## Versions

| Minecraft | PopEffects |
| --- | --- |
| 26.2 | 2.1.x |
| 1.21.11 · 1.21.10 | 1.4.x |

Each Minecraft version has its own jar — the version is in the filename.

On **26.x** and on **1.21.10**, the optional **Glow** and **Through walls**
switches are unavailable: Minecraft does not expose the render building blocks
needed for them there, so the menu greys them out instead of offering something
that does nothing. Both are off by default anyway; everything else is
identical.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for your version
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) in your `mods` folder — **required**
3. Add the PopEffects jar for your Minecraft version next to it

**Minecraft 26.x needs Java 25**, which the game itself requires. The 1.21
builds need Java 21.

**Lunar Client:** open the launcher, pick your version on the left, click
*Settings* at the bottom right, open the *Mods* tab and drag both jars in.
Restart Lunar afterwards. Use the launcher's own Mods button rather than
hunting for folders — that way the files land in the right place.

---

## Nothing happening?

| Symptom | Cause |
| --- | --- |
| Mod does not show up | **Fabric API** is missing from `mods` |
| Menu shows keys like `popeffects.edit.style` | Wrong jar — the `-sources.jar` does not belong in `mods` |
| No effects at all | `/pe status` — are the mod and that effect switched on? |
| Only on some targets | Range too small, or *Own hits only* is on |
| Fires too rarely | Threshold too high: `/pe damage threshold 4` |

---

## Licence

MIT.
