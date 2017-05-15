package com.goldmansachs.kata2go.tools.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableKataExercise.class)
@JsonDeserialize(as = ImmutableKataExercise.class)
public interface KataExercise
{
    @JsonProperty
    String sourceFileName();

    @JsonProperty
    String className();

    @JsonProperty
    String source();
}
