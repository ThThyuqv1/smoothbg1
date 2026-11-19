package dev.smoothbg;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Plain-text config: .minecraft/config/smoothbg.properties */
final class Config {
    /** สัดส่วนเวลาสูงสุดที่เธรดพื้นหลังได้ใช้ CPU (0.05 = 5%) */
    double dutyCycle = 0.10;
    /** FPS สูงสุดตอนหน้าต่างเกมไม่ได้โฟกัส (ขั้นต่ำ 10) */
    int unfocusedFps = 30;
    /** เปิดการจำกัด FPS ตอนไม่โฟกัสหรือไม่ */
    boolean throttleUnfocused = true;
    /** ถ้าไม่โฟกัสนานเกินกี่วินาที ให้เก็บกวาดหน่วยความจำ 1 ครั้ง (0 = ปิด) */
    int idleGcSeconds = 120;
    /** tick เฉลี่ยเกินกี่ ms ถือว่าเกมกำลังหนัก งานพื้นหลังจะหยุดรอ */
    double busyTickMs = 58.0;

    static Config load() {
        Config c = new Config();
        Path p = FabricLoader.getInstance().getConfigDir().resolve("smoothbg.properties");
        Properties pr = new Properties();
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                pr.load(r);
            } catch (IOException e) {
                SmoothBg.LOG.warn("Cannot read config, using defaults", e);
            }
        }
        c.dutyCycle = clamp(dbl(pr, "dutyCycle", c.dutyCycle), 0.01, 0.50);
        c.unfocusedFps = (int) clamp(dbl(pr, "unfocusedFps", c.unfocusedFps), 10, 260);
        c.throttleUnfocused = Boolean.parseBoolean(pr.getProperty("throttleUnfocused", "true"));
        c.idleGcSeconds = (int) clamp(dbl(pr, "idleGcSeconds", c.idleGcSeconds), 0, 86400);
        c.busyTickMs = clamp(dbl(pr, "busyTickMs", c.busyTickMs), 51, 200);

        Properties out = new Properties();
        out.setProperty("dutyCycle", Double.toString(c.dutyCycle));
        out.setProperty("unfocusedFps", Integer.toString(c.unfocusedFps));
        out.setProperty("throttleUnfocused", Boolean.toString(c.throttleUnfocused));
        out.setProperty("idleGcSeconds", Integer.toString(c.idleGcSeconds));
        out.setProperty("busyTickMs", Double.toString(c.busyTickMs));
        try (Writer w = Files.newBufferedWriter(p)) {
            out.store(w, "SmoothBG config");
        } catch (IOException ignored) {
        }
        return c;
    }

    private static double dbl(Properties p, String k, double d) {
        try {
            return Double.parseDouble(p.getProperty(k, Double.toString(d)).trim());
        } catch (NumberFormatException e) {
            return d;
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
