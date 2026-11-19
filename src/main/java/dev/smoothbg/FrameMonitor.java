package dev.smoothbg;

/**
 * วัดแรงกดดันของเธรดหลักจากระยะห่างระหว่าง client tick (ปกติ 50 ms)
 * ใช้ค่าเฉลี่ยเคลื่อนที่แบบ exponential เพื่อไม่ให้ไวต่อสไปก์เดี่ยว
 */
final class FrameMonitor {
    private final double busyMs;
    private long last;
    private volatile double emaMs = 50.0;

    FrameMonitor(double busyMs) {
        this.busyMs = busyMs;
    }

    /** เรียกจากเธรดหลักปลาย tick */
    void onTick() {
        long now = System.nanoTime();
        if (last != 0) {
            double ms = (now - last) / 1_000_000.0;
            // ตัด outlier (เช่น เปิดเมนู/โหลดโลก) ไม่ให้ดันค่าเฉลี่ยสูงเกินจริง
            ms = Math.min(ms, 500.0);
            emaMs = emaMs * 0.85 + ms * 0.15;
        }
        last = now;
    }

    /** เรียกจากเธรดไหนก็ได้ */
    boolean isBusy() {
        return emaMs > busyMs;
    }
}
