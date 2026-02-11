package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Library;
import io.github.nowipi.ffm.processor.pointer.Pointer;

@Library(value = "point.dll")
public interface PointLibrary {

    @Function(value = "point_add")
    Point pointAdd(Point a, Point to);

    @Function(value = "point_add_mut")
    void pointAddMut(Pointer<Point> a, Point to);
}
