package io.github.nowipi.ffm.info;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;

import static java.lang.foreign.ValueLayout.JAVA_FLOAT;

public class Point {

    public static final StructLayout LAYOUT;
    public static final long xOffset;
    public static final long yOffset;

    static {
        LAYOUT = MemoryLayout.structLayout(
                JAVA_FLOAT.withName("x"),
                JAVA_FLOAT.withName("y")
        );

        xOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("x"));
        yOffset = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("y"));
    }

    private final MemorySegment nativeSegment;
    private final Arena instanceArena;

    public Point() {
        this(Arena.ofAuto());
    }

    public Point(Arena arena) {
        this(arena, arena.allocate(LAYOUT));
    }

    private Point(Arena arena, MemorySegment nativeSegment) {
        instanceArena = arena;
        this.nativeSegment = nativeSegment;
    }

    public static Point from(MemorySegment nativeSegment) {
        return new Point(null, nativeSegment);
    }

    public float getX() {
        return nativeSegment.get(JAVA_FLOAT, xOffset);
    }

    public float getY() {
        return nativeSegment.get(JAVA_FLOAT, yOffset);
    }

    public void setX(float x) {
        nativeSegment.set(JAVA_FLOAT, xOffset, x);
    }

    public void setY(float y) {
        nativeSegment.set(JAVA_FLOAT, yOffset, y);
    }

    MemorySegment getNativeSegment() {
        return nativeSegment;
    }
}
