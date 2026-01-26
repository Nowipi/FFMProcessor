package io.github.nowipi.ffm;

import org.junit.jupiter.api.*;

import java.lang.foreign.Arena;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PointTests {

    static PointLibrary pointLib;

    @BeforeAll
    static void beforeAll() {
        pointLib = new PointLibraryImpl();
    }

    @AfterEach
    void tearDown() {
        arena.close();
    }

    Arena arena;
    Point a;
    Point b;

    @BeforeEach
    void setUp() {
        arena = Arena.ofConfined();

        a = new Point(arena);
        a.setX(10);
        a.setY(5);

        b = new Point(arena);
        b.setX(1);
        b.setY(2);
    }

    @Test
    void addPointsTest() {
        pointLib.pointAdd(a, b);
        assertEquals(11, a.getX());
        assertEquals(7, a.getY());
    }
}
