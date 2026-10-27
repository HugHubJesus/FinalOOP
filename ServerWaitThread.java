import java.io.*;
import java.net.*;

// เทรดของ Server: คอยรอคนต่อเข้ามาเรื่อยๆ คนที่เข้ามาจะได้ id 1, 2, 3, 4 ตามลำดับ
public class ServerWaitThread extends Thread {
    private Network network;
    private ServerSocket serverSocket;

    public ServerWaitThread(Network network, ServerSocket serverSocket) {
        this.network = network;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        for (int id = 1; id < Network.MAX_PLAYERS; id++) {
            try {
                Socket socket = serverSocket.accept();   // หยุดรอตรงนี้จนกว่าจะมีคนต่อเข้ามา
                network.addClient(socket, id);
            } catch (IOException e) {
                System.out.println("Connect error: " + e.getMessage());
            }
        }
    }
}
