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

import java.util.Map;

/*
    Class loader that loads class from memory.
 */

public class ManagedClassLoader extends ClassLoader
{
    private final Map<String, ByteArrayFileObject> classes;

    public ManagedClassLoader(Map<String, ByteArrayFileObject> classes)
    {
        super(ManagedClassLoader.class.getClassLoader());
        this.classes = classes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        if (!classes.containsKey(name))
        {
            throw new ClassNotFoundException(name + " not a managed class");
        }
        ByteArrayFileObject byteArrayFileObject = classes.get(name);
        byte[] bytes = byteArrayFileObject.getBytes();
        return defineClass(name, bytes, 0, bytes.length);
    }
}
