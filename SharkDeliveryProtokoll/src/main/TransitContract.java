import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.pki.ASAPCertificate;
import net.sharksystem.asap.pki.ASAPKeyStorage;
import net.sharksystem.pki.SharkPKIComponent;

import java.time.LocalTime;

public class TransitContract   implements MessageContent, Comparable<TransitContract> {
    TransitContractContent content;
    byte[] signatureTransferor;
    byte[] signatureTransferee;



    public TransitContract(TransitContractContent content) {
        this.content = content;
    }

    public static TransitContract createTransitContract(int order, CharSequence packageID, CharSequence transferor, CharSequence transferee, Location location, LocalTime time){
        TransitContractContent contractContent = new TransitContractContent(order,packageID, transferor, transferee, location, time);
        return new TransitContract(contractContent);
    }
    public void setSignatureTransferor(byte[] signatureTransferor) {
        this.signatureTransferor = signatureTransferor;
    }

    public void setSignatureTransferee(byte[] signatureTransferee) {
        this.signatureTransferee = signatureTransferee;
    }

    public boolean signedByTransferor(PKIManager pkiManager) {
        CharSequence transferor = content.transferor;
        ASAPCertificate certificateTransferor = null;
        try {
            certificateTransferor = pkiManager.getCertificateOf(transferor);
        } catch (ASAPSecurityException e) {
            throw new RuntimeException(e);
        }
        if (signatureTransferor != null && certificateTransferor != null) {
            return Utils.verifySignature(Utils.toBytes(content),signatureTransferor, certificateTransferor.getPublicKey());
        }
        return false;
    }
    public boolean signedByTransferee(PKIManager pkiManager) {
        CharSequence transferee = content.transferee;
        ASAPCertificate certificateTransferee = null;
        try {
            certificateTransferee = pkiManager.getCertificateOf(transferee);
        } catch (ASAPSecurityException e) {
            throw new RuntimeException(e);
        }
        if (signatureTransferee != null && certificateTransferee != null) {
            return Utils.verifySignature(Utils.toBytes(content),signatureTransferee, certificateTransferee.getPublicKey());
        }
        return false;
    }
    public boolean signedByBothPartner(PKIManager pkiManager){
        return signedByTransferor(pkiManager) && signedByTransferee(pkiManager);

    }
    public CharSequence getTransferor(){
        return content.transferor;
    }
    public CharSequence getTransferee(){
        return content.transferee;
    }
    public LocalTime getTime(){
        return content.time;
    }
    public Location getLocation(){
        return content.location;
    }
    public int getOrder(){ return content.order;}
    public CharSequence getPackageID(){
        return content.packageID;
    }
    public void sign(ASAPKeyStore asapKeyStore, SharkDeliveryProtocolRole role) throws ASAPSecurityException {
        if(role == SharkDeliveryProtocolRole.TRANSFEROR){
            setSignatureTransferor(ASAPCryptoAlgorithms.sign(Utils.toBytes(content), asapKeyStore));
        }else if(role == SharkDeliveryProtocolRole.TRANSFEREE){
            setSignatureTransferee(ASAPCryptoAlgorithms.sign(Utils.toBytes(content), asapKeyStore));
        }
    }
    public boolean isSameAs(TransitContract otherTransitContract){
        if(otherTransitContract.getOrder() == this.getOrder() &&
        otherTransitContract.getTransferor() == this.getTransferor() &&
        otherTransitContract.getTransferee() == this.getTransferee() &&
        otherTransitContract.getPackageID() == this.getPackageID() &&
        otherTransitContract.getTime() == this.getTime() &&
        otherTransitContract.getLocation() == this.getLocation()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int compareTo(TransitContract compareTransit) {
        int compareage = ((TransitContract)compareTransit).getOrder();

        //  For Ascending order
        return this.getOrder() - compareage;
    }
}
