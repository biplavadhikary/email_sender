package com.example.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {
    public static String readAsStringFromFile(String relativePath, boolean shouldThrowException) throws IOException {
        StringBuilder builder = new StringBuilder();

        String fullPath = ClassLoader.getSystemResource(relativePath).getPath();

        try (BufferedReader buffer = new BufferedReader(new FileReader(fullPath))) {
            String str = "";

            while ((str = buffer.readLine()) != null) {

                builder.append(str).append("\n");
            }
        }

        catch (IOException e) {
            if (shouldThrowException) {
                throw e;
            }
            else {
                builder = new StringBuilder();
                builder.append("");
            }
        }

        return builder.toString();
    }
}
