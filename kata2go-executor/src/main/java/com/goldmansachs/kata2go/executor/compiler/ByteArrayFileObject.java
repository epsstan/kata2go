/*
 Copyright 2016 Goldman Sachs.
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

package com.goldmansachs.kata2go.executor.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class ByteArrayFileObject extends SimpleJavaFileObject
{
    private ByteArrayOutputStream baos;

    public ByteArrayFileObject(String className, Kind kind) throws URISyntaxException
    {
        super(new URI(className), kind);
    }

    @Override
    public InputStream openInputStream() throws IOException
    {
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public OutputStream openOutputStream() throws IOException
    {
        this.baos = new ByteArrayOutputStream();
        return this.baos;
    }

    public byte[] getBytes()
    {
        return baos.toByteArray();
    }
}
