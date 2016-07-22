package com.ontotext.ehri.experiments;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.language.*;
import org.apache.commons.codec.language.bm.BeiderMorseEncoder;

import java.io.*;

/**
 * Test various algorithms for phonetic similarity.
 */
public class PhoneticSimilarity {
    private static final String SEPARATOR = "\t";

    private static final StringEncoder[] ENCODERS = {
            new BeiderMorseEncoder(),
            new Caverphone1(),
            new Caverphone2(),
            new ColognePhonetic(),
            new DaitchMokotoffSoundex(),
            new DoubleMetaphone(),
            new MatchRatingApproachEncoder(),
            new Metaphone(),
            new Nysiis(),
            //new RefinedSoundex(),
            //new Soundex()
    };

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("USAGE: java " + PhoneticSimilarity.class.getName() + " <input file> <output file>");
            System.exit(1);
        }

        if (args.length < 2) {
            System.err.println("Not enough arguments!");
            System.exit(1);
        }

        try {
            BufferedReader input = new BufferedReader(new FileReader(args[0]));
            BufferedWriter output = new BufferedWriter(new FileWriter(args[1]));
            String line = input.readLine();
            output.write(line);

            for (StringEncoder encoder : ENCODERS) {
                output.write(SEPARATOR + encoder.getClass().getSimpleName());
            }

            output.newLine();

            while ((line = input.readLine()) != null) {
                output.write(line);
                String[] strings = line.split(SEPARATOR);

                for (StringEncoder encoder : ENCODERS) {
                    output.write(SEPARATOR + encoder.encode(strings[0]).equals(encoder.encode(strings[1])));
                }

                output.newLine();
            }

            input.close();
            output.close();

        } catch (IOException | EncoderException e) {
            e.printStackTrace();
        }
    }
}
