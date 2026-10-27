// กับดัก: พื้นที่ดูเหมือนพื้นธรรมดา แต่พอผู้เล่นเดินมาถึง triggerX พื้นจะร่วงลงไป
public class FallingFloor extends GameFloor {
    private int triggerX;
    private boolean falling = false;

    public FallingFloor(int x, int y, int width, int height, int triggerX) {
        super(x, y, width, height);
        this.triggerX = triggerX;
    }

    @Override
    public void update(Player player) {
        // ขอบขวาของไก่ = x + ความกว้าง
        int playerRight = player.getBounds().x + player.getBounds().width;
        if (playerRight > triggerX) {
            falling = true;
        }

        // ร่วงลงทีละ 8 พิกเซล
        if (falling) {
            bounds.y = bounds.y + 8;
        }
    }
}
