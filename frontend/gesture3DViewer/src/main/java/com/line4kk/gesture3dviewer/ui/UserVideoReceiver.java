package com.line4kk.gesture3dviewer.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicReference;

public class UserVideoReceiver implements Runnable {
    private final String address;

    private final ImageView imageView;
    private final AtomicReference<Image> pendingFrame = new AtomicReference<>(null);

    private volatile boolean running = false;

    public UserVideoReceiver(ImageView imageView) {
        this.imageView = imageView;
        address = "tcp://localhost:5556";
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext();
             ZMQ.Socket sub = context.createSocket(SocketType.SUB)) {
            sub.connect(address);
            sub.subscribe("");

            ZMQ.Poller poller = context.createPoller(1);
            poller.register(sub, ZMQ.Poller.POLLIN);

            while (running && !Thread.currentThread().isInterrupted()) {
                if (poller.poll(500) > 0 && poller.pollin(0)) {
                    byte[] frameBytes = sub.recv();
                    try {
                        Image image = new Image(new ByteArrayInputStream(frameBytes));
                        pendingFrame.set(image);
                    } catch (Exception e) {
                        // Error log
                    }
                }
            }

            poller.close();
        }
    }

    public void tick() {
        Image frame = pendingFrame.getAndSet(null);
        if (frame != null) {
            imageView.setImage(frame);
        }
    }

    public void start() {
        if (running) return;
        running = true;
        imageView.setVisible(true);

        Thread t = new Thread(this);
        t.setDaemon(true);
        t.setName("video-receiver");
        t.start();
    }

    public void stop() {
        running = false;
        imageView.setVisible(false);
    }
}