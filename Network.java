import java.io.*;
import java.net.*;

// ระบบเล่นหลายคน
// - เครื่อง Server (คนเปิดห้อง) เป็นตัวกลาง รับข้อความจากทุก Client แล้วส่งต่อให้คนอื่นทุกคน
// - เครื่อง Client (คนเข้าห้อง) ส่งข้อความไปหา Server อย่างเดียว
//
// ข้อความ 1 บรรทัด = "id ด่าน x y จำนวนครั้งที่ตาย เข้าประตูแล้ว(0/1)"
// เช่น "2 3 150 410 5 0" = ผู้เล่น id 2 อยู่ด่าน 3 ตำแหน่ง (150, 410) ตายไป 5 ครั้ง ยังไม่เข้าประตู
public class Network {
    public static final int MAX_PLAYERS = 5;   // มีไก่ 5 สี เลยเล่นได้สูงสุด 5 คน
    public static final int PORT = 50101;

    public int myId = 0;              // Server = 0, Client ได้เลขจาก Server ตอนต่อเข้ามา
    public boolean isServer = false;

    private PrintWriter toServer;                                    // ใช้ตอนเป็น Client: ช่องส่งไปหา Server
    private PrintWriter[] toClients = new PrintWriter[MAX_PLAYERS];  // ใช้ตอนเป็น Server: ช่องส่งไปหา Client แต่ละคน ตาม id

    // ข้อมูลผู้เล่นแต่ละคน เก็บตามเลข id
    // friendLevel = -1 คือไม่มีคนนี้ หรือหลุดไปแล้ว, 0 = อยู่หน้า lobby
    public int[] friendLevel = new int[MAX_PLAYERS];
    public int[] friendX = new int[MAX_PLAYERS];
    public int[] friendY = new int[MAX_PLAYERS];
    public int[] friendDeaths = new int[MAX_PLAYERS];
    public boolean[] friendAtDoor = new boolean[MAX_PLAYERS];

    // ip = null คือเปิดห้องเป็น Server, มี ip คือเข้าห้องเป็น Client
    public Network(String ip) throws IOException {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            friendLevel[i] = -1;   // ตอนแรกยังไม่มีใคร
        }
        if (ip == null) {
            startServer();
        } else {
            startClient(ip);
        }
    }

    // ==================== ฝั่ง Server ====================

    private void startServer() throws IOException {
        isServer = true;
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server waiting on port " + PORT + " ...");

        // เปิดเทรดคอยรับคนที่ต่อเข้ามา
        ServerWaitThread waitThread = new ServerWaitThread(this, serverSocket);
        waitThread.start();
    }

    // มี Client ต่อเข้ามาใหม่ (ServerWaitThread เป็นคนเรียก)
    public void addClient(Socket socket, int id) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("ID " + id);   // บอก Client ว่าได้เลขอะไร
        toClients[id] = out;
        System.out.println("Player " + (id + 1) + " connected");

        // เปิดเทรดคอยฟังข้อความจาก Client คนนี้
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ReceiveThread receiveThread = new ReceiveThread(this, in, id);
        receiveThread.start();
    }

    // Server ส่งข้อความให้ Client ทุกคน ยกเว้นคน skipId (เจ้าของข้อความ)
    public void sendToClients(String message, int skipId) {
        for (int i = 1; i < MAX_PLAYERS; i++) {
            if (i != skipId && toClients[i] != null) {
                toClients[i].println(message);
            }
        }
    }

    // ==================== ฝั่ง Client ====================

    private void startClient(String ip) throws IOException {
        isServer = false;
        Socket socket = new Socket(ip, PORT);   // ต่อไปหา Server
        toServer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // บรรทัดแรกที่ Server ส่งมา = "ID เลข" เช่น "ID 2"
        String firstLine = in.readLine();
        String[] parts = firstLine.split(" ");
        myId = Integer.parseInt(parts[1]);
        System.out.println("Connected as player " + (myId + 1));

        // เปิดเทรดคอยฟังข้อความจาก Server (Server คือ id 0)
        ReceiveThread receiveThread = new ReceiveThread(this, in, 0);
        receiveThread.start();
    }

    // ==================== ใช้ทั้ง 2 ฝั่ง ====================

    // ส่งข้อมูลของเราออกไป
    public void send(int level, int x, int y, int deaths, boolean atDoor) {
        int door = 0;
        if (atDoor) {
            door = 1;
        }
        String message = myId + " " + level + " " + x + " " + y + " " + deaths + " " + door;

        if (isServer) {
            sendToClients(message, 0);   // Server ส่งให้ Client ทุกคน
        } else {
            toServer.println(message);   // Client ส่งให้ Server
        }
    }

    // ได้รับข้อความมา: แยกเป็นส่วนๆ แล้วเก็บไว้ในช่องของคนนั้น
    public void readMessage(String message) {
        String[] parts = message.split(" ");
        int id = Integer.parseInt(parts[0]);
        friendX[id] = Integer.parseInt(parts[2]);
        friendY[id] = Integer.parseInt(parts[3]);
        friendDeaths[id] = Integer.parseInt(parts[4]);
        friendAtDoor[id] = parts[5].equals("1");
        friendLevel[id] = Integer.parseInt(parts[1]);   // ใส่ด่านทีหลังสุด ข้อมูลอื่นจะได้พร้อมก่อน
    }

    // ผู้เล่นคนนี้หลุดออกจากเกม
    public void playerLeft(int id) {
        System.out.println("Player " + (id + 1) + " left the game");
        friendLevel[id] = -1;
        if (isServer) {
            toClients[id] = null;
            sendToClients(id + " -1 0 0 0 0", id);   // บอกคนอื่นว่าคนนี้หลุดแล้ว (ด่าน -1)
        }
    }
}
