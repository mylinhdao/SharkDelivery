import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPKeyStore;

import java.util.ArrayList;

public interface DeliveryContract extends MessageContent{
    TransitContract getCurrentTransitContract();

    boolean deliveryContractIsOK(PKIManager pkiManager) throws ASAPSecurityException;
    void addCurrentTransitContractTohistory(TransitContract transitContract);
    ArrayList<TransitContract> getHistoryOfTransitContracts();
    void setCurrentTransitContract(TransitContract currentTransitContract);
    CharSequence getPackageID();

}
