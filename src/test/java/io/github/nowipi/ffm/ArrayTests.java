package io.github.nowipi.ffm;

import io.github.nowipi.ffm.processor.pointer.IntegerPointer;
import io.github.nowipi.ffm.processor.pointer.Pointer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayTests {

    static ArrayLibrary arrayLib;

    @BeforeAll
    static void beforeAll() {
        arrayLib = new ArrayLibraryImpl();
    }

    @Test
    void creationTest() {
        long countInput = 10;
        Pointer<Array> array = arrayLib.arrayNew(countInput);
        assertEquals(countInput, array.get(0).getCount());
    }

    @Test
    void setTest() {
        long countInput = 10;
        Pointer<Array> arrayPtr = arrayLib.arrayNew(countInput);
        Array array = arrayPtr.get(0);
        Pointer<Integer> data = array.getData();
        for (int i = 0; i < array.getCount(); i++) {
            data.setAtIndex(i, i);
            assertEquals(data.getAtIndex(i), i);
        }
    }

}
