package io.github.nowipi.ffm.memory.objectoriented;

import java.lang.foreign.Arena;

final class Example {

    public static void main(String[] args) {

        for (int i = 0; i < 100; i++) {
            test();
        }

    }

    private static void test() {
        try(var arena = Arena.ofConfined()) {

            for (int i = 0; i < 1_000_000; i++) {
                Point p = new Point(arena);
                p.x(i * 0.5f);
                p.y(i);
            }
        }
    }

}
