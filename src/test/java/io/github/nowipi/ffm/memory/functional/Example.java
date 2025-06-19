package io.github.nowipi.ffm.memory.functional;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

final class Example {
    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            test();
        }
    }

    private static void test() {
        try(var arena = Arena.ofShared()) {

            for (int i = 0; i < 1_000_000; i++) {
                MemorySegment p = Point.allocate(arena);
                Point.x(p, i * 0.5f);
                Point.y(p, i);
            }
        }
    }
}
