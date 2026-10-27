import java.awt.*;

// ด่าน: เก็บพื้น หนาม ปุ่ม และประตูของด่านนั้น
public class Level {
    public static final int LAST_LEVEL = 7;         // ด่านสุดท้าย
    public static final int FIRST_COOP_LEVEL = 6;   // ด่าน 6 ขึ้นไปต้องเล่น 2 คน

    private int groundY = 450;        // ขอบบนของพื้น
    private int groundHeight = 150;   // ความหนาของพื้น
    private int spikeY = 420;         // หนามสูง 30 วางบนพื้นพอดี (450 - 30)
    private int doorY = 390;          // ประตูสูง 60 วางบนพื้นพอดี (450 - 60)

    // ของในด่าน เก็บเป็น array + ตัวนับว่าใส่ไปแล้วกี่อัน
    public GameFloor[] floors = new GameFloor[10];
    public int floorCount = 0;
    private Spike[] spikes = new Spike[20];
    private int spikeCount = 0;
    private FloorButton[] buttons = new FloorButton[5];
    private int buttonCount = 0;
    private Door door;
    private GameFloor bridge = null;   // สะพานที่โผล่เมื่อมีคนเหยียบปุ่ม (มีแค่ด่าน 7)

    // สร้างด่านตามหมายเลข ตายเมื่อไหร่ก็ new Level ใหม่ กับดักจะกลับเป็นเหมือนเดิม
    public Level(int number) {
        if (number == 1) {
            // พื้นตรงกลางดูปกติ แต่เดินไปแล้วร่วง
            addFloor(new GameFloor(0, groundY, 400, groundHeight));
            addFloor(new FallingFloor(400, groundY, 100, groundHeight, 400));
            addFloor(new GameFloor(500, groundY, 500, groundHeight));
            door = new Door(920, doorY, 920);
        } else if (number == 2) {
            // กระโดดข้ามหนามแรก แล้วหนามใหม่โผล่ตรงที่จะลง
            addFloor(new GameFloor(0, groundY, 1000, groundHeight));
            addSpike(new Spike(300, spikeY, 0));
            addSpike(new Spike(330, spikeY, 0));
            addSpike(new Spike(470, spikeY, 300));
            addSpike(new Spike(500, spikeY, 300));
            door = new Door(920, doorY, 920);
        } else if (number == 3) {
            // ประตูหนี แล้วพื้นก่อนถึงประตูร่วง
            addFloor(new GameFloor(0, groundY, 700, groundHeight));
            addFloor(new FallingFloor(700, groundY, 100, groundHeight, 690));
            addFloor(new GameFloor(800, groundY, 200, groundHeight));
            door = new Door(500, doorY, 920);
        } else if (number == 4) {
            // ประตูอยู่ใกล้นิดเดียว แต่หนีไปสุดจอ ระหว่างทางมีหนามโผล่ใส่หน้า 2 รอบ
            addFloor(new GameFloor(0, groundY, 1000, groundHeight));
            addSpike(new Spike(600, spikeY, 540));
            addSpike(new Spike(860, spikeY, 780));
            door = new Door(200, doorY, 920);
        } else if (number == 5) {
            // พื้นร่วงเหมือนด่าน 1 ให้ตายใจ แต่พื้นก่อนถึงประตูก็ร่วงด้วย ต้องกระโดดไปลงพื้นตรงประตู
            addFloor(new GameFloor(0, groundY, 300, groundHeight));
            addFloor(new FallingFloor(300, groundY, 100, groundHeight, 300));
            addFloor(new GameFloor(400, groundY, 250, groundHeight));
            addFloor(new FallingFloor(650, groundY, 190, groundHeight, 750));
            addFloor(new GameFloor(840, groundY, 160, groundHeight));   // พื้นตรงประตู ไว้ยืนรอเพื่อน
            addSpike(new Spike(520, spikeY, 0));
            door = new Door(860, doorY, 860);
        } else if (number == 6) {
            // (2 คน) พรมหนามยาวเกินกระโดด คนนึงยืนกดปุ่มให้หนามหลบ อีกคนเดินข้ามไปกดปุ่มฝั่งโน้นให้
            addFloor(new GameFloor(0, groundY, 1000, groundHeight));
            // วางหนามเรียงกันตั้งแต่ x = 300 ถึง 700 ห่างกันอันละ 30 (เท่าความกว้างหนาม)
            for (int x = 300; x < 700; x = x + 30) {
                Spike spike = new Spike(x, spikeY, 0);
                spike.useButton = true;
                addSpike(spike);
            }
            addButton(new FloorButton(200, groundY - 10));
            addButton(new FloorButton(760, groundY - 10));
            addSpike(new Spike(850, spikeY, 800));   // หนามโผล่หน้าประตู
            door = new Door(920, doorY, 920);
        } else {
            // (2 คน) เหวกว้างเกินกระโดด ต้องกดปุ่มให้สะพานโผล่
            // ข้ามไปแล้วประตูหนีกลับไปฝั่งเริ่ม ต้องช่วยกันข้ามกลับอีกรอบ
            addFloor(new GameFloor(0, groundY, 300, groundHeight));
            bridge = new GameFloor(300, groundY, 250, 20);
            addFloor(bridge);
            addFloor(new GameFloor(550, groundY, 450, groundHeight));
            addButton(new FloorButton(150, groundY - 10));
            addButton(new FloorButton(620, groundY - 10));
            door = new Door(900, doorY, 70);
        }
    }

    private void addFloor(GameFloor floor) {
        floors[floorCount] = floor;
        floorCount++;
    }

    private void addSpike(Spike spike) {
        spikes[spikeCount] = spike;
        spikeCount++;
    }

    private void addButton(FloorButton button) {
        buttons[buttonCount] = button;
        buttonCount++;
    }

    // ให้กับดักชิ้นนี้เช็คไก่เราและไก่เพื่อนทุกตัว
    private void updateObject(GameObject object, Player player, Player[] friends) {
        object.update(player);
        for (int i = 0; i < friends.length; i++) {
            if (friends[i] != null) {
                object.update(friends[i]);
            }
        }
    }

    // เช็คกับดักทุกเฟรม (friends = เพื่อนที่อยู่ด่านเดียวกัน ช่องไหนเป็น null คือไม่มีคนนั้น)
    public void update(Player player, Player[] friends) {
        for (int i = 0; i < floorCount; i++) {
            updateObject(floors[i], player, friends);
        }
        for (int i = 0; i < spikeCount; i++) {
            updateObject(spikes[i], player, friends);
        }
        updateObject(door, player, friends);

        // มีปุ่มไหนถูกกดอยู่มั้ย
        boolean pressed = false;
        for (int i = 0; i < buttonCount; i++) {
            buttons[i].check(player, friends);
            if (buttons[i].isPressed()) {
                pressed = true;
            }
        }

        // มีคนกดปุ่ม = หนามที่ผูกกับปุ่มหลบลง
        for (int i = 0; i < spikeCount; i++) {
            if (spikes[i].useButton) {
                if (pressed) {
                    spikes[i].setActive(false);
                } else {
                    spikes[i].setActive(true);
                }
            }
        }

        // มีคนกดปุ่ม = สะพานโผล่
        if (bridge != null) {
            bridge.setSolid(pressed);
        }
    }

    // ตกจอ หรือ โดนหนาม = ตาย
    public boolean isDead(Player player) {
        if (player.getBounds().y > GamePaint.SCREEN_HEIGHT) {
            return true;
        }
        for (int i = 0; i < spikeCount; i++) {
            Spike spike = spikes[i];
            if (spike.isActive() && player.getBounds().intersects(spike.getHitBox())) {
                return true;
            }
        }
        return false;
    }

    // ชนประตู = ผ่านด่าน
    public boolean isWin(Player player) {
        return player.getBounds().intersects(door.getBounds());
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < floorCount; i++) {
            floors[i].draw(g);
        }
        for (int i = 0; i < spikeCount; i++) {
            spikes[i].draw(g);
        }
        for (int i = 0; i < buttonCount; i++) {
            buttons[i].draw(g);
        }
        door.draw(g);
    }
}
