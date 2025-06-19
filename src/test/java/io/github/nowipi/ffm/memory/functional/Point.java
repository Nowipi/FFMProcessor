package io.github.nowipi.ffm.memory.functional;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

public final class Point {

    /*
    struct point {
        float x;
        float y;
    }
     */

    private static final StructLayout layout;

    private static final VarHandle xHandle;
    private static final VarHandle yHandle;

    static {
        layout = MemoryLayout.structLayout(
                JAVA_FLOAT.withName("x"),
                JAVA_FLOAT.withName("y")
        ).withName("point");
        xHandle = layout.varHandle(PathElement.groupElement("x"));
        yHandle = layout.varHandle(PathElement.groupElement("y"));
    }


    private Point() {
    }

    public static MemorySegment allocate(SegmentAllocator allocator) {

        return allocator.allocate(layout);
    }

    public static float x(MemorySegment point) {
        return (float) xHandle.get(point, 0);
    }

    public static float y(MemorySegment point) {
        return (float) yHandle.get(point, 0);
    }

    public static void x(MemorySegment point, float x) {
        xHandle.set(point, 0, x);
    }

    public static void y(MemorySegment point, float y) {
        yHandle.set(point, 0, y);
    }

}
