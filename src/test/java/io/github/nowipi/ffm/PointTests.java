package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.pointer.Pointer;
import io.github.nowipi.ffm.processor.pointer.StructPointer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        a = pointLib.pointNew(10, 5).get(0);

        b = pointLib.pointNew(1, 2).get(0);
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

    @Test
    void addPointArrayTest() {
        PointArray array = pointLib.pointArrayNew(10).get(0);
        long count = array.getCount();
        Pointer<Point> data = array.getData(); //encouraged to eliminate creating the pointer over and over again
        for (int i = 0; i < count; i++) {
            Point p = data.getAtIndex(i);
            float x = p.getX();
            float y = p.getY();
            assertEquals(x, i);
            assertEquals(y, i);
        }
        assertTrue(true);
    }
}
