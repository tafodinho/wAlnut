package model.MARK_II;

import model.util.JsonFileInputOutput;
import com.google.gson.Gson;
import model.MARK_II.ColumnPosition;
import model.theory.MemoryClassifier;
import model.theory.Memory;
import java.util.Set;
import model.theory.Idea;
import model.MARK_II.SpatialPooler;
import java.io.IOException;
import model.MARK_II.Neocortex;
import model.MARK_II.Region;
import model.MARK_II.VisionCell;
import model.Retina;
import model.MARK_II.ConnectTypes.SensorCellsToRegionConnect;
import model.MARK_II.ConnectTypes.SensorCellsToRegionRectangleConnect;
import model.NervousSystem;
import model.LateralGeniculateNucleus;
import model.MARK_II.ConnectTypes.RegionToRegionRectangleConnect;
import model.MARK_II.ConnectTypes.RegionToRegionConnect;

/**
 * @author Quinn Liu (quinnliu@vt.edu)
 * @version MARK II | June 24, 2013
 */
public class HowToUseMARK_II extends junit.framework.TestCase {
    private NervousSystem nervousSystem;
    private MemoryClassifier digitsSVM;
    Gson gson = new Gson();

    public void setUp() throws IOException {
	this.nervousSystem = this.constructConnectedNervousSystem();
	this.digitsSVM = this.trainMemoryClassifierWithNervousSystem();
    }

    private NervousSystem constructConnectedNervousSystem() {
	// construct Neocortex with just V1
	Region rootRegionOfNeocortex = new Region("V1", 4, 4, 4, 50, 3);
	RegionToRegionConnect neocortexConnectType = new RegionToRegionRectangleConnect();
	Neocortex unconnectedNeocortex = new Neocortex(rootRegionOfNeocortex,
		neocortexConnectType);

	// construct LGN
	Region LGNRegion = new Region("LGN", 8, 8, 1, 50, 3);
	LateralGeniculateNucleus unconnectedLGN = new LateralGeniculateNucleus(
		LGNRegion);

	// construct Retina
	VisionCell[][] visionCells = new VisionCell[65][65];
	for (int x = 0; x < visionCells.length; x++) {
	    for (int y = 0; y < visionCells[0].length; y++) {
		visionCells[x][y] = new VisionCell();
	    }
	}
	Retina unconnectedRetina = new Retina(visionCells);

	// construct 1 object of NervousSystem to encapsulate all classes in
	// MARK II
	NervousSystem nervousSystem = new NervousSystem(unconnectedNeocortex,
		unconnectedLGN, unconnectedRetina);

	// connect Retina to LGN
	Retina retina = nervousSystem.getPNS().getSNS().getRetina();

	LateralGeniculateNucleus LGN = nervousSystem.getCNS().getBrain()
		.getThalamus().getLGN();

	SensorCellsToRegionConnect retinaToLGN = new SensorCellsToRegionRectangleConnect();
	retinaToLGN.connect(retina.getVisionCells(), LGN.getRegion(), 0, 0);

	// connect LGN to V1 Region of Neocortex
	Neocortex neocortex = nervousSystem.getCNS().getBrain().getCerebrum()
		.getCerebralCortex().getNeocortex();

	RegionToRegionConnect LGNToV1 = new RegionToRegionRectangleConnect();
	LGNToV1.connect(LGN.getRegion(), neocortex.getCurrentRegion(), 0, 0);

	return nervousSystem;
    }

    private MemoryClassifier trainMemoryClassifierWithNervousSystem()
	    throws IOException {
	Retina retina = nervousSystem.getPNS().getSNS().getRetina();

	Region LGNStructure = nervousSystem.getCNS().getBrain().getThalamus()
		.getLGN().getRegion();

	// Region V1 = nervousSystem.getCNS().getBrain().getCerebrum()
	// .getCerebralCortex().getNeocortex().getCurrentRegion();

	// -------------train NervousSystem update Memory----------------
	retina.seeBMPImage("2.bmp");

	SpatialPooler spatialPooler = new SpatialPooler(LGNStructure);
	spatialPooler.setLearningState(true);
	Set<ColumnPosition> LGNNeuronActivity = spatialPooler
		.performSpatialPoolingOnRegion();

	assertEquals(11, LGNNeuronActivity.size());

	Idea twoIdea = new Idea("two");
	twoIdea.unionColumnPositions(LGNNeuronActivity);

	Memory digitsMemory = new Memory();
	digitsMemory.addNewIdea(twoIdea);

	// TODO: train LGNStructure on many more different images of 2's

	MemoryClassifier digitsSVM = new MemoryClassifier(digitsMemory);

	// save MemoryClassifier object as a JSON file
	String myObjectJson = this.gson.toJson(digitsSVM);
	JsonFileInputOutput.saveObjectToTextFile(myObjectJson,
		"./train/model/MARK_II/MemoryClassifier_Digits.txt");

	return digitsSVM;
    }

    public void test_MemoryClassifierOnNewImages() throws IOException {
	String memoryClassifierAsString = JsonFileInputOutput
		.openObjectInTextFile("./train/model/MARK_II/MemoryClassifier_Digits.txt");
	MemoryClassifier mc = this.gson.fromJson(memoryClassifierAsString,
		MemoryClassifier.class);
	System.out.println(mc.toString());

	Retina retina = nervousSystem.getPNS().getSNS().getRetina();

	Region LGNStructure = nervousSystem.getCNS().getBrain().getThalamus()
		.getLGN().getRegion();

	// retina.seeBMPImage("new2.bmp");
	// digitsSVM.updateIdeas(spatialPooler.performSpatialPoolingOnRegion());
	// digitsSVM.toString();
    }
}
