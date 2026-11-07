package com.example.hitfx;

import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;

/** Colour themes. Add a row here and it automatically appears in /hitfx theme. */
public enum Theme {
    VIOLET(200, 90, 255, 255, 158, 242),
    GOLD(255, 196, 60, 255, 236, 160);

    public final int r, g, b, r2, g2, b2;

    Theme(int r, int g, int b, int r2, int g2, int b2) {
        this.r = r; this.g = g; this.b = b; this.r2 = r2; this.g2 = g2; this.b2 = b2;
    }

    public int argb(int alpha) { return (alpha << 24) | (r << 16) | (g << 8) | b; }
    public int rgb() { return (r << 16) | (g << 8) | b; }
    public int rgb2() { return (r2 << 16) | (g2 << 8) | b2; }

    // 1.21.0-1.21.1 signature (Vector3f). On 1.21.2+ this takes an int colour instead.
    public DustParticleEffect dust(float size)  { return new DustParticleEffect(new Vector3f(r / 255f, g / 255f, b / 255f), size); }
    public DustParticleEffect dust2(float size) { return new DustParticleEffect(new Vector3f(r2 / 255f, g2 / 255f, b2 / 255f), size); }
}
