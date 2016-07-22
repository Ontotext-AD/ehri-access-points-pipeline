package com.ontotext.ehri.clients;

public class IndexBuilder {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("USAGE: java " + IndexBuilder.class.getName() + " <model file> <index file> [<dump file>]");
            System.exit(1);
        }

        if (args.length < 2) {
            System.err.println("Not enough arguments!");
            System.exit(1);
        }
    }
}
