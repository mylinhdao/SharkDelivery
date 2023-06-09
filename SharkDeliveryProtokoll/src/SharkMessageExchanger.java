import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.utils.ASAPSerialization;
import net.sharksystem.pki.SharkPKIComponent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SharkMessageExchanger {
    SharkPKIComponent sharkPKIComponent;
    ASAPPeer asapPeer;
    public SharkMessageExchanger(ASAPPeer asapPeer, SharkPKIComponent sharkPKIComponent){
        this.sharkPKIComponent = sharkPKIComponent;
        this.asapPeer = asapPeer;
    }

    public byte[] decryptMessage(byte[] message) throws IOException, ASAPException {
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
            return new MessageContent(snMessage,receiverArray[0], snSender);
        }
        return null; //exception???
    }

}
