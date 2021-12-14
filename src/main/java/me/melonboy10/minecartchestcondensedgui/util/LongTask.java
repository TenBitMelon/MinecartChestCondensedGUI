package me.melonboy10.minecartchestcondensedgui.util;

public abstract class LongTask {

    private boolean delayScheduled;
    private boolean broken = false;

    public abstract void initialize();

    public abstract boolean condition();

    public abstract void increment();

    public abstract void body();

    public final void _break() {
        broken = true;
    }

    public final boolean isCompleted() {
        return broken || !condition();
    }

    public void onCompleted() {}

    public final void scheduleDelay() {
        delayScheduled = true;
    }

    public final void unscheduleDelay() {
        delayScheduled = false;
    }

    public final boolean isDelayScheduled() {
        return delayScheduled;
    }

    public boolean stopOnWorldUnload(boolean isDisconnect) {
        return true;
    }

}