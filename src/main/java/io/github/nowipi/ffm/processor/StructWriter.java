package io.github.nowipi.ffm.processor;

import javax.annotation.processing.Filer;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
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
        for (StructFileData.StructMember member : fileData.getMembers()) {
            writer.write("public static final long ");
            writer.write(member.getOffsetName());
            writer.write(";\n");
        }
    }

    private void writeStaticInitialization(Writer writer) throws IOException {
        writer.write("static {\n");
        writer.write("LAYOUT = ");
        if (fileData.isPadded()) {
            writer.write("io.github.nowipi.ffm.processor.Padding.createPaddedLayout(\n");
        } else {
            writer.write("MemoryLayout.structLayout(\n");
        }
        List<StructFileData.StructMember> members = fileData.getMembers();

        for (int i = 0; i < members.size(); i++) {
            StructFileData.StructMember member = members.get(i);
            writer.write(member.getLayout());
            writer.write(".withName(\"");
            writer.write(member.getName());
            writer.write("\")");

            if (i != members.size()-1) {
                writer.write(",");

            }
            writer.write("\n");

        }
        writer.write(");\n");

        for (StructFileData.StructMember member : members) {
            writer.write(member.getOffsetName());
            writer.write(" = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement(\"");
            writer.write(member.getName());
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
        for (StructFileData.StructMember member : fileData.getMembers()) {

            if (member instanceof StructFileData.StructValueMember valueMember) {
                writer.write("public ");
                writer.write(valueMember.getTypeName());
                writer.write(" get");
                writer.write(capitalize(member.getName()));
                writer.write("() {\nreturn ");
                writer.write(valueMember.getTypeName());
                writer.write(".from(nativeSegment.asSlice(");
                writer.write(member.getOffsetName());
                writer.write(", ");
                writer.write(member.getLayout());
                writer.write("));\n}\n");
            } else {
                writer.write("public ");
                writer.write(member.getTypeName());
                writer.write(" get");
                writer.write(capitalize(member.getName()));
                writer.write("() {\nreturn ");
                if (member instanceof StructFileData.PointerMember pointerMember) {
                    writer.write("new ");
                    writer.write(pointerMember.getPointerClassImplementation());
                    if (pointerMember.isPointsToStruct()) {
                        writer.write("<>");
                    }
                    writer.write("(");
                    if (pointerMember.isPointsToStruct()) {
                        writer.write(pointerMember.getPointsToTypeName());
                        writer.write("::getNativeSegment, ");
                        writer.write(pointerMember.getPointsToTypeName());
                        writer.write("::from, ");
                    }

                }
                writer.write("nativeSegment.get(");
                writer.write(member.getLayout());
                if (member instanceof StructFileData.PointerMember pointerMember) {
                    writer.write(".withTargetLayout(");
                    writer.write(pointerMember.getPointsToLayout());
                    writer.write(")");
                }
                writer.write(", ");
                writer.write(member.getOffsetName());
                writer.write(")");

                if (member instanceof StructFileData.PointerMember pointerMember && pointerMember.getPointsToLayout().contains(".LAYOUT")) {
                    writer.write(", ");
                    writer.write(pointerMember.getPointsToLayout());
                }

                if (member instanceof StructFileData.PointerMember) {
                    writer.write(")");
                }
                writer.write(";\n}\n");
            }
        }
    }

    private void writeMemberSetters(Writer writer) throws IOException {
        for (StructFileData.StructMember member : fileData.getMembers()) {
            if (member instanceof StructFileData.StructValueMember) {
                writer.write("public void set");
                writer.write(capitalize(member.getName()));
                writer.write("(");
                writer.write(member.getTypeName());
                writer.write(" ");
                writer.write(member.getName());
                writer.write(") {\nnativeSegment.asSlice(");
                writer.write(member.getOffsetName());
                writer.write(", ");
                writer.write(member.getLayout());
                writer.write(").copyFrom(");
                writer.write(member.getName());
                writer.write(".getNativeSegment());\n}\n");
            } else {
                writer.write("public void set");
                writer.write(capitalize(member.getName()));
                writer.write("(");
                writer.write(member.getTypeName());
                writer.write(" ");
                writer.write(member.getName());
                writer.write(") {\nnativeSegment.set(");
                writer.write(member.getLayout());
                writer.write(", ");
                writer.write(member.getOffsetName());
                writer.write(", ");
                writer.write(member.getName());
                if (member instanceof StructFileData.PointerMember) {
                    writer.write(".getNativeSegment()");
                }
                writer.write(");\n}\n");
            }
        }
    }

    private static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
