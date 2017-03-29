package com.goldmansachs.kata2go.tools

import com.goldmansachs.kata2go.tools.processor.KataProcessor
import com.goldmansachs.kata2go.tools.utils.TarGz
import com.google.common.io.Resources
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path

class TestKataProcessor {

    private static final Path TEMP_DIR = Files.createTempDirectory(null);
    private static final Path OUT_TEMP_DIR = Files.createTempDirectory(null);

    @Test
    public void splitKataIntoExercises()
    {
        def inputTar = new File(Resources.getResource("hello-world-kata.tar.gz").toURI())
        def outputTar = new KataProcessor(TEMP_DIR).process(inputTar.toPath())

        TarGz.decompress(outputTar, OUT_TEMP_DIR)

        def ex1File = OUT_TEMP_DIR.resolve("src").resolve("HelloWorldKata_ex1.java")
        def ex1Src = readFile(ex1File)
        def ex1ExpectedSrc = """package com.goo;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Test;

public class HelloWorldKata_ex1 {
  @Test
  public void ex1() {

           // assertEquals("hello",  ...);
        }
}
"""
        assert ex1ExpectedSrc == ex1Src.replaceAll("\r", "")

        def ex2File = OUT_TEMP_DIR.resolve("src").resolve("HelloWorldKata_ex2.java")
        def ex2Src = readFile(ex2File)
        def ex2ExpectedSrc = """package com.goo;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Test;

public class HelloWorldKata_ex2 {
  @Test
  public void ex2() {

            // assertEquals("world", ...);
        }
}
"""
        assert ex2ExpectedSrc == ex2Src


    }

    private String readFile(Path path)
    {
        return new String(Files.readAllBytes(path))
    }
}
