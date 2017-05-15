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

import java.util.Map;

/*
    Wrapper around the standard Junit runner.
 */

public class StdOutStdErrCapturingKataRunner
{
    public static synchronized KataJunitTestResult run(Map<String, ByteArrayFileObject> classes, String testClassName)
    {
        return run(classes, testClassName, Filter.ALL);
    }

    public static synchronized KataJunitTestResult run(Map<String, ByteArrayFileObject> classes, String testClassName, String testName)
    {
        return run(classes, testClassName, new MethodNameFilter(testName));
    }

    /*
        This method has to be static synchronized to serialize access to the JVM's stdout and stderr streams

        This method does the following :
        1) Capture the JVM's stdout and stderr
        2) Reset the JVM's stdout and stderr with new streams
        3) Runs the input test class
        4) Reset the JVM's stdout and stderr with the streams from step 1)
        5) Returns the Junit test result along with the stdout and stderr step 2)
     */
    private static synchronized KataJunitTestResult run(Map<String, ByteArrayFileObject> classes, String testClassName, Filter testFilter)
    {
        StreamsCaptor.Pair streamsPair = StreamsCaptor.captureAndResetSystemStreams();
        try
        {
            ManagedClassLoader managedClassLoader = new ManagedClassLoader(classes);
            Class<?> testClass = null;
            try
            {
                testClass = managedClassLoader.findClass(testClassName);
            }
            catch (ClassNotFoundException e)
            {
                // TODO: handle exception
                e.printStackTrace();
            }
            JUnit4TestAdapter adapter = new JUnit4TestAdapter(testClass);

            try
            {
                adapter.filter(testFilter);
            }
            catch (NoTestsRemainException e)
            {
                // TODO: handle exception
                e.printStackTrace();
            }

            TestResult testResult = new TestRunner().doRun(adapter, false);
            return ImmutableKataJunitTestResult.builder()
                    .junitResult(testResult)
                    .stdOut(streamsPair.newStreams.getStdOutByteArray().toString())
                    .stdErr(streamsPair.newStreams.getStdErrByteArray().toString())
                    .build();
        }
        finally
        {
            streamsPair.restoreSystemStreams();
        }
    }

}
