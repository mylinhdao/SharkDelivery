import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.pki.ASAPCertificate;

import java.time.LocalTime;

public class TransitContract implements Comparable<TransitContract>, MessageContent{
    TransitContractContent content;
    byte[] signatureTransferor;
    byte[] signatureTransferee;
    ASAPCertificate certificateTransferor;
    ASAPCertificate certificateTransferee;


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

    public void setGetSignatureTransferee(byte[] signatureTransferee) {
        this.signatureTransferee = signatureTransferee;
    }
    public boolean signedByTransferor() { //TODO: folgende 2 Methoden: verify certificate and compare subjektid with transferorid
        if(content.transferor != certificateTransferor.getSubjectID()){
            return false;
        }
        if (signatureTransferor != null && certificateTransferor != null) {

            return Utils.verifySignature(Utils.toBytes(content),signatureTransferor, certificateTransferor.getPublicKey());
        }
        return false;
    }
    public boolean signedByTransferee() {
        if(content.transferee != certificateTransferee.getSubjectID()){
            return false;
        }
        if (signatureTransferee != null && certificateTransferee != null) {
            return Utils.verifySignature(Utils.toBytes(content),signatureTransferee, certificateTransferee.getPublicKey());
        }
        return false;
    }
    public boolean signedByBothPartner(){
        return signedByTransferor() && signedByTransferee();

    }
    public CharSequence getTransferor(){
        return content.transferor;
    }
    public CharSequence getTransferee(){
        return content.transferee;
    }

    public void setCertificateTransferor(ASAPCertificate certificateTransferor) {
        this.certificateTransferor = certificateTransferor;
    }

    public void setCertificateTransferee(ASAPCertificate certificateTransferee) {
        this.certificateTransferee = certificateTransferee;
    }

    public int getOrder(){ return content.order;}
    public CharSequence getPackageID(){
        return content.packageID;
    }
    public void sign(ASAPKeyStore asapKeyStore, SharkDeliveryProtocolRole role) throws ASAPSecurityException {
        if(role == SharkDeliveryProtocolRole.TRANSFEROR){
            setSignatureTransferor(ASAPCryptoAlgorithms.sign(Utils.toBytes(content), asapKeyStore));
            setCertificateTransferor(.........);
        }else{
            setSignatureTransferee(ASAPCryptoAlgorithms.sign(Utils.toBytes(content), asapKeyStore));
            setCertificateTransferee(............);
        }
    }

    @Override
    public int compareTo(TransitContract compareTransit) {
        int compareage = ((TransitContract)compareTransit).getOrder();

        //  For Ascending order
        return this.getOrder() - compareage;
    }
}
