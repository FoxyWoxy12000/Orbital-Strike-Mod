package com.orbitalstrike.core.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.Iterator;
import java.util.LinkedList;

public class StrikeScheduler {

    private static final LinkedList<Task> TASKS = new LinkedList<>();
    private static boolean registered = false;

    public static void init() {
        if (registered) return;
        registered = true;

        ServerTickEvents.END_SERVER_TICK.register(server -> tick(server));
    }

    public static void schedule(int delay, Runnable task) {
        TASKS.add(new Task(delay, task));
    }

    private static void tick(MinecraftServer server) {
        // Snapshot current tasks; new schedules during run() go into TASKS fresh
        LinkedList<Task> current = new LinkedList<>(TASKS);
        TASKS.clear();

        for (Task t : current) {
            t.delay--;
            if (t.delay <= 0) {
                t.task.run(); // safe — any schedule() calls go into the now-empty TASKS
            } else {
                TASKS.add(t); // not ready yet, keep it
            }
        }
    }

    private static class Task {
        int delay;
        Runnable task;

        Task(int delay, Runnable task) {
            this.delay = delay;
            this.task = task;
        }
    }
}
