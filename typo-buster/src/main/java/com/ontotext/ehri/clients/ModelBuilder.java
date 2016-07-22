package com.ontotext.ehri.clients;

public class ModelBuilder {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("USAGE: java " + ModelBuilder.class.getName() + " <token file> <model file> [<dump file>]");
            System.exit(1);
        }

        if (args.length < 2) {
            System.err.println("Not enough arguments!");
            System.exit(1);
        }
    }
}
