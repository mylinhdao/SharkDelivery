import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPKeyStore;

import java.util.ArrayList;
import java.util.Collections;

public class DeliveryContractImpl implements DeliveryContract, MessageContent{
    DeliveryMetadataImpl metadata;
    TransitContract currentTransitContract;
    ArrayList<TransitContract> historyOfTransitContracts; // the certificates are in transitcontract.

    public DeliveryMetadataImpl getMetadata() {
        return metadata;
    }
    @Override
    public TransitContract getCurrentTransitContract() {
        return currentTransitContract;
    }


    @Override
    public ArrayList<TransitContract> getHistoryOfTransitContracts() {
        return historyOfTransitContracts;
    }
    @Override
    public void setCurrentTransitContract(TransitContract currentTransitContract){
        this.currentTransitContract = currentTransitContract;
    }

    @Override
    public CharSequence getPackageID() {
        return this.getMetadata().getPackageID();
    }

    // Why? Because a peer can change their keypair after it signed a transit. So the next transit need other certificate.
    public DeliveryContractImpl(TransitContract currentTransitContract, ArrayList<TransitContract> historyOfTransitContracts, DeliveryMetadataImpl deliveryMetadata) {
        this.currentTransitContract = currentTransitContract;
        this.historyOfTransitContracts = historyOfTransitContracts;
        this.metadata = deliveryMetadata;
    }
    public boolean proveHistoryOfTransitContracts(PKIManager pkiManager){ //Annahme: metdadata geprüft
        TransitContract provingContract;
        CharSequence lastTransit = metadata.getE2ESender();
        Collections.sort(historyOfTransitContracts);
        for(int i = 0; i<historyOfTransitContracts.size(); i++){
            provingContract = historyOfTransitContracts.get(i);
            if(i != historyOfTransitContracts.get(i).getOrder()){//der Array muss geordnet sein
                return false;
            }
            if(!provingContract.signedByBothPartner(pkiManager) || lastTransit != provingContract.getTransferor()){ //von beide Seiten signiert und trasferor ist der letzte transferee
                return false;
                }
            lastTransit = provingContract.getTransferee();
            if(provingContract.getPackageID() != metadata.getPackageID()){
                return false;
            }
            }

        return true;
    }
    public boolean proveCurrentTransit(SharkDeliveryProtocolRole role, PKIManager pkiManager){
        //TODO: prove time and location and info on transitContract
        if(currentTransitContract.getPackageID() != metadata.getPackageID()){
            return false;
        }
        if(role == SharkDeliveryProtocolRole.TRANSFEROR){ //prüft, nachdem von Trensferee unterschirieben wurde
            return currentTransitContract.signedByBothPartner(pkiManager);
        }else{ //prüft, ob transit contract von transferor signiert ist. Ist transferor der transferee im vorherige transsit?.
            return (currentTransitContract.getTransferor() == historyOfTransitContracts.get(historyOfTransitContracts.size()-1).getTransferee()
            && currentTransitContract.signedByTransferor(pkiManager));
        }

    }
    @Override
    public boolean deliveryContractIsOK(PKIManager pkiManager) throws ASAPSecurityException { //only Transferee use this
            boolean metadataProved = metadata.signedByE2ESender(pkiManager);
            boolean historyProved = proveHistoryOfTransitContracts(pkiManager);
            boolean currentTransitProved = proveCurrentTransit(SharkDeliveryProtocolRole.TRANSFEREE, pkiManager);
            boolean packageIDProved = Utils.provePackageID(metadata.getPackageID()); //look at the package if it has the right packageID
            return metadataProved && historyProved && currentTransitProved && packageIDProved;

    }


    @Override
    public void addCurrentTransitContractTohistory(TransitContract transitContract) {
        historyOfTransitContracts.add(transitContract);
        currentTransitContract = null;
    }
}
