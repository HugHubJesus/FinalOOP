import java.awt.*;

// หนาม โดนแล้วตาย
public class Spike extends GameObject {
    private Image picture;
    private int triggerX;          // ผู้เล่นเดินเลย x นี้เมื่อไหร่ หนามโผล่ (0 = โผล่ตั้งแต่แรก)
    private boolean active;        // false = ซ่อนอยู่ ไม่วาด ไม่ฆ่า
    public boolean useButton = false;   // true = หนามนี้หลบลงเมื่อมีคนเหยียบปุ่ม (ด่าน 2 คน)

    // หนามขนาด 30 x 30
    public Spike(int x, int y, int triggerX) {
        super(x, y, 30, 30);
        this.triggerX = triggerX;
        if (triggerX == 0) {
            active = true;
        } else {
            active = false;
        }
        picture = loadImage("Image/Item/spike.png");
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // หนามเป็นสามเหลี่ยม เลยให้จุดชนเล็กกว่ารูปนิดหน่อย จะได้ไม่ตายตอนแค่เฉียดมุม
    // หดซ้าย 8 หดขวา 8 และตัดยอดแหลมด้านบนออก 10
    public Rectangle getHitBox() {
        int x = bounds.x + 8;
        int y = bounds.y + 10;
        int width = bounds.width - 16;
        int height = bounds.height - 10;
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void update(Player player) {
        if (active == false && player.getBounds().x > triggerX) {
            active = true;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (active == false) {
            return;
        }
        if (picture != null) {
            g.drawImage(picture, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else {
            // โหลดรูปไม่ได้ ก็วาดสามเหลี่ยมแทน (มุมซ้ายล่าง, ยอดตรงกลาง, มุมขวาล่าง)
            g.setColor(Color.DARK_GRAY);
            int left = bounds.x;
            int middle = bounds.x + bounds.width / 2;
            int right = bounds.x + bounds.width;
            int top = bounds.y;
            int bottom = bounds.y + bounds.height;
            int[] xPoints = {left, middle, right};
            int[] yPoints = {bottom, top, bottom};
            g.fillPolygon(xPoints, yPoints, 3);
        }
    }
}
