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
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            t.delay--;
            if (t.delay <= 0) {
                t.task.run();
                it.remove();
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
