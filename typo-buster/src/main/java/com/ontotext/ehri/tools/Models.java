package com.ontotext.ehri.tools;

import com.ontotext.ehri.tybus.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Models {

    public static void extendModel(Model model, File tokenFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(tokenFile));
        String token;

        while ((token = reader.readLine()) != null) {
            token = token.trim();
            model.addToken(token);
        }

        reader.close();
    }
}
