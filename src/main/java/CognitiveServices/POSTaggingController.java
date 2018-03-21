package CognitiveServices;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class POSTaggingController {
    private static POSTaggingController instance = null;
    POSTaggerME tagger = null;

    public static POSTaggingController getInstance() throws IOException {
        if (instance == null) {
            instance = new POSTaggingController();
        }
        return instance;
    }

    private POSTaggingController() throws IOException {
        if (tagger == null) {
            InputStream modelIn = new FileInputStream(getClass().getResource("").getPath().split("target")[0] + "src/main/POSTag/en-pos-maxent.bin");
            POSModel model = new POSModel(modelIn);
            tagger = new POSTaggerME(model);
        }
    }

    public void tagging(String sentence) {
        String[] words = sentence.split("\\W+");
        String tags[] = tagger.tag(words);
    }
}
