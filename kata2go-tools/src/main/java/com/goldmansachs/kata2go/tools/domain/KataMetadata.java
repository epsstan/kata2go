package com.goldmansachs.kata2go.tools.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Value.Immutable
@Value.Style(jdkOnly = true)
@JsonSerialize(as = ImmutableKataMetadata.class)
@JsonDeserialize(as = ImmutableKataMetadata.class)
public interface KataMetadata
{
    @JsonProperty
    String name();

    @JsonProperty
    String description();

    /*
        Map exercise source file names to the fully qualified class names.
        This is so that we don't have to parse the source multiple times for the class names.
     */
    @JsonProperty
    Map<String, String> exerciseMetaData();

    default void writeTo(Path path) throws IOException
    {
        new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValue(path.toFile(), this);
    }

    static KataMetadata readFrom(Path path) throws IOException
    {
        return new ObjectMapper().readValue(path.toFile(), KataMetadata.class);
    }
}
