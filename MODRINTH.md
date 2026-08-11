# PopEffects

**Configurable pop effects for Minecraft.** When someone burns a totem, takes a
heavy hit, or goes down, PopEffects draws a glowing shape around them — and it
looks exactly the way *you* set it up.

Think MassEffects, but every trigger gets its own shape, its own colours, its
own size, its own sound. All configurable in game, with a live preview.

- **Client-side only.** The server never sees it, other players see nothing.
- **No shader files, no textures.** Every shape is built from geometry at
  runtime, so nothing breaks when a resource pack changes.
- Works on plain Fabric and on **Lunar Client**.

## Four triggers, configured separately

| Trigger | Fires when | Default |
| --- | --- | --- |
| **Totem pop** | Someone burns a Totem of Undying | Golden shockwave, three rings, beacon sound |
| **Big damage** | A living entity loses enough health in one hit | Red ring, threshold 6 damage, silent |
| **Kill** | A living entity dies | Purple dome that stays where they fell |
| **Damage taken** | You take a heavy hit yourself | Red pillar — **off by default** |

Filters let you turn effects off for yourself, for other players, or for mobs
independently. **Own hits only** restricts damage effects to enemies *you* hit.

## Nine styles

`ring` · `shockwave` · `disc` · `dome` · `sphere` · `pillar` · `helix` ·
`burst` · `crown`

Rings are drawn with soft edges — the colour falls off to nothing on the inside
and the outside, which reads far better than a hard cut.

## About 25 settings per effect

Two colours with a gradient, rainbow mode, opacity, duration, fade in, fade
out, **drift** (how far the effect keeps travelling outwards while it fades),
start and end radius, height, thickness, corner count, ring count, spin speed,
height offset, "follow the target", trigger threshold, **size by damage**,
sound (which one, volume, pitch) and particles (which one, how many).

The menu has four tabs — Shape, Colour, Timing, Sound — so nothing needs
scrolling, plus colour sliders with a hex readout and a **preview button** that
plays the effect seven blocks in front of you. You see every change instantly,
without having to go and hit somebody.

## The fade actually fades

The effect keeps expanding outwards while it becomes transparent, and thins out
as it goes — so it dissolves instead of freezing in place and vanishing. Fade
times are set in **ticks, not percent**: make the effect last longer and the
fade stays exactly as long as before.

## Extras

- **Pop counter** in the HUD: who popped, and how often.
- Commands: `/popeffects` (short: `/pe`) with a subtree per effect.
- Key `P` opens the menu; two more keys are free to bind.
- German and English translations.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for your version
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) in your `mods` folder — **required**
3. Add the PopEffects jar for your Minecraft version next to it

**Minecraft 26.x needs Java 25**, which the game itself requires.

**Lunar Client:** open the launcher, pick your version on the left, click
*Settings* at the bottom right, open the *Mods* tab and drag both jars in.
Restart Lunar afterwards.

## Version support

| Minecraft | PopEffects |
| --- | --- |
| 26.2 | 2.0.x |
| 1.21.11 · 1.21.10 | 1.4.x |

On 26.x and on 1.21.10 the optional **Glow** and **Through walls** switches are
unavailable — Minecraft does not expose the render building blocks needed for
them there, so the menu greys them out instead of offering something that does
nothing. Both are off by default anyway; everything else is identical.

## Nothing happening?

Nine times out of ten **Fabric API** is missing from the `mods` folder.
Otherwise `/pe status` tells you whether the mod and the individual effects are
switched on.
