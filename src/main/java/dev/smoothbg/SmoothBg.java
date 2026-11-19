package dev.smoothbg;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SmoothBg implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("SmoothBG");

    @Override
    public void onInitializeClient() {
        Config cfg = Config.load();
        FrameMonitor monitor = new FrameMonitor(cfg.busyTickMs);
        BudgetExecutor exec = new BudgetExecutor(cfg.dutyCycle, monitor::isBusy);
        UnfocusedGovernor governor = new UnfocusedGovernor(cfg, exec);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            monitor.onTick();
            governor.onTick(client);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            governor.restore(client);
            exec.shutdown();
        });

        LOG.info("SmoothBG ready (duty={}%, unfocusedFps={})",
                (int) (cfg.dutyCycle * 100), cfg.unfocusedFps);
    }
}
