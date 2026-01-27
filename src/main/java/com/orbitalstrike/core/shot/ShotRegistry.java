package com.orbitalstrike.core.shot;

import java.util.HashMap;
import java.util.Map;

public class ShotRegistry {

    private static final Map<String, OrbitalShot> SHOTS = new HashMap<>();

    public static void register(OrbitalShot shot) {
        SHOTS.put(shot.id(), shot);
    }

    public static OrbitalShot get(String id) {
        return SHOTS.get(id);
    }
}
