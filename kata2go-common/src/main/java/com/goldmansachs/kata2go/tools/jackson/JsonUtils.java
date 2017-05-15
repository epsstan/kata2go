package com.goldmansachs.kata2go.tools.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonUtils
{
    public static ObjectWriter prettyPrinter()
    {
        return new ObjectMapper().writerWithDefaultPrettyPrinter();
    }
}
