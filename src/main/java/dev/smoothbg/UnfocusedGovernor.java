package dev.smoothbg;

import net.minecraft.client.Minecraft;

/**
 * ตอน alt-tab: ลด FPS ชั่วคราว (คืนค่าเดิมทันทีที่กลับมา)
 * และเก็บกวาดหน่วยความจำ 1 ครั้งหลังไม่โฟกัสนานพอ
 * ไม่แตะค่ากราฟิก, particle หรือการเรนเดอร์ตอนเล่นปกติเลย
 */
final class UnfocusedGovernor {
    private final Config cfg;
    private final BudgetExecutor exec;

    private boolean wasActive = true;
    private boolean limited = false;
    private int savedFps;
    private long unfocusedSince;
    private boolean gcDone;

    UnfocusedGovernor(Config cfg, BudgetExecutor exec) {
        this.cfg = cfg;
        this.exec = exec;
    }

    void onTick(Minecraft mc) {
        boolean active = mc.isWindowActive();

        if (!active) {
            long now = System.nanoTime();
            if (wasActive) {
                unfocusedSince = now;
                gcDone = false;
                if (cfg.throttleUnfocused) {
                    int cur = mc.options.framerateLimit().get();
                    if (cur > cfg.unfocusedFps) {
                        savedFps = cur;
                        mc.options.framerateLimit().set(cfg.unfocusedFps);
                        limited = true;
                    }
                }
            } else if (cfg.idleGcSeconds > 0 && !gcDone
                    && (now - unfocusedSince) / 1_000_000_000L >= cfg.idleGcSeconds) {
                gcDone = true;
                exec.submit(System::gc);
            }
        } else if (!wasActive) {
            restore(mc);
        }
        wasActive = active;
    }

    void restore(Minecraft mc) {
        if (limited) {
            mc.options.framerateLimit().set(savedFps);
            limited = false;
        }
    }
}
