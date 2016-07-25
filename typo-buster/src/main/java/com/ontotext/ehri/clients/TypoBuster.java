package com.ontotext.ehri.clients;

import com.ontotext.ehri.tools.Models;
import com.ontotext.ehri.tools.Serialization;
import com.ontotext.ehri.tybus.Index;
import com.ontotext.ehri.tybus.Model;

import java.io.*;

public class TypoBuster {

    public static void main(String[] args) {

        // print usage
        if (args.length == 0) {
            System.out.println("USAGE: java " + TypoBuster.class.getName() + " <input file> <output file> [<index file>]");
            System.exit(1);
        }

        // check arguments
        if (args.length < 2) {
            System.err.println("Not enough arguments!");
            System.exit(1);
        }

        Index index = null;
        long start, time;

        // build new model and index
        if (args.length < 3) {
            System.out.print("Building model...");
            start = System.currentTimeMillis();
            Model model = new Model();

            try {
                Models.extendModel(model, new File(args[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }

            time = System.currentTimeMillis() - start;
            System.out.println(" " + time + " ms");

            // TODO: allow the user to override defaults
            System.out.print("Building index...");
            start = System.currentTimeMillis();
            index = new Index(model);
            time = System.currentTimeMillis() - start;
            System.out.println(" " + time + " ms");

        // deserialize existing index
        } else {
            System.out.print("Deserializing index...");
            start = System.currentTimeMillis();

            try {
                index = (Index) Serialization.deserialize(new File(args[2]));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            time = System.currentTimeMillis() - start;
            System.out.println(" " + time + " ms");
        }

        if (index == null) {
            System.err.println("No index!");
            System.exit(1);
        }

        // bust typos
        System.out.print("Busting typos...");
        start = System.currentTimeMillis();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
            String token;

            while ((token = reader.readLine()) != null) {
                token = token.trim();
                writer.write(index.correctFully(token));
                writer.newLine();
            }

            reader.close();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        time = System.currentTimeMillis() - start;
        System.out.println(" " + time + " ms");
    }
}
