import net.sharksystem.SharkException;
import net.sharksystem.SharkPeerFS;
import net.sharksystem.pki.*;

public class SharkDeliveryProtocolApp {
    private final SharkPeerFS sharkPeerFS;

    private static final CharSequence ROOTFOLDER = "sharkDeliveryprotocolDataStorage";
    private final SharkPKIComponent pkiComponent;
    private final SharkDeliveryProtocolComponent deliveryProtocolComponent;
    SharkDeliveryProtocolApp(String peerName) throws SharkException {
        this.sharkPeerFS = new SharkPeerFS(peerName, ROOTFOLDER + "/" + peerName);
        //setting SharkPKI and SharkmessagerComponent
        SharkPKIComponentFactory pkiComponentFactory = new SharkPKIComponentFactory();
        this.sharkPeerFS.addComponent(pkiComponentFactory, SharkPKIComponent.class);
         SharkDeliveryProtocolComponentFactory sharkDeliveryProtocolComponentFactory = new SharkDeliveryProtocolComponentFactory(
                (SharkPKIComponent) sharkPeerFS.getComponent(SharkPKIComponent.class));
        this.sharkPeerFS.addComponent(sharkDeliveryProtocolComponentFactory, SharkDeliveryProtocolComponent.class);
        this.sharkPeerFS.start();

        // get component to add listener. Question: CredentialReceivedlistener ncot implemented
        this.pkiComponent = (SharkPKIComponent) this.sharkPeerFS.getComponent(SharkPKIComponent.class);
        //dont know if we need listener for deliveryprotocol
        this.deliveryProtocolComponent = (SharkDeliveryProtocolComponent) this.sharkPeerFS.getComponent(SharkDeliveryProtocolComponent.class);

    }

}
