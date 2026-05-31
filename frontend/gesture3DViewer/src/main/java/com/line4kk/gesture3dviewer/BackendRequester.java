package com.line4kk.gesture3dviewer;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class BackendRequester {

    private static final String ADDRESS = "tcp://localhost:5557";
    private static final int TIMEOUT_MS = 2000;

    public static String send(String command) {
        try (ZContext context = new ZContext();
             ZMQ.Socket req = context.createSocket(SocketType.REQ)) {

            req.connect(ADDRESS);
            req.setSendTimeOut(TIMEOUT_MS);
            req.setReceiveTimeOut(TIMEOUT_MS);

            req.send(command);

            String reply = req.recvStr();
            return reply != null ? reply : "NO_REPLY";

        } catch (Exception e) {
            return "ERROR";
        }
    }
}