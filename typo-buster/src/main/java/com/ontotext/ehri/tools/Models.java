package com.ontotext.ehri.tools;

import com.ontotext.ehri.tybus.Model;

import java.io.*;

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

    public static void dumpModel(Model model, File dumpFile) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(dumpFile));
        writer.write(model.toString());
        writer.close();
    }
}
