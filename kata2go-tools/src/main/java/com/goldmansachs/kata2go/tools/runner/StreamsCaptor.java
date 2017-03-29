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

package com.goldmansachs.kata2go.tools.runner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/*
    Helper class to capture JVM's stdout and stderr
 */

public class StreamsCaptor
{
    static class Pair
    {
        public final StreamsCaptor savedStreams;
        public final StreamsCaptor newStreams;

        public Pair(StreamsCaptor savedStreams, StreamsCaptor newStreams)
        {
            this.savedStreams = savedStreams;
            this.newStreams = newStreams;
        }

        public void restoreSystemStreams()
        {
            System.setOut(savedStreams.stdOut);
            System.setErr(savedStreams.stdErr);
        }
    }

    private PrintStream stdOut;
    private final ByteArrayOutputStream stdOutByteArray;
    private PrintStream stdErr;
    private final ByteArrayOutputStream stdErrByteArray;

    public StreamsCaptor()
    {
        this.stdOutByteArray = new ByteArrayOutputStream();
        this.stdOut = new PrintStream(stdOutByteArray);
        this.stdErrByteArray = new ByteArrayOutputStream();
        this.stdErr = new PrintStream(stdErrByteArray);
    }

    public StreamsCaptor(PrintStream stdOut, PrintStream stdErr)
    {
        this.stdOut = stdOut;
        this.stdOutByteArray = new ByteArrayOutputStream();
        this.stdErr = stdErr;
        this.stdErrByteArray = new ByteArrayOutputStream();
    }


    public ByteArrayOutputStream getStdErrByteArray()
    {
        return stdErrByteArray;
    }

    public ByteArrayOutputStream getStdOutByteArray()
    {
        return stdOutByteArray;
    }

    public static Pair captureAndResetSystemStreams()
    {
        // capture the System's stdout and stderr
        StreamsCaptor existingStreams = new StreamsCaptor(System.out, System.err);

        // Reset the System's streams with newly created stdout and stderr
        StreamsCaptor newStreams = new StreamsCaptor();
        System.setOut(newStreams.stdOut);
        System.setErr(newStreams.stdErr);

        return new Pair(existingStreams, newStreams);
    }
}
