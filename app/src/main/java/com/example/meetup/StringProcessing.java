package com.example.meetup;

import android.content.res.Resources;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class StringProcessing {
    private static final String TAG = "String Processing";

    static String[] getPosTags(String[] sent) {
        InputStream posModelIn;
        String[] tags = new String[]{};
        try {
            posModelIn = new FileInputStream("opennlp-en-ud-ewt-pos-1.0-1.9.3.bin");
            POSModel posModel = new POSModel(posModelIn);
            POSTaggerME tagger = new POSTaggerME(posModel);

//            sent = new String[]{"Most", "large", "cities", "in", "the", "US", "had",
//                    "morning", "and", "afternoon", "newspapers", "."};
            tags = tagger.tag(sent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tags;
    }

    static String[] getTokenizedText(String s) {
        InputStream tokenizerModelIn;
        String[] tokens = new String[]{};
        try {
            tokenizerModelIn = Resources.getSystem().openRawResource(R.raw.tokens);
            TokenizerModel tokenizerModel= new TokenizerModel(tokenizerModelIn);
            Tokenizer tokenizer = new TokenizerME(tokenizerModel);

            // tokens = tokenizer.tokenize("An input sample sentence.");
            tokens = tokenizer.tokenize(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }

    public static void getNouns(String s) {
        String[] tokens = getTokenizedText(s);
        String[] tags = getPosTags(tokens);
        for (int i = 0; i < tags.length; i++) {
            Log.i(TAG, tokens[i] + ' ' + tags[i]);
        }
        ArrayList<String> nouns = new ArrayList<String>();
//        for (String tag : tags) {
//            if (// tag is noun)
//        }
    }
}
