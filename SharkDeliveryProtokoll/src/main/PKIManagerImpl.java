import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.pki.ASAPCertificate;
import net.sharksystem.pki.SharkPKIComponent;

public class PKIManagerImpl implements PKIManager {
    CharSequence caID;
    SharkPKIComponent sharkPKIComponent;
    public PKIManagerImpl(CharSequence caID, SharkPKIComponent sharkPKIComponent) {
        this.caID = caID;
        this.sharkPKIComponent = sharkPKIComponent;
    }

    @Override
    public CharSequence getCertificateAuthority() {
        return this.caID;
    }

    @Override
    public ASAPCertificate getCertificateOf(CharSequence subject) throws ASAPSecurityException {
        return sharkPKIComponent.getCertificateByIssuerAndSubject(caID, subject);
    }
}
