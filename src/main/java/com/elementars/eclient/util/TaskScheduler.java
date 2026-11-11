package com.elementars.eclient.util;

import java.util.ArrayList;

public class TaskScheduler
{
    private ArrayList<Runnable> tasks;

    public TaskScheduler() {
        this.tasks = new ArrayList<Runnable>();
    }

    public void add(final Runnable task) {
        this.tasks.add(task);
    }

    public void onUpdate() {
        if (this.tasks.isEmpty()) {
            return;
        }
        final ArrayList<Runnable> copy = new ArrayList<Runnable>(this.tasks);
        this.tasks.clear();
        for (final Runnable task : copy) {
            task.run();
        }
    }
}
