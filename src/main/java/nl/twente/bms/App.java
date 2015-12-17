package nl.twente.bms;

import nl.twente.bms.model.MatchingModel;

/**
 * Test application
 */
public class App {
    public static void main(String[] args) {
        String confFilePath = "Data.xls";
        if(args.length == 1){
            confFilePath = args[0];
        }

        MatchingModel model = new MatchingModel(confFilePath);
        model.solve();
    }
}
