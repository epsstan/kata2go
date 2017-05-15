package com.goldmansachs.kata2go.tools.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

public class FileUtils
{
    public static String readFile(File file)
    {
        try
        {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e)
        {
            // TODO: handle exception
            return "";
        }
    }

    public static File loadFileFromClassPath(String name) throws URISyntaxException
    {
        URL resource = FileUtils.class.getResource(name);
        return new File(resource.toURI());
    }
}
