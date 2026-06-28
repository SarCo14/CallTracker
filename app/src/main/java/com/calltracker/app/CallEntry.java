package com.calltracker.app;

public class CallEntry {
    public String name;
    public String type;
    public long date;
    public int duration;

    public CallEntry(String name, String type, long date, int duration) {
        this.name = name;
        this.type = type;
        this.date = date;
        this.duration = duration;
    }

    public String getFormattedDuration() {
        if (duration <= 0) return "";
        int min = duration / 60;
        int sec = duration % 60;
        if (min > 0) return min + "min " + sec + "s";
        return sec + "s";
    }
}
