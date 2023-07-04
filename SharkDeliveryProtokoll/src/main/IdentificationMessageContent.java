import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class IdentificationMessageContent implements MessageContent {
    private IdentificationMessageType type;
    private byte[] myRandomCode;
    private byte[] partnerRandomCode;

    public IdentificationMessageContent(IdentificationMessageType type, byte[] myRandomCode, byte[] partnerRandomCode) {
        super();
        this.myRandomCode = myRandomCode;
        this.partnerRandomCode = partnerRandomCode;
        this.type = type;
    }

    public byte[] getPartnerRandomCode() {
        return partnerRandomCode;
    }

    public void setMyRandomCode(byte[] myRandomCode) {
        this.myRandomCode = myRandomCode;
    }

    public void setPartnerRandomCode(byte[] partnerRandomCode) {
        this.partnerRandomCode = partnerRandomCode;
    }

    public IdentificationMessageType getType() {
        return type;
    }
    public byte[] getMyRandomCode(){
        return myRandomCode;
    }

}
