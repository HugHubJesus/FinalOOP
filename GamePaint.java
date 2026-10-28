import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// หน้าจอเกม: ลูปเกม + วาดจอ + รับปุ่มกด
public class GamePaint extends JPanel implements Runnable, KeyListener, ActionListener {
    public static final int SCREEN_WIDTH = 1000;
    public static final int SCREEN_HEIGHT = 600;

    private int startX = 50;     // จุดเกิดของไก่
    private int startY = 410;

    private int levelNumber = 1;   // 0 = หน้า lobby รอเพื่อน (เฉพาะตอนเล่นกับเพื่อน)
    private int deaths = 0;
    private boolean win = false;
    private Level level = new Level(1);
    private Image background;
    private Player player;

    // ช่วงเปลี่ยนฉาก (ตาย / เข้าประตู) waitTime นับถอยหลังจาก 80 ถึง 0 (0 = เล่นปกติ)
    // ครึ่งแรกจอค่อยๆ มืด -> ตรงกลางเปลี่ยนด่าน -> ครึ่งหลังจอค่อยๆ สว่าง
    private int fullWaitTime = 80;
    private int halfWaitTime = 40;
    private int waitTime = 0;
    private boolean dying = false;   // true = กำลังตาย, false = กำลังผ่านด่าน
    private boolean atDoor = false;  // เข้าประตูแล้ว ไก่หายเข้าไปรอเพื่อนในประตู

    // สีไก่ตามเลขผู้เล่น (id 0 = Server) ทุกเครื่องใช้ตารางเดียวกัน เลยเห็นสีตรงกัน
    private String[] colors = {"white", "brown", "black", "gray", "golden"};
    private Image[] icons = new Image[Network.MAX_PLAYERS];   // รูปไก่แต่ละสี ไว้โชว์ในหน้า lobby

    // เล่นกับเพื่อน (network = null คือเล่นคนเดียว)
    private Network network;
    private int myId = 0;
    private Player[] friends = new Player[Network.MAX_PLAYERS];       // ไก่เพื่อนแต่ละคน ตาม id
    private Player[] friendsHere = new Player[Network.MAX_PLAYERS];   // เพื่อนที่อยู่ด่านเดียวกับเรา (null = ไม่อยู่)
    private int[] lastFriendDeaths = new int[Network.MAX_PLAYERS];    // จำนวนตายของเพื่อนรอบที่แล้ว (-1 = ยังไม่เคยเห็น)
    private boolean friendDied = false;   // เริ่มใหม่เพราะเพื่อนตาย (ไว้โชว์ข้อความ)

    // ปุ่ม: START (หน้า lobby เฉพาะคนเปิดห้อง) / PLAY AGAIN (ตอนจบเกม)
    private JButton startButton = new JButton("START");
    private JButton restartButton = new JButton("PLAY AGAIN");
    private boolean startClicked = false;     // กดปุ่มแล้ว ให้ลูปเกมเป็นคนจัดการต่อ
    private boolean restartClicked = false;

    public GamePaint(Network network) {
        this.network = network;
        if (network != null) {
            myId = network.myId;
            levelNumber = 0;   // เล่นกับเพื่อน เริ่มที่หน้า lobby
        }

        player = new Player(startX, startY, colors[myId]);
        for (int i = 0; i < Network.MAX_PLAYERS; i++) {
            if (i != myId) {
                friends[i] = new Player(startX, startY, colors[i]);
            }
            lastFriendDeaths[i] = -1;
            icons[i] = GameObject.loadImage("Image/Chicken/" + colors[i] + "/stand_right.png");
        }

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.WHITE);   // สีพื้นหลัง (ใช้ตอนโหลดรูปไม่ได้)
        background = GameObject.loadImage("Image/Map/background.png");

        // วางปุ่มเองตามตำแหน่ง x, y
        setLayout(null);
        startButton.setBounds(400, 470, 200, 50);
        restartButton.setBounds(400, 300, 200, 50);
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        restartButton.setFont(new Font("Arial", Font.BOLD, 24));
        startButton.setFocusable(false);     // กดปุ่มแล้วคีย์บอร์ดยังอยู่ที่เกม
        restartButton.setFocusable(false);
        startButton.setVisible(false);
        restartButton.setVisible(false);
        add(startButton);
        add(restartButton);

        startButton.addActionListener(this);
        restartButton.addActionListener(this);
        addKeyListener(this);
        setFocusable(true);
    }

    // ==================== รับปุ่มกด ====================

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            startClicked = true;
        }
        if (e.getSource() == restartButton) {
            restartClicked = true;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A) {
            player.left = true;
        }
        if (key == KeyEvent.VK_D) {
            player.right = true;
        }
        if (key == KeyEvent.VK_SPACE) {
            player.jump = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A) {
            player.left = false;
        }
        if (key == KeyEvent.VK_D) {
            player.right = false;
        }
        if (key == KeyEvent.VK_SPACE) {
            player.jump = false;
        }
    }

    // บังคับให้ต้องมี
    public void keyTyped(KeyEvent e) {
    }

    // ==================== ลูปเกม ====================

    // เริ่มเกม สร้างเทรดใหม่มารัน
    public void start() {
        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    // ลูปเกม
    public void run() {
        while (true) {
            if (network != null) {
                updateFriends();
            }
            updateButtons();

            if (waitTime > 0) {
                changeScene();         // กำลังเปลี่ยนฉาก เกมหยุดนิ่ง
            } else if (win == false && levelNumber > 0) {
                playGame();            // เล่นปกติ
            }

            repaint();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

    private void startChangeScene(boolean isDying) {
        dying = isDying;
        waitTime = fullWaitTime;
    }

    // เล่นปกติ 1 เฟรม
    private void playGame() {
        if (atDoor == false) {
            player.update(level.floors, level.floorCount);   // ขยับไก่ (เข้าประตูแล้วไม่ขยับ)
        }
        level.update(player, friendsHere);   // เช็คกับดัก

        if (atDoor == false && level.isDead(player)) {
            deaths++;
            friendDied = false;
            startChangeScene(true);
        } else if (atDoor || level.isWin(player)) {
            atDoor = true;
            if (canLeave()) {
                startChangeScene(false);
            }
        }
    }

    // ช่วงเปลี่ยนฉาก 1 เฟรม
    private void changeScene() {
        waitTime--;

        // ถึงครึ่งทาง (จอมืดสุดแล้ว) ค่อยเปลี่ยนด่านตรงนี้ ผู้เล่นจะได้ไม่เห็น
        if (waitTime == halfWaitTime) {
            if (dying == false) {
                levelNumber++;
            }
            if (levelNumber > Level.LAST_LEVEL) {
                win = true;
            } else {
                level = new Level(levelNumber);   // สร้างด่านใหม่ กับดักกลับเป็นแบบเดิม
                player.respawn(startX, startY);
                atDoor = false;
            }
        }
    }

    // เริ่มเล่นใหม่ตั้งแต่ด่าน 1
    private void restart() {
        levelNumber = 1;
        win = false;
        atDoor = false;
        waitTime = 0;
        level = new Level(1);
        player.respawn(startX, startY);
    }

    // โชว์/ซ่อนปุ่ม แล้วเช็คว่ามีคนกดปุ่มมั้ย
    private void updateButtons() {
        // ปุ่ม START โชว์เฉพาะคนเปิดห้อง (id 0) ตอนอยู่หน้า lobby
        if (network != null && myId == 0 && levelNumber == 0 && waitTime == 0) {
            startButton.setVisible(true);
        } else {
            startButton.setVisible(false);
        }

        restartButton.setVisible(win);

        if (startClicked) {
            startClicked = false;
            startChangeScene(false);   // เปลี่ยนฉากจาก lobby ไปด่าน 1
        }
        if (restartClicked) {
            restartClicked = false;
            restart();
        }
    }

    // ==================== เล่นกับเพื่อน ====================

    private void updateFriends() {
        network.send(levelNumber, player.getBounds().x, player.getBounds().y, deaths, atDoor);

        for (int i = 0; i < Network.MAX_PLAYERS; i++) {
            friendsHere[i] = null;
            boolean isMe = (i == myId);
            boolean online = network.friendLevel[i] >= 0;
            if (isMe == false && online) {
                updateOneFriend(i);
            }
        }
    }

    // อัปเดตเพื่อนคนเดียว (id = เลขของเพื่อน)
    private void updateOneFriend(int id) {
        int friendLevel = network.friendLevel[id];
        friends[id].moveTo(network.friendX[id], network.friendY[id]);
        //ย้ายเพื่อน

        // อยู่ด่านเดียวกับเรา
        if (friendLevel == levelNumber) {
            friendsHere[id] = friends[id];
        }

        // เพื่อนเพิ่งตายในด่านเดียวกับเรา
        // (ไม่นับเป็นตายของเรา ไม่งั้นจะสั่งกันตายวนไปมา)
        if (network.friendDeaths[id] != lastFriendDeaths[id]) {
            boolean firstTime = (lastFriendDeaths[id] == -1);   // เพิ่งเห็นคนนี้ครั้งแรก ไม่ใช่ตาย
            lastFriendDeaths[id] = network.friendDeaths[id];
            if (firstTime == false && friendLevel == levelNumber && waitTime == 0 && win == false) {
                friendDied = true;
                startChangeScene(true);
            }
        }

        // เพื่อนไปด่านถัดไปแล้ว หรือคนเปิดห้องกด START
        // ไม่นับเพื่อนที่จบเกมแล้ว ไม่งั้นกด PLAY AGAIN แล้วจะโดนดึงกลับไปหน้าจบ
        if (friendLevel > levelNumber && friendLevel <= Level.LAST_LEVEL && waitTime == 0 && win == false) {
            startChangeScene(false);
        }

        // เราจบเกมแล้ว แต่เพื่อนกด PLAY AGAIN กลับไปด่าน 1 = เราเล่นใหม่ด้วย
        if (win && friendLevel == 1) {
            restartClicked = true;
        }
    }

    // ออกจากด่านได้หรือยัง
    // เล่นคนเดียว เข้าประตูได้เลย ยกเว้นด่านเล่น 2 คน
    // เล่นกับเพื่อน ต้องรอจนเพื่อนทุกคนที่ออนไลน์เข้าประตูมาด้วย
    private boolean canLeave() {
        if (network == null) {
            if (levelNumber < Level.FIRST_COOP_LEVEL) {
                return true;
            } else {
                return false;
            }
        }

        int onlineCount = 0;
        for (int i = 0; i < Network.MAX_PLAYERS; i++) {
            int friendLevel = network.friendLevel[i];
            if (i != myId && friendLevel >= 0) {
                onlineCount++;
                if (friendLevel < levelNumber) {
                    return false;   // ยังมีคนตามมาไม่ถึง
                }
                if (friendLevel == levelNumber && network.friendAtDoor[i] == false) {
                    return false;   // ยังมีคนบ่เข้าประตู ถ้าเพื่อนไปด่านถัดไป ถือว่าเข้า
                }
            }
        }

        // ด่าน 2 คน แต่ไม่มีเพื่อนออน
        if (levelNumber >= Level.FIRST_COOP_LEVEL && onlineCount == 0) {
            return false;
        }
        return true;
    }

    // ==================== วาดจอ ====================

    // ความมืดของจอตอนเปลี่ยนฉาก (0 = ใส, 255 = มืดสนิท)
    private int getDarkness() {
        if (waitTime > halfWaitTime) {
            // ครึ่งแรก: ค่อยๆ มืดลง (waitTime 80 -> 40 ความมืด 0 -> 255)
            int passed = fullWaitTime - waitTime;
            return passed * 255 / halfWaitTime;
        } else {
            // ครึ่งหลัง: ค่อยๆ สว่างขึ้น (waitTime 40 -> 0 ความมืด 255 -> 0)
            return waitTime * 255 / halfWaitTime;
        }
    }

    // หน้า lobby: รายชื่อผู้เล่นที่เข้ามาแล้ว
    private void drawLobby(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 200));   // กล่องขาวโปร่งแสง
        g2.fillRoundRect(250, 60, 500, 480, 30, 30);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString("LOBBY", 435, 115);

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        int y = 150;
        for (int i = 0; i < Network.MAX_PLAYERS; i++) {
            boolean isMe = (i == myId);
            boolean online = network.friendLevel[i] >= 0;
            if (isMe || online) {
                g2.drawImage(icons[i], 300, y, 50, 50, null);
                String text = "Player " + (i + 1) + " (" + colors[i] + ")";
                if (i == 0) {
                    text = text + "  - HOST";
                }
                if (isMe) {
                    text = text + "  - YOU";
                }
                g2.drawString(text, 370, y + 33);
                y = y + 60;   // คนถัดไปเลื่อนลงมา 60
            }
        }

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        if (myId == 0) {
            g2.drawString("Press START when everyone is here", 340, 455);
        } else {
            g2.drawString("Waiting for host to start...", 385, 500);
        }
    }

    // วาดด่าน ไก่ และข้อความมุมซ้ายบน
    private void drawGame(Graphics2D g2) {
        level.draw(g2);

        // วาดไก่เพื่อนที่อยู่ด่านเดียวกัน (เข้าประตูแล้วไม่ต้องวาด)
        int playersHere = 1;   // นับตัวเราด้วย
        for (int i = 0; i < Network.MAX_PLAYERS; i++) {
            Player friend = friendsHere[i];
            if (friend != null) {
                playersHere++;
                if (network.friendAtDoor[i] == false) {
                    friend.draw(g2);
                }
            }
        }

        // วาดไก่เรา ยกเว้น เข้าประตูไปแล้ว หรือ ช่วงครึ่งแรกของการผ่านด่าน (ไก่อยู่ในประตู)
        boolean firstHalf = waitTime > halfWaitTime;
        boolean showPlayer = true;
        if (atDoor) {
            showPlayer = false;
        }
        if (firstHalf && dying == false) {
            showPlayer = false;
        }
        if (showPlayer) {
            player.draw(g2);
        }

        // จบเกมแล้ว levelNumber จะเป็น 8 แต่ให้โชว์แค่ Level 7
        int showLevel = levelNumber;
        if (showLevel > Level.LAST_LEVEL) {
            showLevel = Level.LAST_LEVEL;
        }
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Level " + showLevel + "   Deaths: " + deaths, 20, 30);

        if (network == null && levelNumber >= Level.FIRST_COOP_LEVEL && win == false) {
            g2.drawString("2 PLAYERS LEVEL - need a friend!", 20, 60);
        } else if (network != null && waitTime == 0 && win == false && atDoor) {
            g2.drawString("Waiting for friends at the door...", 20, 60);
        }
        if (network != null) {
            g2.drawString("You are player " + (myId + 1) + " (" + colors[myId] + ")   Players here: " + playersHere, 20, 90);
        }
    }

    // จอมืด/แดง ตอนเปลี่ยนฉาก
    private void drawChangeScene(Graphics2D g2) {
        int darkness = getDarkness();
        if (dying) {
            g2.setColor(new Color(200, 0, 0, darkness));   // ตาย = จอแดง
        } else {
            g2.setColor(new Color(0, 0, 0, darkness));     // ผ่านด่าน = จอดำ
        }
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // โชว์ข้อความเฉพาะครึ่งแรก (ออกจาก lobby ไม่ต้องโชว์)
        boolean firstHalf = waitTime > halfWaitTime;
        if (firstHalf && levelNumber > 0) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            if (dying && friendDied) {
                g2.drawString("FRIEND DIED", 310, 250);
            } else if (dying) {
                g2.drawString("YOU DIED", 350, 250);
            } else {
                g2.drawString("LEVEL CLEAR!", 290, 250);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // ภาพพิกเซลไม่เบลอตอนขยาย
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // วาดพื้นหลังก่อน ของอื่นจะได้ทับข้างบน
        if (background != null) {
            g2.drawImage(background, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        }

        if (levelNumber == 0) {
            drawLobby(g2);
        } else {
            drawGame(g2);
        }

        if (waitTime > 0) {
            drawChangeScene(g2);
        }

        if (win) {
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            g2.drawString("YOU WIN!", 360, 250);
        }
    }
}
