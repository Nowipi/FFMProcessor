package io.github.nowipi.ffm;


import io.github.nowipi.ffm.processor.pointer.BytePointer;
import io.github.nowipi.ffm.processor.pointer.Pointer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import static io.github.nowipi.ffm.processor.pointer.Pointer.NULL;

final class LibCTest {

    @Test
    public void strlenTest() {
        LibC libC = new LibCImpl();
        try(Arena arena = Arena.ofConfined()) {
            String str = "Hello World!";
            int l = libC.strlen(new BytePointer(arena, str.getBytes(StandardCharsets.US_ASCII)));
            Assertions.assertEquals(str.length(), l);
        }
    }

    @Test
    public void messageBoxTest() {
        User32 user32 = new User32Impl();
        try(var arena = Arena.ofConfined()) {
            var text = new BytePointer(arena, "Hello World!".getBytes(StandardCharsets.UTF_16LE));
            var title = new BytePointer(arena, "Hello Title".getBytes(StandardCharsets.UTF_16LE));
            user32.messageBox(NULL, text, title, 0x00000003L);
        }
    }

    @Test
    public void writeTest() {
        LibC libC = new LibCImpl();
        try(var arena = Arena.ofConfined()) {
            Pointer<Void> file = libC.fopen(
                    new BytePointer(arena, "file-does-not-exist".getBytes(StandardCharsets.US_ASCII)),
                    new BytePointer(arena, "r".getBytes(StandardCharsets.US_ASCII)));
            if (file.getAddress() == 0) {
                Pointer<Byte> message = libC.strerror(libC.errno());
                int messageLength = libC.strlen(message);
                System.out.println(BytePointer.toString(message, messageLength));
            } else {
                System.out.println("no error has occurred");
            }
        }
    }
}
