package com.hrs.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (InputStream in = clientSocket.getInputStream();
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
        	

            String input;
            byte[] buf = new byte[1];
            StringBuilder sb = new StringBuilder();
            int balance = 0;
            
            while (true) {
                int n = in.read(buf);
                input = Character.toString(buf[0]);
                sb.append(input);

                if ("{".equals(input)) {
                    balance++;
                } else if ("}".equals(input)) {
                    balance--;
                }

                if (balance <= 0) {
                    try {
                        if (sb.length() > 3 && sb.charAt(0) == '{') {
                            DemoApplication.processMessage(sb, out);
                            System.out.println(sb);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sb.setLength(0);
                }
            }
        } catch (IOException e) {
        	System.out.println("device disconnected");
            //e.printStackTrace();
        }
    }
}