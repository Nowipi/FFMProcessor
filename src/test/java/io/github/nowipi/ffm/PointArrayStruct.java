package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Struct;
import io.github.nowipi.ffm.processor.pointer.Pointer;

@Struct("PointArray")
interface PointArrayStruct {
    Pointer<Point> data();
    long count();
}
