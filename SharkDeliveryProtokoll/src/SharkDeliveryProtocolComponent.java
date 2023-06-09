import net.sharksystem.ASAPFormats;
import net.sharksystem.SharkComponent;

@ASAPFormats(formats = {SharkDeliveryProtocolComponent.SHARK_DELIVERY_PROTOCOL_FORMAT})
public interface SharkDeliveryProtocolComponent extends SharkComponent {
    String SHARK_DELIVERY_PROTOCOL_FORMAT = "shark/delivery";
    String SHARK_DELIVERY_URI_IDENTIFICATION_CODE = "sn2://identificationcode";
    String SHARK_DELIVERY_URI_DELIVERYCONTRACT_CODE = "sn2://deliverycontract";


}
