package com.line4kk.gesture3dviewer.model;

public class GestureMessage {
    public String type;
    public Double dx;
    public Double dy;
    public Double dz;
    public Double dr;

    @Override
    public String toString() {
        return "GestureMessage{" +
                "type='" + type + '\'' +
                ", dx=" + dx +
                ", dy=" + dy +
                ", dz=" + dz +
                ", dr=" + dr +
                '}';
    }
}
