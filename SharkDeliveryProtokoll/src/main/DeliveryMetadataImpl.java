import main.DeliveryMetadata;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;

public class DeliveryMetadataImpl implements DeliveryMetadata {
    public DeliveryMetadataImpl(DeliveryMetadataContent content) {
        this.content = content;
    }

    public static DeliveryMetadataImpl createDeliveraMetadata(CharSequence e2eSender, CharSequence e2eReceiver, Location localtion, CharSequence packageID){
        DeliveryMetadataContent  content = new DeliveryMetadataContent(e2eSender, e2eReceiver, localtion, packageID);
        return new DeliveryMetadataImpl(content);
    }
    DeliveryMetadataContent content;
    byte[] signatureByE2ESender;

    @Override
    public boolean signedByE2ESender(PKIManager pkiManager) throws ASAPSecurityException {
        return Utils.verifySignature(Utils.toBytes(content),signatureByE2ESender, pkiManager.getCertificateOf(content.e2eSender).getPublicKey());

    }

    public CharSequence getE2ESender(){
        return content.e2eSender;
    }
    public CharSequence getPackageID(){return content.packageID;}
    public void sign(ASAPKeyStore asapKeyStore) throws ASAPSecurityException {
        signatureByE2ESender = ASAPCryptoAlgorithms.sign(Utils.toBytes(content), asapKeyStore);
    }
}
