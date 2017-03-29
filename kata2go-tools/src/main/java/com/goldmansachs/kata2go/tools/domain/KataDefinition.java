package com.goldmansachs.kata2go.tools.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@Value.Immutable
@JsonSerialize(as = ImmutableKataDefinition.class)
@JsonDeserialize(as = ImmutableKataDefinition.class)
public interface KataDefinition
{
    @JsonProperty
    KataMetadata metadata();

    @JsonProperty
    List<KataExercise> exercises();

    @JsonProperty
    Map<String, String> sources();

    @JsonProperty
    List<String> jars();

    default KataSummary toSummary()
    {
        return ImmutableKataSummary.builder()
                .name(metadata().name())
                .description(metadata().description())
                .exercises(exercises())
                .sources(sources())
                .build();
    }

}
