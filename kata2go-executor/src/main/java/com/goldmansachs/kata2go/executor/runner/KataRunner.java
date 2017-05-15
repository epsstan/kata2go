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

package com.goldmansachs.kata2go.executor.runner;

import com.goldmansachs.kata2go.executor.compiler.ByteArrayFileObject;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestResult;
import junit.textui.TestRunner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

/*
    Wrapper around the standard Junit runner.
    Unlike StdOutStdErrCapturingKataRunner, this runner does not capture System.err
 */

public class KataRunner
{
    private ByteArrayOutputStream stdOutByteArray = new ByteArrayOutputStream();
    private PrintStream stdOut = new PrintStream(stdOutByteArray);

    public KataJunitTestResult run(Map<String, ByteArrayFileObject> classes, String testClassName)
    {
        return run(classes, testClassName, Filter.ALL);
    }

    public KataJunitTestResult run(Map<String, ByteArrayFileObject> classes, String testClassName, String testName)
    {
        return run(classes, testClassName, new MethodNameFilter(testName));
    }

    private KataJunitTestResult run(Map<String, ByteArrayFileObject> classes, String testClassName, Filter testFilter)
    {
        ManagedClassLoader managedClassLoader = new ManagedClassLoader(classes);
        Class<?> testClass = null;
        try
        {
            testClass = managedClassLoader.findClass(testClassName);
        } catch (ClassNotFoundException e)
        {
            // TODO: handle exception
            e.printStackTrace();
        }
        JUnit4TestAdapter adapter = new JUnit4TestAdapter(testClass);

        try
        {
            adapter.filter(testFilter);
        } catch (NoTestsRemainException e)
        {
            // TODO: handle exception
            e.printStackTrace();
        }

        TestRunner testRunner = new TestRunner(stdOut);
        TestResult testResult = testRunner.doRun(adapter, false);
        return ImmutableKataJunitTestResult.builder()
                .junitResult(testResult)
                .stdOut(stdOutByteArray.toString())
                .build();
    }
}
