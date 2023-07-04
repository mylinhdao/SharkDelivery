import net.sharksystem.SharkComponent;
import net.sharksystem.SharkComponentFactory;
import net.sharksystem.pki.SharkPKIComponent;

public class SharkHedwigComponentFactory implements SharkComponentFactory {
    private final SharkPKIComponent sharkPKIComponent;
    public SharkHedwigComponentFactory(SharkPKIComponent sharkPKIComponent){
        this.sharkPKIComponent = sharkPKIComponent;
    }
    @Override
    public SharkComponent getComponent() {
        return new SharkHedwigComponentImpl(sharkPKIComponent);
    }
}
