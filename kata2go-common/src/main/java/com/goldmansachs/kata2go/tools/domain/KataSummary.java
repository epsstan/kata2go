package com.goldmansachs.kata2go.tools.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@Value.Immutable
@JsonSerialize(as = ImmutableKataSummary.class)
@JsonDeserialize(as = ImmutableKataSummary.class)
public interface KataSummary
{
    @JsonProperty
    String name();

    @JsonProperty
    String description();

    @JsonProperty
    List<KataExercise> exercises();

    @JsonProperty
    Map<String, String> sources();
}
