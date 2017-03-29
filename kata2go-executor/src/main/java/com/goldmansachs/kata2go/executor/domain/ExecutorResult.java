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

package com.goldmansachs.kata2go.executor.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.goldmansachs.kata2go.tools.compiler.CompilationResult;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableExecutorResult.class)
@JsonDeserialize(as = ImmutableExecutorResult.class)
public interface ExecutorResult
{
    @JsonProperty
    CompilationResult compilationResult();

    @JsonProperty
    RunResult runResult();
}
