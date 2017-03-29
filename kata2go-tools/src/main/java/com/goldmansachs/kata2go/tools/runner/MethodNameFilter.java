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

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

public class MethodNameFilter extends Filter
{
    private final String methodName;

    public MethodNameFilter(String methodName)
    {
        this.methodName = methodName;
    }

    @Override
    public boolean shouldRun(Description description)
    {
        return description.getMethodName().equals(methodName);
    }

    @Override
    public String describe()
    {
        return "method sourceFileName filter";
    }

    @Override
    public void apply(Object child) throws NoTestsRemainException
    {
        super.apply(child);
    }
}