package rift;

/** Plain statics so common code can feed the client-only sky/HUD effects. */
public final class ClientRiftState {
    public static float target = 0f;
    public static float current = 0f;
    public static int ttl = 0;

    private ClientRiftState() {}

    public static void ping(float s) {
        if (ttl == 0) target = 0f;
        target = Math.max(target, s);
        ttl = 3;
    }

    public static void tick() {
        if (ttl > 0) ttl--; else target = 0f;
        current += (target - current) * 0.08f;
        if (current < 0.001f) current = 0f;
    }
}
