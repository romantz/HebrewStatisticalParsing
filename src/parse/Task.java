package parse;

import decode.Decode;
import grammar.Grammar;
import tree.Tree;

import java.util.List;

/**
 * Created by Roman_ on 24/05/2018.
 */
class Task implements Runnable {
    List<String> sentence;
    Decode decodeInstance;
    private Tree tree;
    private static int counter = 0;

    public Task(List<String> s, Decode d) {
        sentence = s;
        decodeInstance = d;
    }

    public Tree getTree(){
        return tree;
    }

    public void run() {
        tree = decodeInstance.decode(sentence);
        synchronized(this){
            counter++;
            if(counter % 10 == 0)
                System.out.println("Finished processing " + counter + " sentences");
        }
       //System.out.println(tree);
    }
}
