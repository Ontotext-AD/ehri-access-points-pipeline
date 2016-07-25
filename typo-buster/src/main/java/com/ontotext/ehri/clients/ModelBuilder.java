package com.ontotext.ehri.clients;

import com.ontotext.ehri.tools.Models;
import com.ontotext.ehri.tools.Serialization;
import com.ontotext.ehri.tybus.Model;

import java.io.File;
import java.io.IOException;

public class ModelBuilder {

    public static void main(String[] args) {

        // print usage
        if (args.length == 0) {
            System.out.println("USAGE: java " + ModelBuilder.class.getName() + " <token file> <model file> [<dump file>]");
            System.exit(1);
        }

        // check arguments
        if (args.length < 2) {
            System.err.println("Not enough arguments!");
            System.exit(1);
        }

        File modelFile = new File(args[1]);
        Model model = null;
        long start, time;

        // deserialize existing model
        if (modelFile.isFile()) {
            System.out.print("Deserializing model...");
            start = System.currentTimeMillis();

            try {
                model = (Model) Serialization.deserialize(modelFile);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            time = System.currentTimeMillis() - start;
            System.out.println(" " + time + " ms");

        // initialize new model
        } else {
            System.out.print("Initializing model...");
            start = System.currentTimeMillis();
            model = new Model();
            time = System.currentTimeMillis() - start;
            System.out.println(" " + time + " ms");
        }

        if (model == null) {
            System.err.println("No model!");
            System.exit(1);
        }

        // extend model with new tokens
        System.out.print("Extending model...");
        start = System.currentTimeMillis();

        try {
            Models.extendModel(model, new File(args[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        time = System.currentTimeMillis() - start;
        System.out.println(" " + time + " ms");

        // dump model
        if (args.length >= 3) {
            System.out.print("Dumping model...");
            start = System.currentTimeMillis();

            try {
                Models.dumpModel(model, new File(args[2]));
            } catch (IOException e) {
                e.printStackTrace();
            }

            time = System.currentTimeMillis() - start;
            System.out.println(" " + time + " ms");
        }

        // serialize model
        System.out.print("Serializing model...");
        start = System.currentTimeMillis();

        try {
            Serialization.serialize(model, new File(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        time = System.currentTimeMillis() - start;
        System.out.println(" " + time + " ms");
    }
}
