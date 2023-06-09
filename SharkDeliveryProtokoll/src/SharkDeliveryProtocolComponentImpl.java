import net.sharksystem.SharkComponent;
import net.sharksystem.SharkException;
import net.sharksystem.asap.*;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.utils.ASAPSerialization;
import net.sharksystem.pki.SharkPKIComponent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.util.Set;

public class SharkDeliveryProtocolComponentImpl implements SharkComponent, SharkDeliveryProtocolComponent, ASAPMessageReceivedListener {

    ASAPPeer asapPeer;
    SharkPKIComponent sharkPKIComponent;
    IdentificationSession identificationSession;
    SharkDeliveryProtocolRole role;
    boolean readyTostartDeleveryProtocol = false;
    DeliveryContract deliveryContract;
    public SharkDeliveryProtocolComponentImpl(SharkPKIComponent sharkPKIComponent){
        this.sharkPKIComponent = sharkPKIComponent;
    }

    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {
        this.asapPeer=asapPeer;
        this.asapPeer.addASAPMessageReceivedListener(
                SharkDeliveryProtocolComponent.SHARK_DELIVERY_PROTOCOL_FORMAT,
                this);
        try {
            this.asapPeer.getASAPStorage(SHARK_DELIVERY_PROTOCOL_FORMAT).createChannel(SHARK_DELIVERY_URI_IDENTIFICATION_CODE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages, String s, List<ASAPHop> list) throws IOException {
        CharSequence uri = asapMessages.getURI();
        switch (uri.toString()){
            case SHARK_DELIVERY_URI_IDENTIFICATION_CODE:
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
                IdentificationMessageContent cont = identificationSession.answerIdentificationMessage((IdentificationMessageContent) Utils.byteArrayToObject(decryptedMessage.content) );
                if(cont == null){
                    return; //WHEN CONFIRMATION maessage, notify listener? TODO:
                }
                try {
                    sendAndEncryptMessage(cont, decryptedMessage.sender);
                } catch (ASAPException e) {
                    throw new RuntimeException(e);
                }
                break;
            case SHARK_DELIVERY_URI_DELIVERYCONTRACT_CODE:
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
                    DeliveryContract received = (DeliveryContract) Utils.byteArrayToObject(decrytedMsg.content);
                    TransitContract transit= received.getCurrentTransitContract();
                    if(received.proveDeliveryContract()){
                        transit.sign(sharkPKIComponent,SharkDeliveryProtocolRole.TRANSFEREE);
                    }
                    try {
                        sendAndEncryptMessage(transit, transit.getTransferor());
                        deliveryContract.addCurrentTransitContractTohistory();

                        //TODO: save this contract
                    } catch (ASAPException e) {
                        throw new RuntimeException(e);
                    }

                }else if(role == SharkDeliveryProtocolRole.TRANSFEROR){
                    TransitContract transitContract = (TransitContract) Utils.byteArrayToObject(decrytedMsg.content);
                    if(transitContract.signedByBothPartner()){
                        deliveryContract.addCurrentTransitContractTohistory();
                        //TODO: save this deliveryContract
                        //Paketübergabe findet statt
                    }else{
                        //hat nicht geklappt
                    }
                }






        }
    }
    public void createAndSendNewDeliveryContract(CharSequence transferee, CharSequence packageID) throws ASAPException, IOException {
        DeliveryContract lastContract = getDeliveryContractByPackageID(CharSequence packageID);
        DeliveryContract newContract = DeliveryContract.createAndSignDeliveryContractForTransit(lastContract.getMetadata(), transferee,sharkPKIComponent, lastContract.historyOfTransitContracts );
        role = SharkDeliveryProtocolRole.TRANSFEROR;
        this.deliveryContract = newContract;
        sendAndEncryptMessage(deliveryContract, transferee);
    }
    public ASAPChannel getChannel(CharSequence uri) throws IOException {
        try {
            ASAPStorage asapStorage =
                    this.asapPeer.getASAPStorage(SHARK_DELIVERY_PROTOCOL_FORMAT);

            ASAPChannel channel = asapStorage.getChannel(uri);

            return channel;
        } catch (ASAPException e) {
            throw new RuntimeException(e);
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
        this.asapPeer.sendASAPMessage(SharkDeliveryProtocolComponent.SHARK_DELIVERY_PROTOCOL_FORMAT, SharkDeliveryProtocolComponent.SHARK_DELIVERY_URI_IDENTIFICATION_CODE, message);

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

}
