import java.awt.*;
import java.awt.image.BufferedImage;

// พื้นธรรมดา
public class GameFloor extends GameObject {
    private Image picture;
    private boolean solid = true;   // false = สะพานที่ยังไม่โผล่ เหยียบไม่ได้ และไม่วาด

    public GameFloor(int x, int y, int width, int height) {
        super(x, y, width, height);

        // รูปดินกว้างเท่าจอ ตัดเอาเฉพาะส่วนที่ตรงกับพื้นแผ่นนี้มาใช้ ลายดินจะได้ไม่ยืดเบี้ยว
        BufferedImage ground = loadImage("Image/Map/ground.png");
        if (ground != null) {
            picture = ground.getSubimage(x, 0, width, height);
        }
    }

    public boolean isSolid() {
        return solid;
    }

    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    @Override
    public void draw(Graphics2D g) {
        if (solid == false) {
            return;
        }
        if (picture != null) {
            g.drawImage(picture, bounds.x, bounds.y, null);
        } else {
            // โหลดรูปไม่ได้ ก็ใช้สีน้ำตาลแทน
            g.setColor(new Color(139, 69, 19));
            g.fill(bounds);
        }
    }
}
