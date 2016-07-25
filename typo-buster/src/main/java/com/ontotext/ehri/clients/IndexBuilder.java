package com.ontotext.ehri.clients;

import com.ontotext.ehri.tools.Serialization;
import com.ontotext.ehri.tybus.Index;
import com.ontotext.ehri.tybus.Model;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class IndexBuilder {

    public static void main(String[] args) {

        // print usage
        if (args.length == 0) {
            System.out.println("USAGE: java " + IndexBuilder.class.getName() + " <model file> <index file> [<dump file>]");
            System.exit(1);
        }

        // check arguments
        if (args.length < 2) {
            System.err.println("Not enough arguments!");
            System.exit(1);
        }

        Model model = null;
        long start, time;

        // deserialize model
        System.out.print("Deserializing model...");
        start = System.currentTimeMillis();

        try {
            model = (Model) Serialization.deserialize(new File(args[0]));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        time = System.currentTimeMillis() - start;
        System.out.println(" " + time + " ms");

        if (model == null) {
            System.err.println("No model!");
            System.exit(1);
        }

        // prompt user for index parameters
        Scanner scanner = new Scanner(System.in);

        int minLength = Index.MIN_LENGTH;
        System.out.println("Enter minimum length of correction or typo or press ENTER to use default (" + minLength + "):");
        Integer minLengthInput = parseInteger(scanner.nextLine());
        if (minLengthInput != null) minLength = minLengthInput;

        int minCorrectionFrequency = Index.MIN_CORRECTION_FREQUENCY;
        System.out.println("Enter minimum frequency of correction or press ENTER to use default (" + minCorrectionFrequency + "):");
        Integer minCorrectionFrequencyInput = parseInteger(scanner.nextLine());
        if (minCorrectionFrequencyInput != null) minCorrectionFrequency = minCorrectionFrequencyInput;

        int maxTypoFrequency = Index.MAX_TYPO_FREQUENCY;
        System.out.println("Enter absolute maximum frequency of typo or press ENTER to use default (" + maxTypoFrequency + "):");
        Integer maxTypoFrequencyInput = parseInteger(scanner.nextLine());
        if (maxTypoFrequencyInput != null) maxTypoFrequency = maxTypoFrequencyInput;

        float typoFrequencyRatio = Index.TYPO_FREQUENCY_RATIO;
        System.out.println("Enter typo-frequency to correction-frequency ratio or press ENTER to use default (" + typoFrequencyRatio + "):");
        Float typoFrequencyRatioInput = parseFloat(scanner.nextLine());
        if (typoFrequencyRatioInput != null) typoFrequencyRatio = typoFrequencyRatioInput;

        boolean checkPhonetics = Index.CHECK_PHONETICS;
        System.out.println("Enter toggle phonetic check or press ENTER to use default (" + checkPhonetics + "):");
        Boolean checkPhoneticsInput = parseBoolean(scanner.nextLine());
        if (checkPhoneticsInput != null) checkPhonetics = checkPhoneticsInput;

        scanner.close();

        // build index
        System.out.print("Building index...");
        start = System.currentTimeMillis();
        Index index = new Index(model, minLength, minCorrectionFrequency, maxTypoFrequency, typoFrequencyRatio, checkPhonetics);
        time = System.currentTimeMillis() - start;
        System.out.println(" " + time + " ms");

        // dump index
        if (args.length >= 3) {
            System.out.print("Dumping index...");
            start = System.currentTimeMillis();

            try {
                Serialization.dump(index, new File(args[2]));
            } catch (IOException e) {
                e.printStackTrace();
            }

            time = System.currentTimeMillis() - start;
            System.out.println(" " + time + " ms");
        }

        // serialize index
        System.out.print("Serializing index...");
        start = System.currentTimeMillis();

        try {
            Serialization.serialize(index, new File(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        time = System.currentTimeMillis() - start;
        System.out.println(" " + time + " ms");
    }

    private static Integer parseInteger(String string) {
        Integer i = null;
        string = string.trim();

        try {
            if (string.length() > 0) i = Integer.valueOf(string);
        } catch (NumberFormatException e) {
            System.err.println("Invalid integer!");
        }

        return i;
    }

    private static Float parseFloat(String string) {
        Float f = null;
        string = string.trim();

        try {
            if (string.length() > 0) f = Float.valueOf(string);
        } catch (NumberFormatException e) {
            System.err.println("Invalid float!");
        }

        return f;
    }

    private static Boolean parseBoolean(String string) {
        Boolean b = null;
        string = string.trim();

        try {
            if (string.length() > 0) b = Boolean.valueOf(string);
        } catch (NumberFormatException e) {
            System.err.println("Invalid boolean!");
        }

        return b;
    }
}
