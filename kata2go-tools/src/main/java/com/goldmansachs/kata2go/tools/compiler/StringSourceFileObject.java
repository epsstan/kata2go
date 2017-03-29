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

package com.goldmansachs.kata2go.tools.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class StringSourceFileObject extends SimpleJavaFileObject
{
    private final String source;

    public StringSourceFileObject(String name, String source) throws URISyntaxException
    {
        super(new URI(name), Kind.SOURCE);
        this.source = source;
    }

    public static StringSourceFileObject make(String name, String source)
    {
        try
        {
            return new StringSourceFileObject(name, source);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException
    {
        return source;
    }
}
