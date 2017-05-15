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
package com.goldmansachs.kata2go.executor.compiler

import org.eclipse.collections.api.map.ImmutableMap
import org.eclipse.collections.impl.factory.Lists
import org.eclipse.collections.impl.factory.Maps
import org.junit.Test

import java.nio.file.Paths

class TestKataCompiler {
    @Test
    public void compilationSuccessful()
    {
        String testSource = """
public class Test
{
    public static void main(String[] args)
    {
    }
}
        """

        ImmutableMap<String, String> sources = Maps.immutable.of("Test.java", testSource);
        CompilationResult compilationResult = new KataCompiler().compile(sources);

        assert compilationResult.successful()
        assert compilationResult.errors().isEmpty()
    }

    @Test
    public void compilationFailure()
    {
        String testSource = """
public class Test
{
    public static main(String[] args)
    {
    }
}
        """

        ImmutableMap<String, String> sources = Maps.immutable.of("Test.java", testSource);
        CompilationResult compilationResult = new KataCompiler().compile(sources);

        assert !compilationResult.successful()
        assert !compilationResult.errors().isEmpty()
        assert compilationResult.errors()[0].contains("invalid method declaration; return type required")
    }

    @Test
    public void compileMutlipleFiles()
    {
        String test1Source = """
public class Test1
{
    public static void main(String[] args)
    {
    }
}
        """

        String test2Source = """
public class Test2 extends Test1
{
    public static void main(String[] args)
    {
    }
}
        """

        def sources = Maps.immutable.of("Test1.java", test1Source, "Test2.java", test2Source);
        def compilationResult = new KataCompiler().compile(sources);

        assert compilationResult.successful()
        assert compilationResult.errors().isEmpty()

    }

    @Test
    public void testWithJars()
    {
        String test1Source = """
import com.hello.Hello;
import com.world.World;
public class Test1
{
    public static void main(String[] args)
    {
        System.out.println(Hello.hello());
        System.out.println(World.world());
    }
}
        """

        def helloJar = TestKataCompiler.class.classLoader.getResource("hello.jar");
        def helloJarPath = Paths.get(helloJar.toURI()).toFile().absolutePath

        def worldJar = TestKataCompiler.class.classLoader.getResource("world.jar");
        def worldJarPath = Paths.get(worldJar.toURI()).toFile().absolutePath

        def classPathJars = Lists.immutable.of(helloJarPath, worldJarPath)

        def sources = Maps.immutable.of("Test1.java", test1Source);
        def compilationResult = new KataCompiler().compile(sources, classPathJars);

        assert compilationResult.successful()
        assert compilationResult.errors().isEmpty()
    }
}
