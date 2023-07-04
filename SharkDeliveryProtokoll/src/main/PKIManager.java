import net.sharksystem.asap.ASAP;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.pki.ASAPCertificate;

public interface PKIManager {
    CharSequence getCertificateAuthority();
    ASAPCertificate getCertificateOf(CharSequence subject) throws ASAPSecurityException;
}
