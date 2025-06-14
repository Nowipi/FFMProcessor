package io.github.nowipi.ffm;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

final class LibCTest {

    @Test
    public void strlenTest() {
        LibC libC = new LibCImpl();
        try(var arena = Arena.ofConfined()) {
            String str = "Hello World!";
            var format = arena.allocateFrom(str + "\0", StandardCharsets.US_ASCII);
            int l = libC.strlen(format);
            Assertions.assertEquals(str.length(), l);
        }
    }

    @Test
    public void messageBoxTest() {
        User32 user32 = new User32Impl();
        try(var arena = Arena.ofConfined()) {
            var text = arena.allocateFrom("Hello World!", StandardCharsets.UTF_16LE);
            var title = arena.allocateFrom("Hello Title", StandardCharsets.UTF_16LE);
            user32.messageBox(MemorySegment.NULL, text, title, 0x00000003L);
        }
    }

    @Test
    public void writeTest() {
        LibC libC = new LibCImpl();
        try(var arena = Arena.ofConfined()) {
            MemorySegment file = libC.fopen(arena.allocateFrom("file-does-not-exist"), arena.allocateFrom("r"));
            if (file.address() == 0) {
                MemorySegment message = libC.strerror(libC.errno());
                int messageLength = libC.strlen(message);
                System.out.println(message.reinterpret(messageLength + 1).getString(0));
            } else {
                System.out.println("no error has occurred");
            }
        }
    }
}
