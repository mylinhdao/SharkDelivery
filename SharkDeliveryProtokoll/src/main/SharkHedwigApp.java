import net.sharksystem.SharkException;
import net.sharksystem.SharkPeerFS;
import net.sharksystem.pki.*;

public class SharkHedwigApp {
    private final SharkPeerFS sharkPeerFS;

    private static final CharSequence ROOTFOLDER = "sharkDeliveryprotocolDataStorage";
    private final SharkPKIComponent pkiComponent;
    private final SharkHedwigComponent deliveryProtocolComponent;
    SharkHedwigApp(String peerName) throws SharkException {
        this.sharkPeerFS = new SharkPeerFS(peerName, ROOTFOLDER + "/" + peerName);
        //setting SharkPKI and SharkmessagerComponent
        SharkPKIComponentFactory pkiComponentFactory = new SharkPKIComponentFactory();
        this.sharkPeerFS.addComponent(pkiComponentFactory, SharkPKIComponent.class);
         SharkHedwigComponentFactory sharkDeliveryProtocolComponentFactory = new SharkHedwigComponentFactory(
                (SharkPKIComponent) sharkPeerFS.getComponent(SharkPKIComponent.class));
        this.sharkPeerFS.addComponent(sharkDeliveryProtocolComponentFactory, SharkHedwigComponent.class);
        this.sharkPeerFS.start();

        this.pkiComponent = (SharkPKIComponent) this.sharkPeerFS.getComponent(SharkPKIComponent.class);
        this.deliveryProtocolComponent = (SharkHedwigComponent) this.sharkPeerFS.getComponent(SharkHedwigComponent.class);

    }

}
