package com.line4kk.gesture3dviewer.model;

public class GestureMessage {
    public String type;
    public Double dx;
    public Double dy;
    public Double dz;
    public Double scale;

    @Override
    public String toString() {
        return "GestureMessage{" +
                "type='" + type + '\'' +
                ", dx=" + dx +
                ", dy=" + dy +
                ", dz=" + dz +
                ", scale=" + scale +
                '}';
    }
}
