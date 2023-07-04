import main.DeliveryContract;
import net.sharksystem.ASAPFormats;
import net.sharksystem.SharkComponent;
import net.sharksystem.asap.ASAPException;

import java.io.IOException;
import java.util.ArrayList;

@ASAPFormats(formats = {SharkHedwigComponent.SHARK_HEDWIG_FORMAT})
public interface SharkHedwigComponent extends SharkComponent {
    String SHARK_HEDWIG_FORMAT = "shark/hedwig";
    String SHARK_HEDWIG_URI_IDENTIFICATION_CODE = "sn2://identificationcode";
    String SHARK_HEDWIG_URI_DELIVERY = "sn2://delivery";
    CharSequence caID = "caID";
    /** start the contract exchange session. 1. Identification 2. Delivery contract exchange. Every step runs automatically */
    void startSession(CharSequence transferee, CharSequence packageID) throws IOException, ASAPException;
    /** create Delivery contract for a package. This happens before any contract exchange*/

    void initiateAndSaveDeliveryContract(CharSequence e2eReceiver, CharSequence packageID, Location e2eReceiverLocation) throws ASAPException;

    /** return all Delivery contract in storage*/
    ArrayList<DeliveryContract> getAllDeliveryContract() throws IOException, ClassNotFoundException;
}
