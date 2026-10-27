import java.awt.*;

// ปุ่มบนพื้น (ด่านเล่น 2 คน) มีคนยืนทับอยู่ = ปุ่มถูกกด
public class FloorButton extends GameObject {
    private Image upPicture;     // ปุ่มแดง ยังไม่มีคนเหยียบ
    private Image downPicture;   // ปุ่มเขียว มีคนเหยียบอยู่
    private boolean pressed = false;

    public FloorButton(int x, int y) {
        super(x, y, 40, 10);
        upPicture = loadImage("Image/Item/button_up.png");
        downPicture = loadImage("Image/Item/button_down.png");
    }

    public boolean isPressed() {
        return pressed;
    }

    // เช็คว่าไก่ตัวเรา หรือไก่เพื่อนคนไหน ยืนทับปุ่มอยู่มั้ย
    // friends = ไก่เพื่อนที่อยู่ด่านเดียวกัน (ช่องไหนเป็น null คือไม่มีเพื่อนคนนั้น)
    public void check(Player player, Player[] friends) {
        pressed = false;
        if (player.getBounds().intersects(bounds)) {
            pressed = true;
        }
        for (int i = 0; i < friends.length; i++) {
            if (friends[i] != null && friends[i].getBounds().intersects(bounds)) {
                pressed = true;
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        Image picture = upPicture;
        if (pressed) {
            picture = downPicture;
        }

        if (picture != null) {
            g.drawImage(picture, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else {
            // โหลดรูปไม่ได้ ก็วาดสี่เหลี่ยมแทน
            if (pressed) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.RED);
            }
            g.fill(bounds);
        }
    }
}
