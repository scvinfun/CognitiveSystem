package CognitiveServices;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.PropertiesUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class POSTaggingController {
    private static POSTaggingController instance = null;

    public static POSTaggingController getInstance() throws IOException {
        if (instance == null) {
            instance = new POSTaggingController();
        }
        return instance;
    }

    public void tagging() {
        String text = "Joe Smith was born in California. " +
                "In 2017, he went to Paris, France in the summer. " +
                "His flight left at 3:00pm on July 10th, 2017. " +
                "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
                "He sent a postcard to his sister Jane Smith. " +
                "After hearing about Joe's trip, Jane decided she might go to France one day.";

        Document doc = new Document(text);
        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            System.out.println("The second word of the sentence '" + sent + "' is " + sent.word(1));
            System.out.println("The third lemma of the sentence '" + sent + "' is " + sent.lemma(2));
            System.out.println("The parse of the sentence '" + sent + "' is " + sent.parse());
            System.out.println();
        }
    }

    private boolean isQuotationSentence(String sentence) {
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(sentence);
        if (matcher.find())
            return true;
        else
            return false;
    }
}
