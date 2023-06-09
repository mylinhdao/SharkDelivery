import net.sharksystem.SharkComponent;
import net.sharksystem.SharkComponentFactory;
import net.sharksystem.pki.SharkPKIComponent;

public class SharkDeliveryProtocolComponentFactory implements SharkComponentFactory {
    private final SharkPKIComponent sharkPKIComponent;
    public SharkDeliveryProtocolComponentFactory(SharkPKIComponent sharkPKIComponent){
        this.sharkPKIComponent = sharkPKIComponent;
    }
    @Override
    public SharkComponent getComponent() {
        return new SharkDeliveryProtocolComponentImpl(sharkPKIComponent);
    }
}
