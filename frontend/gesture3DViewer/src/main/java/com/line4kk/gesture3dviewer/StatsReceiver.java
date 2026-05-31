package com.line4kk.gesture3dviewer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.atomic.AtomicReference;

public class StatsReceiver implements Runnable {

    public record StatsSnapshot(double fps, int hands_num, String current_pose) {}

    private final String address;
    private volatile boolean running = true;
    private final AtomicReference<StatsSnapshot> latest = new AtomicReference<>();

    public StatsReceiver() {
        this.address = "tcp://localhost:5558";
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.setRcvHWM(1);
            socket.connect(address);
            socket.subscribe("".getBytes(ZMQ.CHARSET));

            ObjectMapper mapper = new ObjectMapper();

            while (running && !Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr();
                if (message == null) continue;
                try {
                    latest.set(mapper.readValue(message, StatsSnapshot.class));
                } catch (Exception ignored) {}
            }
        }
    }

    public StatsSnapshot poll() {
        return latest.get();
    }

    public void stop() {
        running = false;
    }
}