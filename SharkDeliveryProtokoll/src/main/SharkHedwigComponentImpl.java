import main.DeliveryContract;
import main.DeliveryContractImpl;
import main.DeliveryContractStorage;
import net.sharksystem.SharkComponent;
import net.sharksystem.SharkException;
import net.sharksystem.asap.*;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.utils.ASAPSerialization;
import net.sharksystem.pki.SharkPKIComponent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.util.Set;

public class SharkHedwigComponentImpl implements SharkComponent, SharkHedwigComponent, ASAPMessageReceivedListener {
    DeliveryContractStorage contractStorage;
    ASAPPeer asapPeer;
    SharkPKIComponent sharkPKIComponent;
    IdentificationSession identificationSession;
    SharkDeliveryProtocolRole role;
    CharSequence packageID;
    CharSequence myID;
    CharSequence transferee;
    DeliveryContract deliveryContract;
    CharSequence caID = "ImCA";
    PKIManager pkiManager;
    public SharkHedwigComponentImpl(SharkPKIComponent sharkPKIComponent){
        this.sharkPKIComponent = sharkPKIComponent;
    }

    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {
        this.asapPeer=asapPeer;
        this.asapPeer.addASAPMessageReceivedListener(
                SharkHedwigComponent.SHARK_HEDWIG_FORMAT,
                this);
        try {
            this.asapPeer.getASAPStorage(SHARK_HEDWIG_FORMAT).createChannel(SHARK_HEDWIG_URI_IDENTIFICATION_CODE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            myID = this.asapPeer.getASAPStorage(SHARK_HEDWIG_FORMAT).getOwner();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pkiManager = new PKIManagerImpl(caID, sharkPKIComponent);

    }

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages, String s, List<ASAPHop> list) throws IOException {
        CharSequence uri = asapMessages.getURI();
        switch (uri.toString()){
            case SHARK_HEDWIG_URI_IDENTIFICATION_CODE:
                if(this.identificationSession == null){
                    identificationSession = new IdentificationSession();
                }
                byte[] msg = null;
                try {
                    msg = asapMessages.getMessage(0,false); //newest message comes first
                } catch (ASAPException e) {
                    throw new RuntimeException(e); // cannot read from storage
                }
                Message decryptedMessage = null;
                try {
                    decryptedMessage = decryptMessage(msg);
                } catch (ASAPException e) {
                    throw new RuntimeException(e);
                }
                MessageContent cont = identificationSession.answerIdentificationMessage((IdentificationMessageContent) Utils.byteArrayToObject(decryptedMessage.content) );
                if(cont == null){ //Identification done
                    DeliveryContract contract = contractStorage.get(packageID);
                    int transitorder = contract.getHistoryOfTransitContracts().size();
                    TransitContract newTransitContract = TransitContract.createTransitContract(transitorder, packageID, myID, transferee, Utils.getMyLocation(), Utils.getLocaltime());
                    contract.setCurrentTransitContract(newTransitContract);
                    cont = contract;
                }
                try {
                    sendAndEncryptMessage(cont, decryptedMessage.sender);
                } catch (ASAPException e) {
                    throw new RuntimeException(e);
                }
                break;
            case SHARK_HEDWIG_URI_DELIVERY:
                if(!identificationSession.bothPartnerIdentified()){
                    return; //TODO: Throw error???
                }
                try {
                msg = asapMessages.getMessage(0,false); //newest message comes first
            } catch (ASAPException e) {
                throw new RuntimeException(e); // cannot read from storage
            }
                Message decrytedMsg;
                try {
                     decrytedMsg = decryptMessage(msg);
                } catch (ASAPException e) {
                    throw new RuntimeException(e);
                }
                if(role == null){
                    role = SharkDeliveryProtocolRole.TRANSFEREE;
                    DeliveryContract received = (DeliveryContractImpl) Utils.byteArrayToObject(decrytedMsg.content);
                    TransitContract transit= received.getCurrentTransitContract();
                    try {
                        if(received.deliveryContractIsOK(pkiManager)){
                            transit.sign(sharkPKIComponent,SharkDeliveryProtocolRole.TRANSFEREE);
                        }
                    } catch (ASAPSecurityException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        sendAndEncryptMessage(transit, transit.getTransferor());
                        received.addCurrentTransitContractTohistory(transit);
                        contractStorage.save(received);

                    } catch (ASAPException e) {
                        throw new RuntimeException(e);
                    }

                }else if(role == SharkDeliveryProtocolRole.TRANSFEROR){

                    TransitContract transitContract = (TransitContract) Utils.byteArrayToObject(decrytedMsg.content);
                    if(transitContract.signedByBothPartner(pkiManager) && transitContract.isSameAs(deliveryContract.getCurrentTransitContract())){
                        deliveryContract.addCurrentTransitContractTohistory(transitContract);
                        contractStorage.save(deliveryContract);
                        //Paketübergabe findet statt
                    }else{
                        //hat nicht geklappt
                    }
                }






        }
    }
    public void sendAndEncryptMessage(MessageContent messageNotEncrypted, CharSequence receiver) throws IOException, ASAPException {
        Set<CharSequence> recipient = new HashSet<>();
        recipient.add(receiver);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ///// content
        ASAPSerialization.writeByteArray(Utils.toBytes(messageNotEncrypted), baos);
        ///// sender
        ASAPSerialization.writeCharSequenceParameter(this.asapPeer.getPeerID(), baos);
        ///// receiver
        ASAPSerialization.writeCharSequenceSetParameter(recipient, baos);

        byte[] message = baos.toByteArray();
        message = ASAPCryptoAlgorithms.produceEncryptedMessagePackage(
                message,
                receiver,
                sharkPKIComponent);
        this.asapPeer.sendASAPMessage(SharkHedwigComponent.SHARK_HEDWIG_FORMAT, SharkHedwigComponent.SHARK_HEDWIG_URI_IDENTIFICATION_CODE, message);

    }
    public Message decryptMessage(byte[] message) throws IOException, ASAPException {
        ByteArrayInputStream bais = new ByteArrayInputStream(message);
        byte[] tmpMessage = ASAPSerialization.readByteArray(bais);
        bais = new ByteArrayInputStream(tmpMessage);
        ASAPCryptoAlgorithms.EncryptedMessagePackage
                encryptedMessagePackage = ASAPCryptoAlgorithms.parseEncryptedMessagePackage(bais);
        //for me
        if (sharkPKIComponent.isOwner(encryptedMessagePackage.getReceiver())) {
            tmpMessage = ASAPCryptoAlgorithms.decryptPackage(encryptedMessagePackage, sharkPKIComponent);
            bais = new ByteArrayInputStream(tmpMessage);

            ////// content
            byte[] snMessage = ASAPSerialization.readByteArray(bais);
            ////// sender
            String snSender = ASAPSerialization.readCharSequenceParameter(bais);
            ////// recipients
            Set<CharSequence> snReceivers = ASAPSerialization.readCharSequenceSetParameter(bais);
            CharSequence[] receiverArray = (CharSequence[]) snReceivers.toArray();
            return new Message(snMessage,receiverArray[0], snSender);
        }
        return null; //exception???
    }
    public void startIdentificationSession(CharSequence receiver) throws IOException, ASAPException {
        if(!checkCertificateFromPartnerExisting(receiver)){
            //cannot start without publickey of receiver
            return; //exception?
        }
        role = SharkDeliveryProtocolRole.TRANSFEROR;
        identificationSession = new IdentificationSession();
        IdentificationMessageContent message = identificationSession.createRandomCodeMessage();
        sendAndEncryptMessage(message, receiver);
        //if other peer answer, the next messages should be automatically sent.
    }

    public boolean checkCertificateFromPartnerExisting(CharSequence partner){
        try {
            sharkPKIComponent.getPublicKey(partner);
            return true;
        } catch (ASAPSecurityException e) {
            return false;
        }
    }

    @Override
    public void startSession(CharSequence transferee, CharSequence packageID) throws IOException, ASAPException {
        deliveryContract = contractStorage.get(packageID);
        this.transferee = transferee;
        this.packageID = packageID;
        if (deliveryContract == null){
            return; // cannot start a session without the deliverycontract existing.
        }
        startIdentificationSession(transferee);

    }

    @Override
    public void initiateAndSaveDeliveryContract(CharSequence e2eReceiver, CharSequence packageID, Location e2eReceiverLocation) throws ASAPException {
        DeliveryMetadataImpl deliveraMetadata= DeliveryMetadataImpl.createDeliveraMetadata(myID, e2eReceiver, e2eReceiverLocation, packageID);
        deliveraMetadata.sign(sharkPKIComponent);
        DeliveryContractImpl newDeliveryContract = new DeliveryContractImpl(null, new ArrayList<>(), deliveraMetadata);
        contractStorage.save(newDeliveryContract);
    }

    @Override
    public ArrayList<DeliveryContract> getAllDeliveryContract() throws IOException, ClassNotFoundException {
        return contractStorage.getAll();
    }
}
