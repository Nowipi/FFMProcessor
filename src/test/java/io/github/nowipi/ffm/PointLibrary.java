package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Library;
import io.github.nowipi.ffm.processor.annotations.Value;

@Library(value = "point.dll")
public interface PointLibrary {

    @Function(value = "point_add")
    void pointAdd(Point to, @Value Point value);

}
