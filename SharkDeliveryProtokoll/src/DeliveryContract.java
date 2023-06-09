import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPKeyStore;

import java.util.ArrayList;
import java.util.Collections;

public class DeliveryContract implements MessageContent{
    DeliveryMetadata metadata;
    TransitContract currentTransitContract;

    public DeliveryMetadata getMetadata() {
        return metadata;
    }

    public TransitContract getCurrentTransitContract() {
        return currentTransitContract;
    }

    public ArrayList<TransitContract> getHistoryOfTransitContracts() {
        return historyOfTransitContracts;
    }

    ArrayList<TransitContract> historyOfTransitContracts; // the certificates are in transitcontract.
    // Why? Because a peer can change their keypair after it signed a transit. So the next transit need other certificate.
    public DeliveryContract(TransitContract currentTransitContract, ArrayList<TransitContract> historyOfTransitContracts,DeliveryMetadata deliveryMetadata) {
        this.currentTransitContract = currentTransitContract;
        this.historyOfTransitContracts = historyOfTransitContracts;
        this.metadata = deliveryMetadata;
    }
    public boolean proveHistoryOfTransitContracts(){ //Annahme: metdadata geprüft
        TransitContract provingContract;
        CharSequence lastTransit = metadata.getE2ESender();
        Collections.sort(historyOfTransitContracts);
        for(int i = 0; i<historyOfTransitContracts.size(); i++){
            provingContract = historyOfTransitContracts.get(i);
            if(i != historyOfTransitContracts.get(i).getOrder()){//der Array muss geordnet sein
                return false;
            }
            if(!provingContract.signedByBothPartner() || lastTransit != provingContract.getTransferor()){ //von beide Seiten signiert und trasferor ist der letzte transferee
                return false;
                }
            lastTransit = provingContract.getTransferee();
            if(provingContract.getPackageID() != metadata.getPackageID()){
                return false;
            }
            }

        return true;
    }
    public boolean proveCurrentTransit(SharkDeliveryProtocolRole role){
        //TODO: prove time and location and info on transitContract
        //TODO: prove certificate
        if(currentTransitContract.getPackageID() != metadata.getPackageID()){
            return false;
        }
        if(role == SharkDeliveryProtocolRole.TRANSFEROR){ //prüft, nachdem von Trensferee unterschirieben wurde
            return currentTransitContract.signedByBothPartner();
        }else{ //prüft, ob von transferor signiert und transferor ist letzte transferee.
            return (currentTransitContract.getTransferor() == historyOfTransitContracts.get(historyOfTransitContracts.size()-1).getTransferee()
            && currentTransitContract.signedByTransferor());
        }

    }
    public boolean proveDeliveryContract(){ //only Transferee use this
            boolean metadataProved = metadata.signedByOwnerOfCertificate(historyOfTransitContracts.get(0).certificateTransferor);
            boolean historyProved = proveHistoryOfTransitContracts();
            boolean currentTransitProved = proveCurrentTransit(SharkDeliveryProtocolRole.TRANSFEREE);
            boolean packageIDProved = Utils.provePackageID(metadata.getPackageID()); //look at the package if it has the right packageID
            return metadataProved && historyProved && currentTransitProved && packageIDProved;

    }
    public static DeliveryContract initiateDeliveryContract(CharSequence e2eReceiver,
                                                            CharSequence packageID, Location e2eReceiverLocation,
                                                            CharSequence transferee, ASAPKeyStore asapKeyStore) throws ASAPSecurityException {
        DeliveryMetadata deliveraMetadata= DeliveryMetadata.createDeliveraMetadata(asapKeyStore.getOwner(), e2eReceiver, e2eReceiverLocation, packageID);
        deliveraMetadata.sign(asapKeyStore);
        TransitContract newTransitContract = TransitContract.createTransitContract(0, packageID,asapKeyStore.getOwner(), transferee, Utils.getMyLocation(), Utils.getLocaltime() );
        newTransitContract.sign(asapKeyStore, SharkDeliveryProtocolRole.TRANSFEROR);
        return new DeliveryContract(newTransitContract, new ArrayList<TransitContract>(), deliveraMetadata);
    }
    public static DeliveryContract createAndSignDeliveryContractForTransit(DeliveryMetadata deliveryMetadata, CharSequence transferee, ASAPKeyStore asapKeyStore, ArrayList<TransitContract> history) throws ASAPSecurityException {
        TransitContract newTransitContract = TransitContract.createTransitContract(history.size(), deliveryMetadata.getPackageID(),asapKeyStore.getOwner(), transferee, Utils.getMyLocation(), Utils.getLocaltime() );
        newTransitContract.sign(asapKeyStore, SharkDeliveryProtocolRole.TRANSFEROR);
        return new DeliveryContract(newTransitContract, history,deliveryMetadata);
    }
}
