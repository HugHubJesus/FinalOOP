import javax.swing.*;

public class GameFrame extends JFrame {

    GamePaint panel;

    public GameFrame(Network network) {
        super("Cool Head Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        panel = new GamePaint(network);
        add(panel);
        pack();                  // ขนาดหน้าต่างพอดีกับ panel (1000 x 600)
        setResizable(false);     // ล็อกขนาดจอ ด่านจะได้ไม่เพี้ยน
        setLocationRelativeTo(null);
        setVisible(true);

        panel.requestFocusInWindow();    // ให้ panel รับปุ่มกด
        panel.start();                  // เริ่มลูปเกม
    }
}
