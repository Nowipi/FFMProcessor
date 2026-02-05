package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Struct;

@Struct("Line")
interface LineStruct {
    Point a();
    Point b();
}
