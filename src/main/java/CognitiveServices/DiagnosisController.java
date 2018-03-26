package CognitiveServices;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiagnosisController {
    private static DiagnosisController instance = null;

    public static DiagnosisController getInstance() throws IOException {
        if (instance == null) {
            instance = new DiagnosisController();
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
        for (Sentence sentence : doc.sentences()) {
            List<String> words = sentence.words();

            List<String> lemmas = sentence.lemmas();

            Collection<RelationTriple> openie = sentence.openieTriples();
            for (RelationTriple r : openie) {
                System.out.println(String.format("Subject: %s Relation: %s Object: %s", r.subjectGloss(), r.relationGloss(), r.objectGloss()));
            }

            Tree parse = sentence.parse();
            System.out.println(parse.toString());
            ArrayList<Tree> container = new ArrayList<>();
            getAllLeaf(container, parse);
            for (Tree t : container) {
                System.out.println(t.toString());
            }
            System.out.println();
        }
    }

    private void getAllLeaf(ArrayList<Tree> container, Tree tree) {
        if (tree.depth() == 1) {
            container.add(tree);
            return;
        }

        for (Tree t : tree.children()) {
            getAllLeaf(container, t);
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
