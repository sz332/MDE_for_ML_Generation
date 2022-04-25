package at.vres.master.mdml;

import at.vres.master.mdml.decomposition.MLInformationHolder;
import at.vres.master.mdml.decomposition.ModelDecompositionHandler;
import at.vres.master.mdml.tbcg.DocOnceHandler;
import at.vres.master.mdml.tbcg.VelocityTest;

import java.util.Map;

public class TestMain {
    private static final String TEST_MODEL = "src/main/resources/UC1_Weather/UC1_Weather.uml";


    public static void main(String[] args) {
        Map<String, MLInformationHolder> stringMLInformationHolderMap = ModelDecompositionHandler.doExtraction(TEST_MODEL);
        String s = VelocityTest.generateFromExtractedInformation(stringMLInformationHolderMap, "test.vm");
        if(!s.isBlank()) {
            DocOnceHandler.doDotOnceGeneration(s, "dotFiles/test.do.txt");
        }
        //VelocityTest.velTestRun("test.vm");
        //VelocityTest.generateFromJSON("mappings/test.json");
    }


}
