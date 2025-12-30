package com.example.orbitalcannon;

/**
 * Runtime mod configuration (defaults). /orbital config updates these if enabled.
 */
public class OrbitalConfig {
    // Stab defaults
    public static int stabSpacing = 3;
    public static int stabTntCount = 5;
    public static double stabOffset = 0.5;
    public static int stabFuse = 2;

    // Nuke defaults
    public static int nukeTntPerRingBase = 6;
    public static int nukeBaseFuse = 110;
    public static int nukeRingSpacing = 2;
    public static int nukeOffsetHeight = 100;
    public static double nukeSpreadBaseSpeed = 0.3; // how fast inner ring spreads
    public static double nukeSpreadRingMultiplier = 0.05;

    // Arrow defaults
    public static int arrowCount = 40;
    public static double arrowSpread = 5.0;
    public static int arrowWaves = 2;
    public static int arrowWaveDelay = 5;

    // WorldDestroyer defaults (hybrid of stab + nuke)
    public static int wdSpacing = 3;
    public static int wdTntCount = 5;
    public static double wdOffset = 0.5;
    public static int wdFuse = 2;

    public static int wdTntPerRingBase = 6;
    public static int wdBaseFuse = 110;
    public static int wdRingSpacing = 3;
    public static int wdOffsetHeight = 100;

    // Damage multiplier
    public static double multiplyDamage = 5.0;

    // setters
    public static void setStabSpacing(int v) { stabSpacing = Math.max(1, v); }
    public static void setStabTntCount(int v) { stabTntCount = Math.max(1, v); }
    public static void setStabOffset(double v) { stabOffset = v; }
    public static void setStabFuse(int v) { stabFuse = Math.max(1, v); }

    public static void setNukeTntPerRingBase(int v) { nukeTntPerRingBase = Math.max(1, v); }
    public static void setNukeBaseFuse(int v) { nukeBaseFuse = Math.max(1, v); }
    public static void setNukeRingSpacing(int v) { nukeRingSpacing = Math.max(1, v); }
    public static void setNukeOffsetHeight(int v) { nukeOffsetHeight = v; }
    public static void setNukeSpreadBaseSpeed(double v) { nukeSpreadBaseSpeed =  v; }
    public static void setNukeSpreadRingMultiplier(double v) { nukeSpreadRingMultiplier = v; }


    public static void setArrowCount(int v) { arrowCount = Math.max(1, v); }
    public static void setArrowSpread(double v) { arrowSpread = v; }
    public static void setArrowWaves(int v) { arrowWaves = Math.max(1, v); }
    public static void setArrowWaveDelay(int v) { arrowWaveDelay = Math.max(1, v); }

    public static void setWdSpacing(int v) { wdSpacing = Math.max(1, v); }
    public static void setWdTntCount(int v) { wdTntCount = Math.max(1, v); }
    public static void setWdOffset(double v) { wdOffset = v; }
    public static void setWdFuse(int v) { wdFuse = Math.max(1, v); }

    public static void setWdTntPerRingBase(int v) { wdTntPerRingBase = Math.max(1, v); }
    public static void setWdBaseFuse(int v) { wdBaseFuse = Math.max(1, v); }
    public static void setWdRingSpacing(int v) { wdRingSpacing = Math.max(1, v); }
    public static void setWdOffsetHeight(int v) { wdOffsetHeight = v; }

    public static void setMultiplyDamage(double v) { multiplyDamage = Math.max(0.1, v); }
}
