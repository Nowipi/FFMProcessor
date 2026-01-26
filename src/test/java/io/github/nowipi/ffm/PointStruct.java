package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.annotations.Struct;

@Struct("Point")
interface PointStruct {
    float x();
    float y();
}
