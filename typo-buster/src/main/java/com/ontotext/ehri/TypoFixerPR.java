package com.ontotext.ehri;

import com.ontotext.ehri.tybus.Index;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleResource;

@CreoleResource(name = "Typo Fixer", comment = "Fixes typos.")
public class TypoFixerPR extends AbstractLanguageAnalyser {
    private Index index;

    @Override
    public Resource init() throws ResourceInstantiationException {
        // TODO: create index according to user preferences
        return this;
    }

    @Override
    public void execute() throws ExecutionException {
        // TODO: bust typos with index
    }
}
