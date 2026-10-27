import java.awt.*;

// ประตูทางออก เดินชนแล้วผ่านด่าน
// กับดัก: พอผู้เล่นเดินเข้าใกล้ ประตูจะหนีไปอยู่ที่ runToX (ถ้า runToX = x เดิม ก็คือประตูไม่หนี)
public class Door extends GameObject {
    private Image picture;
    private int runToX;

    public Door(int x, int y, int runToX) {
        super(x, y, 40, 60);
        this.runToX = runToX;
        picture = loadImage("Image/Item/door.png");
    }

    @Override
    public void update(Player player) {
        // หาระยะห่างระหว่างไก่กับประตู (ให้เป็นเลขบวกเสมอ ไม่ว่าไก่จะอยู่ซ้ายหรือขวา)
        int distance = player.getBounds().x - bounds.x;
        if (distance < 0) {
            distance = -distance;
        }

        // ไก่เข้าใกล้กว่า 120 พิกเซล ประตูหนี
        if (distance < 120) {
            bounds.x = runToX;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (picture != null) {
            g.drawImage(picture, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else {
            // โหลดรูปไม่ได้ ก็วาดสี่เหลี่ยมสีเขียวแทน
            g.setColor(new Color(0, 150, 0));
            g.fill(bounds);
        }
    }
}
