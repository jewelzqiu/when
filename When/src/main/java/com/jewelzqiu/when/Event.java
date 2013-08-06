package com.jewelzqiu.when;

/**
 * Created by jewelzqiu on 8/6/13.
 */
public class Event {

    public static final int EVENT_TYPE_TIME = 0;
    public static final int EVENT_TYPE_NORMAL = 1;

    private long time;
    private String trigger;
    private int triggerIndex;
    private String action;
    private int actionIndex;
    private boolean isEnabled;
    private int type;

    public Event(String action) {
        this.type = EVENT_TYPE_NORMAL;
        this.action = action;
        this.isEnabled = false;
    }

    public Event(String trigger, String action) {
        this.type = EVENT_TYPE_NORMAL;
        this.action = action;
        this.trigger = trigger;
        this.isEnabled = false;
    }

    public Event(long time, String action) {
        this.type = EVENT_TYPE_TIME;
        this.time = time;
        this.action = action;
        this.isEnabled = true;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
