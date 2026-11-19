package dev.smoothbg;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BooleanSupplier;

/**
 * เธรดพื้นหลังเดียว ลำดับความสำคัญต่ำสุด ทำงานแบบ duty cycle:
 * ทำงานนาน W แล้วพัก W * (1/duty - 1) เพื่อให้ใช้ CPU ไม่เกิน duty
 * และหยุดรอทั้งหมดเมื่อเธรดหลักกำลังหนัก
 */
final class BudgetExecutor {
    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final double restFactor;
    private final BooleanSupplier busy;
    private final Thread worker;
    private volatile boolean running = true;

    BudgetExecutor(double duty, BooleanSupplier busy) {
        this.restFactor = (1.0 / duty) - 1.0;
        this.busy = busy;
        this.worker = new Thread(this::loop, "SmoothBG-Worker");
        this.worker.setDaemon(true);
        this.worker.setPriority(Thread.MIN_PRIORITY);
        this.worker.start();
    }

    void submit(Runnable task) {
        if (running) queue.offer(task);
    }

    void shutdown() {
        running = false;
        worker.interrupt();
    }

    private void loop() {
        try {
            while (running) {
                Runnable task = queue.take();
                // ถอยหลบเมื่อเกมหนัก (เช็กทุก 25 ms)
                while (running && busy.getAsBoolean()) {
                    Thread.sleep(25);
                }
                long t0 = System.nanoTime();
                try {
                    task.run();
                } catch (Throwable t) {
                    SmoothBg.LOG.warn("Background task failed", t);
                }
                long workNs = System.nanoTime() - t0;
                long restMs = (long) (workNs * restFactor / 1_000_000.0);
                if (restMs > 0) Thread.sleep(Math.min(restMs, 2000));
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
