# HitFX (Fabric, Minecraft 1.21.1, Yarn mappings, Java 21)

Client-side PvP feedback mod with two colour themes (violet, gold).

| Feature | What it does |
|---|---|
| Ripple | Sword hit -> target flashes as a translucent themed shell + particle ring. 5s cooldown shown by the crosshair. |
| Hit Splash | Ink-style burst at the impact point, thrown away from you, with heavy droplets. |
| Crit Stars | Crits throw a burst of star sparks. |
| Shield Breaker | Axe on a raised shield: shatter burst, sound, and a SHIELD DOWN countdown under the Target HUD. |
| Backtrack | Glowing outline where your target probably is on the server (your ping + interpolation, along their velocity). |
| Afterglow | On a player's death: rising light column, sky beam, spreading ground ring. |
| Target HUD | Panel beside the crosshair: name, weapon, health, distance, combo, hits/swings. |

## Build
```
gradle wrapper --gradle-version 8.8      # one-time, needs a local Gradle install
./gradlew build                          # jar lands in build/libs/hitfx-1.0.0.jar
./gradlew runClient                      # test in a dev client
```

## Commands (client-side)
```
/hitfx theme violet|gold
/hitfx <ripple|hit_splash|crit_stars|shield_breaker|backtrack|afterglow|target_hud> on|off
```
Settings persist in `config/hitfx.properties`.

## Honest notes
- Untested: I couldn't compile this here. Expect to fix a few Yarn names if you change versions.
- Targets 1.21.1. Minecraft 26.1+ dropped Yarn (Mojang names, Java 25, `net.fabricmc.fabric-loom` plugin), so it needs a port.
- `Theme.dust()` uses the 1.21.0-1.21.1 `DustParticleEffect(Vector3f, float)` constructor; 1.21.2+ takes an int colour.
- Backtrack is an estimate, not true server position, and draws an outline, not a ghost model.
- Hits are confirmed by the target's hurt animation, so blocked or zero-damage hits won't trigger Ripple/Splash/Stars.
- Target HUD sits at a fixed spot beside the crosshair (not projected onto the entity).
