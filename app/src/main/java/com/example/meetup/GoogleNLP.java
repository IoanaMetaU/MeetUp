package com.example.meetup;
// Imports the Google Cloud client library
import android.util.Log;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.AnalyzeSyntaxRequest;
import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Token;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;

import java.io.FileInputStream;
import java.io.IOException;

public class GoogleNLP {
    private static final String TAG = "GoogleNLP";

    static void authExplicit() throws IOException {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("meetup-355522-912b479b8648.json"))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        Storage storage = (Storage) StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        System.out.println("Buckets:");
        Page<Bucket> buckets = storage.list();
        for (Bucket bucket : buckets.iterateAll()) {
            System.out.println(bucket.toString());
        }
    }

    public static void getNouns(String text) throws IOException {
        authExplicit();

        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
            AnalyzeSyntaxRequest request =
                    AnalyzeSyntaxRequest.newBuilder()
                            .setDocument(doc)
                            .setEncodingType(EncodingType.UTF16)
                            .build();
            // analyze the syntax in the given text
            AnalyzeSyntaxResponse response = language.analyzeSyntax(request);
            // print the response
            for (Token token : response.getTokensList()) {
                Log.i(TAG, token.getText().getContent() + ' ' + token.getPartOfSpeech().getTag());
//                System.out.printf("\tText: %s\n", token.getText().getContent());
//                System.out.printf("\tBeginOffset: %d\n", token.getText().getBeginOffset());
//                System.out.printf("Lemma: %s\n", token.getLemma());
//                System.out.printf("PartOfSpeechTag: %s\n", token.getPartOfSpeech().getTag());
//                System.out.printf("\tAspect: %s\n", token.getPartOfSpeech().getAspect());
//                System.out.printf("\tCase: %s\n", token.getPartOfSpeech().getCase());
//                System.out.printf("\tForm: %s\n", token.getPartOfSpeech().getForm());
//                System.out.printf("\tGender: %s\n", token.getPartOfSpeech().getGender());
//                System.out.printf("\tMood: %s\n", token.getPartOfSpeech().getMood());
//                System.out.printf("\tNumber: %s\n", token.getPartOfSpeech().getNumber());
//                System.out.printf("\tPerson: %s\n", token.getPartOfSpeech().getPerson());
//                System.out.printf("\tProper: %s\n", token.getPartOfSpeech().getProper());
//                System.out.printf("\tReciprocity: %s\n", token.getPartOfSpeech().getReciprocity());
//                System.out.printf("\tTense: %s\n", token.getPartOfSpeech().getTense());
//                System.out.printf("\tVoice: %s\n", token.getPartOfSpeech().getVoice());
//                System.out.println("DependencyEdge");
//                System.out.printf("\tHeadTokenIndex: %d\n", token.getDependencyEdge().getHeadTokenIndex());
//                System.out.printf("\tLabel: %s\n\n", token.getDependencyEdge().getLabel());
            }
            // return response.getTokensList();
        }
    }
}