import java.util.Arrays;
import java.util.Random;

public class IdentificationSession{
    byte[] randomCodeForCommunicationPartner;
    boolean partnerIdentified = false;
    boolean identifiedByPartner = false;


    public IdentificationMessageContent createRandomCodeMessage(){
        randomCodeForCommunicationPartner = createRandomcode();
        return new IdentificationMessageContent(IdentificationMessageType.REQUEST, randomCodeForCommunicationPartner,null );
    }
    private byte[] createRandomcode(){
        byte[] array = new byte[7];
        new Random().nextBytes(array);
        return array;
    }
    public boolean bothPartnerIdentified(){
        return partnerIdentified && identifiedByPartner;
    }
    public IdentificationMessageContent answerIdentificationMessage(IdentificationMessageContent identificationMessage) {
        IdentificationMessageType type = identificationMessage.getType();
        byte[] partnerCode = identificationMessage.getMyRandomCode();

        switch (type) {
            case REQUEST:
                byte[] array = createRandomcode();
                return new IdentificationMessageContent(IdentificationMessageType.ANSWER_REQUEST, array, partnerCode);
            case ANSWER_REQUEST:
                if (Arrays.equals(identificationMessage.getPartnerRandomCode(), this.randomCodeForCommunicationPartner)) {
                    partnerIdentified = true;
                    return new IdentificationMessageContent(IdentificationMessageType.CONFIRMATION, null, partnerCode);
                } else {
                    //failed, what to do?
                }
                break;
            case ANSWER:
                if (Arrays.equals(identificationMessage.getPartnerRandomCode(), this.randomCodeForCommunicationPartner)) {
                    partnerIdentified = true;
                    return new IdentificationMessageContent(IdentificationMessageType.CONFIRMATION, null, null);

                }else{
                    //failed, what to do?
                }
                break;
            case CONFIRMATION:
                identifiedByPartner = true;
                break;

        }
        return null;
    }
}
