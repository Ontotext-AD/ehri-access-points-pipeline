#!/usr/bin/env perl

use strict;
use warnings;

# check arguments
die "USAGE: $0 <input file> <output file>\n" unless $#ARGV == 1;

# open file handles
open (my $input, '<:utf8', $ARGV[0]) or die "ERROR: cannot read from \"$ARGV[0]\"\n";
open (my $output, '>:utf8', $ARGV[1]) or die "ERROR: cannot write to \"$ARGV[1]\"\n";

# process input line by line
while (<$input>) {
    next if /^\?/;
    next unless /^([^\t]+)\t"(\d+)"/;

    # output each name as many times as its frequency
    for (my $i = 0; $i < $2; $i++) {
        print $output $1 . "\n";
    }
}

# close file handles
close($input);
close($output);

