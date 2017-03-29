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

package com.goldmansachs.kata2go.tools.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;

/*
    Utility class for creating and extracting tar archives
 */
public class TarGz
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TarGz.class);

    private static final String UNIX_TAR = "/usr/bin/tar";

    public static void compress(Path sourceDirectoryPath, Path archiveFilePath) throws Exception
    {
        try
        {
            ImmutableList<String> tarArgs = Lists.immutable.of(
                    UNIX_TAR,
                    "cvzf",
                    archiveFilePath.toFile().getAbsolutePath(),
                    ".");
            Process process = new ProcessBuilder(tarArgs.toList())
                    .directory(sourceDirectoryPath.toFile())
                    .start();
            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                logStdout(process.getInputStream());
                logStdErr(process.getErrorStream());
                throw new Exception("Failed to compress");
            }
        } catch (Exception e)
        {
            throw new Exception("Failed to compress", e);
        }
    }

    private static void decompressUtil(Path archiveFilePath, Path workingDir, boolean stripParent) throws Exception
    {
        try
        {
            MutableList<String> tarArgs = Lists.mutable.of(
                    UNIX_TAR,
                    "xvzf",
                    archiveFilePath.toFile().getAbsolutePath());

            if (stripParent)
            {
                tarArgs = tarArgs.with("--strip").with("1");
            }
            Process process = new ProcessBuilder(tarArgs.toList()).directory(workingDir.toFile()).start();
            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                logStdout(process.getInputStream());
                logStdErr(process.getErrorStream());
                throw new Exception("Failed to decompress");
            }
        } catch (Exception e)
        {
            throw new Exception("Failed to decompress", e);
        }
    }

    public static void decompress(Path archiveFilePath, Path workingDir) throws Exception
    {
        decompressUtil(archiveFilePath, workingDir, true);
    }

    public static void decompress(Path archiveFilePath, Path workingDir, boolean stripParent) throws Exception
    {
        decompressUtil(archiveFilePath, workingDir, stripParent);
    }

    private static void logStdout(InputStream inputStream) throws IOException
    {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = reader.readLine()) != null)
        {
            LOGGER.error("stdout : {}", line);
        }
    }

    private static void logStdErr(InputStream inputStream) throws IOException
    {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = reader.readLine()) != null)
        {
            LOGGER.error("stderr : {}", line);
        }
    }

    /*
        Write the input stream to a tar gz file.
     */
    public static void write(InputStream tarGzInputStream, Path outTarGz) throws IOException
    {
        GzipCompressorInputStream gzipStream = new GzipCompressorInputStream(tarGzInputStream);
        FileOutputStream fios = new FileOutputStream(outTarGz.toFile());
        int buffersize = 1024;
        final byte[] buffer = new byte[buffersize];
        int n = 0;
        while (-1 != (n = gzipStream.read(buffer)))
        {
            fios.write(buffer, 0, n);
        }
        fios.close();
        gzipStream.close();
    }

    /*
        Unzip the input stream to a directory.
     */
    public static void decompress(InputStream tarGzInputStream, Path outDir) throws IOException
    {
        GzipCompressorInputStream gzipStream = new GzipCompressorInputStream(tarGzInputStream);
        TarArchiveInputStream tarInput = new TarArchiveInputStream(gzipStream);
        TarArchiveEntry entry;
        int bufferSize = 1024;
        while ((entry = (TarArchiveEntry) tarInput.getNextEntry()) != null)
        {
            String entryName = entry.getName();
            // strip out the leading directory like the --strip tar argument
            String entryNameWithoutLeadingDir = entryName.substring(entryName.indexOf("/") + 1);
            if (entryNameWithoutLeadingDir.isEmpty())
            {
                continue;
            }
            Path outFile = outDir.resolve(entryNameWithoutLeadingDir);
            if (entry.isDirectory())
            {
                outFile.toFile().mkdirs();
                continue;
            }
            int count;
            byte data[] = new byte[bufferSize];
            BufferedOutputStream fios = new BufferedOutputStream(
                    new FileOutputStream(outFile.toFile()), bufferSize);
            while ((count = tarInput.read(data, 0, bufferSize)) != -1)
            {
                fios.write(data, 0, count);
            }
            fios.close();
        }
        tarInput.close();
        gzipStream.close();
    }

    public static void decompressFromStream2(InputStream inputStream) throws IOException
    {
        GzipCompressorInputStream gzipStream = new GzipCompressorInputStream(inputStream);
        TarArchiveInputStream tarInput = new TarArchiveInputStream(gzipStream);
        TarArchiveEntry entry;
        int bufferSize = 1024;
        while ((entry = (TarArchiveEntry) tarInput.getNextEntry()) != null)
        {
            String entryName = entry.getName();
            // strip out the leading directory like the --strip tar argument
            String entryNameWithoutLeadingDir = entryName.substring(entryName.indexOf("/") + 1);
            if (entryNameWithoutLeadingDir.isEmpty())
            {
                continue;
            }
            if (entry.isDirectory())
            {
                System.out.println("found dir " + entry.getName());
            }
            else
            {
                int count;
                byte data[] = new byte[bufferSize];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((count = tarInput.read(data, 0, bufferSize)) != -1)
                {
                    baos.write(data, 0, count);
                }
                JarOutputStream jarOutputStream = new JarOutputStream(baos);
                System.out.println(new String(baos.toByteArray()));
            }
        }
        tarInput.close();
        gzipStream.close();
    }
}
