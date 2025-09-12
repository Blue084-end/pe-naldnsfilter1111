package dnsfilter.remote;

import java.io.*;
import java.net.*;

public class RemoteAccessServer extends Thread {

    private static final int PORT = 5454;
    private static final String AUTH_PASSWORD = "AzSuperSecure123"; // Đổi mật khẩu tại đây
    private static final int TIMEOUT_MS = 10000; // 10 giây timeout

    private boolean running = true;

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[RemoteAccessServer] Đang lắng nghe tại cổng " + PORT);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    InetAddress clientAddress = clientSocket.getInetAddress();

                    // Chỉ cho phép localhost
                    if (!clientAddress.isLoopbackAddress()) {
                        System.out.println("[RemoteAccessServer] Từ chối truy cập từ IP: " + clientAddress.getHostAddress());
                        clientSocket.close();
                        continue;
                    }

                    clientSocket.setSoTimeout(TIMEOUT_MS);

                    try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
                    ) {
                        System.out.println("[RemoteAccessServer] Kết nối từ: " + clientAddress.getHostAddress());

                        // Xác thực mật khẩu
                        out.write("Vui lòng nhập mật khẩu:\n");
                        out.flush();
                        String password = in.readLine();

                        if (!AUTH_PASSWORD.equals(password)) {
                            out.write("Sai mật khẩu. Kết nối bị từ chối.\n");
                            out.flush();
                            clientSocket.close();
                            continue;
                        }

                        out.write("Xác thực thành công. Bạn có thể gửi lệnh.\n");
                        out.flush();

                        // Xử lý lệnh đơn giản
                        String command;
                        while ((command = in.readLine()) != null) {
                            if ("status".equalsIgnoreCase(command)) {
                                out.write("DNS Filter đang hoạt động.\n");
                            } else if ("exit".equalsIgnoreCase(command)) {
                                out.write("Đóng kết nối.\n");
                                break;
                            } else {
                                out.write("Lệnh không hợp lệ.\n");
                            }
                            out.flush();
                        }

                    } catch (IOException e) {
                        System.err.println("[RemoteAccessServer] Lỗi khi xử lý client: " + e.getMessage());
                    } finally {
                        clientSocket.close();
                    }

                } catch (IOException e) {
                    System.err.println("[RemoteAccessServer] Lỗi kết nối: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("[RemoteAccessServer] Không thể khởi động server: " + e.getMessage());
        }
    }

    public void stopServer() {
        running = false;
        System.out.println("[RemoteAccessServer] Đã dừng server.");
    }
}
