package io.github.nowipi.ffm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PointTests {

    static PointLibrary pointLib;

    @BeforeAll
    static void beforeAll() {
        pointLib = new PointLibraryImpl();
    }

    Point a;
    Point b;

    @BeforeEach
    void setUp() {
        a = new Point();
        a.setX(10);
        a.setY(5);

        b = new Point();
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
