package com.line4kk.gesture3dviewer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.line4kk.gesture3dviewer.model.GestureMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class DataReceiver implements Runnable {
    private final String address;
    private volatile boolean running = true;

    public DataReceiver() {
        address = "tcp://localhost:5555";
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect(address);
            socket.subscribe("".getBytes(ZMQ.CHARSET));

            ObjectMapper mapper = new ObjectMapper();

            while (running && !Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr();
                try {
                    GestureMessage gestureMessage = mapper.readValue(message, GestureMessage.class);
                    System.out.println(gestureMessage);
                } catch (JsonProcessingException e) {
                    // Error logg
                }

            }
        }
    }

    public void stop() {
        running = false;
    }
}
