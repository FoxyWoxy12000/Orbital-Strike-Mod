package com.orbitalstrike.core.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.List;

public final class TickScheduler {

    private static final List<Task> TASKS = new ArrayList<>();
    private static boolean registered = false;

    private static class Task {
        int ticksLeft;
        final Runnable action;

        Task(int ticksLeft, Runnable action) {
            this.ticksLeft = ticksLeft;
            this.action = action;
        }
    }

    public static void init() {
        if (registered) return;
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            List<Runnable> due = new ArrayList<>();
            for (int i = TASKS.size() - 1; i >= 0; i--) {
                Task t = TASKS.get(i);
                if (--t.ticksLeft <= 0) {
                    due.add(t.action);
                    TASKS.remove(i);
                }
            }
            due.forEach(r -> {
                try {
                    r.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static void schedule(int ticks, Runnable action) {
        TASKS.add(new Task(ticks, action));
    }
}