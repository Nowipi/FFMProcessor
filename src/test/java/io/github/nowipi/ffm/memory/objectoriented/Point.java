package io.github.nowipi.ffm.memory.objectoriented;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_FLOAT;

public final class Point {
    private static final StructLayout layout;

    private static final VarHandle xHandle;
    private static final VarHandle yHandle;

    static {
        layout = MemoryLayout.structLayout(
                JAVA_FLOAT.withName("x"),
                JAVA_FLOAT.withName("y")
        ).withName("point");
        xHandle = layout.varHandle(MemoryLayout.PathElement.groupElement("x"));
        yHandle = layout.varHandle(MemoryLayout.PathElement.groupElement("y"));
    }


    private final MemorySegment nativeSegment;
    public Point(SegmentAllocator allocator) {
        nativeSegment = allocator.allocate(layout);
    }

    public float x() {
        return (float) xHandle.get(nativeSegment, 0);
    }

    public float y() {
        return (float) yHandle.get(nativeSegment, 0);
    }

    public void x(float x) {
        xHandle.set(nativeSegment, 0, x);
    }

    public void y(float y) {
        yHandle.set(nativeSegment, 0, y);
    }

}
