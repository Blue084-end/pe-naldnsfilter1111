package dnsfilter.remote;

import java.io.*;
import java.net.*;

public class RemoteAccessClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5454;
    private static final String AUTH_PASSWORD = "AzSuperSecure123"; // Phải khớp với server

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            socket.setSoTimeout(10000); // Timeout 10 giây

            // Nhận yêu cầu xác thực từ server
            String serverMessage = in.readLine();
            System.out.println("[Server] " + serverMessage);

            // Gửi mật khẩu xác thực
            out.write(AUTH_PASSWORD + "\n");
            out.flush();

            // Nhận phản hồi xác thực
            serverMessage = in.readLine();
            System.out.println("[Server] " + serverMessage);

            if (!serverMessage.toLowerCase().contains("thành công")) {
                System.out.println("[Client] Xác thực thất bại. Dừng kết nối.");
                return;
            }

            // Gửi lệnh từ người dùng
            String command;
            while (true) {
                System.out.print("Nhập lệnh ('status', 'exit', ...): ");
                command = userInput.readLine();
                if (command == null || command.trim().isEmpty()) continue;

                out.write(command + "\n");
                out.flush();

                String response = in.readLine();
                System.out.println("[Server] " + response);

                if ("exit".equalsIgnoreCase(command)) break;
            }

        } catch (IOException e) {
            System.err.println("[Client] Lỗi kết nối hoặc xử lý: " + e.getMessage());
        }
    }
}
