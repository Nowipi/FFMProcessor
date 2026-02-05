package io.github.nowipi.ffm.processor;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class StructWriter {

    private final StructFileData fileData;

    public StructWriter(StructFileData fileData) {
        this.fileData = fileData;
    }


    public void createStructClass(Filer filer) throws IOException {
        JavaFileObject file = filer.createSourceFile(fileData.getPackageName() + "." + fileData.getClassName());
        writeStructClass(file);
    }

    private void writeStructClass(JavaFileObject file) {
        try (Writer writer = file.openWriter()) {
            writer.write("package " + fileData.getPackageName() + ";\n\n");
            writer.write("import java.lang.foreign.*;\nimport java.lang.invoke.MethodHandle;\nimport java.lang.invoke.VarHandle;\nimport static java.lang.foreign.ValueLayout.*;\n\n");
            writeClass(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeClass(Writer writer) throws IOException {

        writer.write("public class ");
        writer.write(fileData.getClassName());
        writer.write(" {\n");

        writeClassBody(writer);

        writer.write("}\n");

    }

    private void writeClassBody(Writer writer) throws IOException {

        writer.write("public static final StructLayout LAYOUT;\n");
        writeStaticOffsetDeclarations(writer);

        writeStaticInitialization(writer);


        writer.write("private final MemorySegment nativeSegment;\n");

        writeConstructors(writer);


        writeMemberGetters(writer);
        writeMemberSetters(writer);

        writer.write("MemorySegment getNativeSegment() {\nreturn nativeSegment;\n}");
    }

    private void writeStaticOffsetDeclarations(Writer writer) throws IOException {
        for (StructFileData.MemberData member : fileData.getMembers()) {
            writer.write("public static final long ");
            writer.write(member.offsetName());
            writer.write(";\n");
        }
    }

    private void writeStaticInitialization(Writer writer) throws IOException {
        writer.write("static {\n");
        writer.write("LAYOUT = MemoryLayout.structLayout(\n");
        List<StructFileData.MemberData> members = fileData.getMembers();
        for (int i = 0; i < members.size(); i++) {
            StructFileData.MemberData member = members.get(i);
            writer.write(member.layout());
            writer.write(".withName(\"");
            writer.write(member.name());

            if (i == members.size()-1) {
                writer.write("\")\n");
            } else {
                writer.write("\"),\n");
            }
        }
        writer.write(");\n");

        for (StructFileData.MemberData member : members) {
            writer.write(member.offsetName());
            writer.write(" = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement(\"");
            writer.write(member.name());
            writer.write("\"));\n");
        }

        writer.write("}\n");
    }

    private void writeConstructors(Writer writer) throws IOException {

        writer.write("public ");
        writer.write(fileData.getClassName());
        writer.write("(Arena arena) {\nthis(arena.allocate(LAYOUT));\n}\n");

        writer.write("private ");
        writer.write(fileData.getClassName());
        writer.write("(MemorySegment segment) {\nnativeSegment = segment;\n}\n");

        writer.write("public static ");
        writer.write(fileData.getClassName());
        writer.write(" from(MemorySegment nativeSegment) {\nreturn new ");
        writer.write(fileData.getClassName());
        writer.write("(nativeSegment);\n}\n");
    }

    private void writeMemberGetters(Writer writer) throws IOException {
        for (StructFileData.MemberData member : fileData.getMembers()) {

            if (member.isStruct()) {
                writer.write("public ");
                writer.write(member.javaTypeName());
                writer.write(" get");
                writer.write(capitalize(member.name()));
                writer.write("() {\nreturn ");
                writer.write(member.javaTypeName());
                writer.write(".from(nativeSegment.asSlice(");
                writer.write(member.offsetName());
                writer.write(", ");
                writer.write(member.layout());
                writer.write("));\n}\n");
            } else {
                writer.write("public ");
                writer.write(member.javaTypeName());
                writer.write(" get");
                writer.write(capitalize(member.name()));
                writer.write("() {\nreturn nativeSegment.get(");
                writer.write(member.layout());
                writer.write(", ");
                writer.write(member.offsetName());
                writer.write(");\n}\n");
            }
        }
    }

    private void writeMemberSetters(Writer writer) throws IOException {
        for (StructFileData.MemberData member : fileData.getMembers()) {
            if (member.isStruct()) {
                writer.write("public void set");
                writer.write(capitalize(member.name()));
                writer.write("(");
                writer.write(member.javaTypeName());
                writer.write(" ");
                writer.write(member.name());
                writer.write(") {\nnativeSegment.asSlice(");
                writer.write(member.offsetName());
                writer.write(", ");
                writer.write(member.layout());
                writer.write(").copyFrom(");
                writer.write(member.name());
                writer.write(".getNativeSegment());\n}\n");
            } else {
                writer.write("public void set");
                writer.write(capitalize(member.name()));
                writer.write("(");
                writer.write(member.javaTypeName());
                writer.write(" ");
                writer.write(member.name());
                writer.write(") {\nnativeSegment.set(");
                writer.write(member.layout());
                writer.write(", ");
                writer.write(member.offsetName());
                writer.write(", ");
                writer.write(member.name());
                writer.write(");\n}\n");
            }
        }
    }

    private static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
