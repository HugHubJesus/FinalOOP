import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

// แม่แบบของทุกอย่างในเกม (ไก่ พื้น หนาม ประตู ปุ่ม)
public abstract class GameObject {
    protected Rectangle bounds;   // ตำแหน่ง + ขนาด (x, y, กว้าง, สูง)

    public GameObject(int x, int y, int width, int height) {
        bounds = new Rectangle(x, y, width, height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    // กับดักแต่ละอันจะเขียนทับเมธอดนี้เอง ของที่อยู่เฉยๆ ไม่ต้องทำอะไร
    public void update(Player player) {
    }

    // ของแต่ละอย่างวาดตัวเองไม่เหมือนกัน
    public abstract void draw(Graphics2D g);

    // โหลดรูปจากไฟล์ เช่น loadImage("Image/Item/door.png")
    // ถ้าหาไม่เจอ ลองหาในโฟลเดอร์เดียวกับไฟล์ .class อีกที (เผื่อรันเกมจากโฟลเดอร์อื่น)
    public static BufferedImage loadImage(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return ImageIO.read(file);
            }
            java.net.URL url = GameObject.class.getResource("/" + path);
            if (url != null) {
                return ImageIO.read(url);
            }
            System.out.println("Image not found: " + path);
        } catch (Exception e) {
            System.out.println("Cannot load image: " + path);
        }
        return null;
    }
}
