import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.pki.ASAPCertificate;

import java.security.PublicKey;

public class DeliveryMetadata {
    public DeliveryMetadata(DeliveryMetadataContent content) {
        this.content = content;
    }

    public static DeliveryMetadata createDeliveraMetadata(CharSequence e2eSender, CharSequence e2eReceiver, Location localtion, CharSequence packageID){
        DeliveryMetadataContent  content = new DeliveryMetadataContent(e2eSender, e2eReceiver, localtion, packageID);
        return new DeliveryMetadata(content);
    }
    DeliveryMetadataContent content;
    byte[] signatureByE2ESender;
    public boolean signedByOwnerOfCertificate(ASAPCertificate e2eSenderCertificate) {
        return Utils.verifySignature(Utils.toBytes(content),signatureByE2ESender, e2eSenderCertificate.getPublicKey());

    }
    public CharSequence getE2ESender(){
        return content.e2eSender;
    }
    public CharSequence getPackageID(){return content.packageID;}
    public void signDeliveryMetadata(ASAPKeyStore asapKeyStore) throws ASAPSecurityException {
        signatureByE2ESender = ASAPCryptoAlgorithms.sign(Utils.toBytes(content), asapKeyStore);

    }
    public void sign(ASAPKeyStore asapKeyStore) throws ASAPSecurityException {
        signatureByE2ESender = ASAPCryptoAlgorithms.sign(Utils.toBytes(content), asapKeyStore);
    }
}
