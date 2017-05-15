/*
 Copyright 2017 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */
package com.goldmansachs.kata2go.executor.runne

import com.goldmansachs.kata2go.executor.compiler.CompilationResult
import com.goldmansachs.kata2go.executor.compiler.KataCompiler
import com.goldmansachs.kata2go.executor.runner.KataRunner
import com.goldmansachs.kata2go.executor.runner.StdOutStdErrCapturingKataRunner
import org.eclipse.collections.impl.factory.Maps
import org.junit.Test

class TestKataRunner {

    @Test
    public void passingTest() {
        String testSource = """
package com.simplekata;

import org.junit.Test;
import static org.junit.Assert.*;

public class PassingTest
{
   @Test
   public void one()
   {
        assertEquals(1,1);
   }
}
        """

        def sources = Maps.immutable.of("PassingTest.java", testSource)
        def compileResult = new KataCompiler().compile(sources);

        def kataResult = StdOutStdErrCapturingKataRunner
                .run(compileResult.compiledClasses(), "com.simplekata.PassingTest")

        assert kataResult.junitResult().wasSuccessful()
        assert kataResult.junitResult().errorCount() == 0
        assert kataResult.junitResult().failureCount() == 0;
        assert kataResult.stdOut().contains("OK (1 test)")
       }


    @Test
    public void failingTest() {
        String testSource = """
package com.simplekata;

import org.junit.Test;
import static org.junit.Assert.*;

public class FailingTest
{
   @Test
   public void one()
   {
        System.err.println(123456);
        assertEquals(1,2);
   }
}
        """

        def sources = Maps.immutable.of("FailingTest.java", testSource)
        def compilationResult = new KataCompiler().compile(sources);

        def kataResult = StdOutStdErrCapturingKataRunner
                .run(compilationResult.compiledClasses(), "com.simplekata.FailingTest")

        assert !kataResult.junitResult().wasSuccessful()
        assert kataResult.junitResult().errorCount() == 1
        assert kataResult.junitResult().failureCount() == 0;

        assert kataResult.stdOut().contains("""There was 1 error:
1) one(com.simplekata.FailingTest)java.lang.AssertionError: expected:<1> but was:<2>
\tat org.junit.Assert.fail(Assert.java:88)
\tat org.junit.Assert.failNotEquals(Assert.java:834)
\tat org.junit.Assert.assertEquals(Assert.java:645)
\tat org.junit.Assert.assertEquals(Assert.java:631)
""")
    }

    @Test
    public void testWritesToStdoutAndStdErr() {
        String testSource = """
package com.simplekata;

import org.junit.Test;
import static org.junit.Assert.*;

public class NoisyTest
{
   @Test
   public void one()
   {
        System.out.println("splitKataIntoExercises");
        System.err.println("bar");
   }
}
        """

        def sources = Maps.immutable.of("NoisyTest.java", testSource)
        CompilationResult compilationResult = new KataCompiler().compile(sources);

        def result = StdOutStdErrCapturingKataRunner
                .run(compilationResult.compiledClasses(), "com.simplekata.NoisyTest")

        assert result.junitResult().wasSuccessful()
        assert result.junitResult().errorCount() == 0
        assert result.junitResult().failureCount() == 0;

        assert result.stdOut().contains("splitKataIntoExercises")
        assert result.stdErr().get().contains("bar")
    }

    @Test
    public void testRunSpecificTest() {
        String testSource = """
package com.simplekata;

import org.junit.Test;
import static org.junit.Assert.*;

public class MultipleTest
{
   @Test
   public void one()
   {
        assertEquals(1,1);
   }

   @Test
   public void two()
   {
        assertEquals(1,2);
   }
}
        """

        def sources = Maps.immutable.of("MultipleTest.java", testSource)
        def compileResult = new KataCompiler().compile(sources);

        def kataResult = StdOutStdErrCapturingKataRunner
                .run(compileResult.compiledClasses(), "com.simplekata.MultipleTest", "two")

        assert !kataResult.junitResult().wasSuccessful()
        assert kataResult.junitResult().runCount() == 1
        assert kataResult.junitResult().errorCount() == 1
        assert kataResult.junitResult().failureCount() == 0;
        assert kataResult.stdOut().contains("expected:<1> but was:<2>")
    }

    @Test
    public void testMultipleClassesWithSameName() {
        String test1Source = """
package com.simplekata;

import org.junit.Test;
import static org.junit.Assert.*;

public class HelloTest
{
   @Test
   public void one()
   {
        System.out.println("HelloTest v1");
   }
}
        """

        //execution of HelloTest
        def sources = Maps.immutable.of("HelloTest.java", test1Source)
        def compileResult = new KataCompiler().compile(sources)

        def kataResult = StdOutStdErrCapturingKataRunner
                .run(compileResult.compiledClasses(), "com.simplekata.HelloTest", "one")

        assert kataResult.junitResult().wasSuccessful()
        assert kataResult.stdOut().contains("HelloTest v1")

        String test2Source = """
package com.simplekata;

import org.junit.Test;
import static org.junit.Assert.*;

public class HelloTest
{
   @Test
   public void one()
   {
        System.out.println("HelloTest v2");
   }
}
        """

        //execution of ANOTHER class, also called as HelloTest
        sources = Maps.immutable.of("HelloTest.java", test2Source)
        compileResult = new KataCompiler().compile(sources)

        kataResult = StdOutStdErrCapturingKataRunner
                .run(compileResult.compiledClasses(), "com.simplekata.HelloTest", "one")

        assert kataResult.junitResult().wasSuccessful()
        assert kataResult.stdOut().contains("HelloTest v2")
    }

    @Test
    public void stdOutAndStdErrNotCaptured() {
        String testSource = """
package com.simplekata;

import org.junit.Test;
import static org.junit.Assert.*;

public class NoisyTest
{
   @Test
   public void one()
   {
        System.out.println("splitKataIntoExercises");
        System.err.println("bar");
   }
}
        """
        def sources = Maps.immutable.of("NoisyTest.java", testSource)
        CompilationResult compilationResult = new KataCompiler().compile(sources);

        def result = new KataRunner()
                .run(compilationResult.compiledClasses(), "com.simplekata.NoisyTest")

        assert result.junitResult().wasSuccessful()
        assert result.junitResult().errorCount() == 0
        assert result.junitResult().failureCount() == 0;

        assert !result.stdOut().contains("splitKataIntoExercises")
        assert !result.stdErr().isPresent()
    }

}