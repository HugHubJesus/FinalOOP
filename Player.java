import java.awt.*;

// ไก่ (ทั้งตัวเรา และไก่เพื่อน)
public class Player extends GameObject {
    // ปุ่มที่กดค้างอยู่ (GamePaint เป็นคนเปลี่ยนค่า)
    public boolean left = false;
    public boolean right = false;
    public boolean jump = false;

    private double gravity = 0.4;     // ทุกเฟรม ความเร็วตกเพิ่มขึ้นเท่านี้
    private double jumpPower = -10;   // ติดลบ = พุ่งขึ้นข้างบน
    private int walkSpeed = 4;        // เดินเฟรมละกี่พิกเซล

    // รูปไก่แต่ละท่า
    private Image standLeft;
    private Image standRight;
    private Image walkLeft1;
    private Image walkLeft2;
    private Image walkRight1;
    private Image walkRight2;
    private Image jumpLeft;
    private Image jumpRight;

    private boolean facingRight = true;   // หันไปทางขวาอยู่มั้ย
    private int walkCounter = 0;          // นับเฟรมไว้สลับท่าเดิน

    private double fallSpeed = 0;         // ความเร็วตก (บวก = ตกลง, ลบ = ลอยขึ้น)
    private boolean onFloor = false;      // ยืนอยู่บนพื้นมั้ย

    // color = ชื่อโฟลเดอร์สีไก่ เช่น "white", "brown"
    public Player(int x, int y, String color) {
        super(x, y, 40, 40);
        String folder = "Image/Chicken/" + color + "/";
        standLeft = loadImage(folder + "stand_left.png");
        standRight = loadImage(folder + "stand_right.png");
        walkLeft1 = loadImage(folder + "walk_left_1.png");
        walkLeft2 = loadImage(folder + "walk_left_2.png");
        walkRight1 = loadImage(folder + "walk_right_1.png");
        walkRight2 = loadImage(folder + "walk_right_2.png");
        jumpLeft = loadImage(folder + "jump_left.png");
        jumpRight = loadImage(folder + "jump_right.png");
    }

    // เลือกรูปตามท่าทางตอนนี้
    private Image getPicture() {
        // ลอยอยู่กลางอากาศ = ท่ากระโดด
        if (onFloor == false) {
            if (facingRight) {
                return jumpRight;
            } else {
                return jumpLeft;
            }
        }

        // กดซ้ายหรือขวาอย่างใดอย่างหนึ่ง = ท่าเดิน
        // ทุกๆ 10 เฟรมสลับท่า: เฟรม 0-9 ท่าที่ 1, เฟรม 10-19 ท่าที่ 2, แล้ววนใหม่
        boolean walking = (left == true && right == false) || (right == true && left == false);
        if (walking) {
            boolean firstStep = (walkCounter % 20) < 10;
            if (facingRight) {
                if (firstStep) {
                    return walkRight1;
                } else {
                    return walkRight2;
                }
            } else {
                if (firstStep) {
                    return walkLeft1;
                } else {
                    return walkLeft2;
                }
            }
        }

        // ยืนเฉยๆ
        if (facingRight) {
            return standRight;
        } else {
            return standLeft;
        }
    }

    // ใช้กับไก่เพื่อน: ย้ายไปตำแหน่งที่ได้จากเน็ต แล้วดูจากการขยับว่าเดิน/กระโดดอยู่
    public void moveTo(int x, int y) {
        left = false;
        right = false;
        if (x < bounds.x) {
            left = true;
            facingRight = false;
        }
        if (x > bounds.x) {
            right = true;
            facingRight = true;
        }

        // ความสูงไม่เปลี่ยน = ยืนอยู่บนพื้น
        if (y == bounds.y) {
            onFloor = true;
        } else {
            onFloor = false;
        }

        walkCounter++;
        bounds.x = x;
        bounds.y = y;
    }

    // กลับไปจุดเกิด
    public void respawn(int x, int y) {
        bounds.x = x;
        bounds.y = y;
        fallSpeed = 0;
    }

    // ขยับไก่ 1 เฟรม (floors = พื้นในด่าน, floorCount = มีพื้นกี่แผ่น)
    public void update(GameFloor[] floors, int floorCount) {
        // เดินซ้าย/ขวา
        if (left) {
            bounds.x = bounds.x - walkSpeed;
        }
        if (right) {
            bounds.x = bounds.x + walkSpeed;
        }
        if (bounds.x < 0) {
            bounds.x = 0;   // กันเดินหลุดจอซ้าย
        }

        // หันหน้าตามทางที่เดิน
        if (left == true && right == false) {
            facingRight = false;
        }
        if (right == true && left == false) {
            facingRight = true;
        }
        walkCounter++;

        // กระโดดได้เฉพาะตอนยืนบนพื้น
        if (jump && onFloor) {
            fallSpeed = jumpPower;
        }

        // แรงโน้มถ่วง: ความเร็วตกเพิ่มขึ้นทุกเฟรม แล้วขยับไก่ลงตามความเร็ว
        int feetBefore = bounds.y + bounds.height;   // ตำแหน่งเท้าก่อนขยับ
        fallSpeed = fallSpeed + gravity;
        bounds.y = bounds.y + (int) fallSpeed;        // (int) = ตัดทศนิยมทิ้ง

        // เช็คว่าตกลงมาโดนพื้นแผ่นไหนมั้ย
        // ยืดเท้าลงไป 1 พิกเซล ให้ตอนยืนแตะพื้นพอดีนับว่ายืนอยู่ ไม่งั้นรูปจะกระพริบ
        Rectangle feet = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height + 1);
        onFloor = false;
        for (int i = 0; i < floorCount; i++) {
            GameFloor floor = floors[i];
            if (floor.isSolid() == false) {
                continue;   // สะพานที่ยังไม่โผล่ ยืนไม่ได้
            }
            Rectangle floorBox = floor.getBounds();

            // ต้องกำลังตกลง + ชนพื้น + ก่อนหน้านี้เท้ายังอยู่เหนือพื้น (กันโดนดูดขึ้นมาจากข้างล่าง)
            boolean fallingDown = fallSpeed > 0;
            boolean touchFloor = feet.intersects(floorBox);
            boolean wasAbove = feetBefore <= floorBox.y;
            if (fallingDown && touchFloor && wasAbove) {
                bounds.y = floorBox.y - bounds.height;   // วางไก่ไว้บนพื้นพอดี
                fallSpeed = 0;
                onFloor = true;
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        Image picture = getPicture();
        if (picture != null) {
            g.drawImage(picture, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else {
            g.setColor(Color.PINK);
            g.fill(bounds);
        }
    }
}
