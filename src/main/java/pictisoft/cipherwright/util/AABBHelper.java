package pictisoft.cipherwright.util;

import net.minecraft.world.phys.AABB;

public final class AABBHelper
{

    public static AABB union(AABB a, AABB b)
    {
        if (a == null) return b;
        if (b == null) return a;
        return new AABB(
                Math.min(a.minX, b.minX),
                Math.min(a.minY, b.minY),
                Math.min(a.minZ, b.minZ),
                Math.max(a.maxX, b.maxX),
                Math.max(a.maxY, b.maxY),
                Math.max(a.maxZ, b.maxZ)
        );
    }

    public static AABB unionAll(Iterable<AABB> boxes)
    {
        AABB out = null;
        for (AABB box : boxes) out = union(out, box);
        return out;
    }
}