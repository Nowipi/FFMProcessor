package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.pointer.StructPointer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Point newPoint = pointLib.pointAdd(a, b);
        assertEquals(11, newPoint.getX());
        assertEquals(7, newPoint.getY());
    }

    @Test
    void addPointsTestMut() {
        pointLib.pointAddMut(StructPointer.from(Point::getNativeSegment, Point::from, a, Point.LAYOUT), b);
        assertEquals(11, a.getX());
        assertEquals(7, a.getY());
    }
}
