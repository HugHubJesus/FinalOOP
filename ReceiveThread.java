import java.io.*;

// เทรดคอยฟังข้อความจากอีกฝั่งตลอดเวลา (ใช้ทั้ง Server และ Client)
// fromId = เลขของคนที่อยู่ปลายสายนี้
public class ReceiveThread extends Thread {
    private Network network;
    private BufferedReader in;
    private int fromId;

    public ReceiveThread(Network network, BufferedReader in, int fromId) {
        this.network = network;
        this.in = in;
        this.fromId = fromId;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = in.readLine();   // หยุดรอตรงนี้จนกว่าจะมีข้อความมา
                if (message == null) {
                    break;   // อีกฝั่งปิดเกมไปแล้ว
                }
                network.readMessage(message);

                // ถ้าเราเป็น Server ส่งต่อให้คนอื่นด้วย
                if (network.isServer) {
                    network.sendToClients(message, fromId);
                }
            }
        } catch (IOException e) {
            // สายหลุด
        }
        network.playerLeft(fromId);
    }
}
