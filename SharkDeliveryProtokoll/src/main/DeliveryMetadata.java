import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPKeyStore;

public interface DeliveryMetadata {
    boolean signedByE2ESender(PKIManager pkiManager) throws ASAPSecurityException;

    CharSequence getE2ESender();
    CharSequence getPackageID();
}
