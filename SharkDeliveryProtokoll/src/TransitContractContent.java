import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalTime;

public class TransitContractContent {
    int order;
    CharSequence packageID;
    CharSequence transferor;
    CharSequence transferee;
    Location location;
    LocalTime time;

    public TransitContractContent(int order, CharSequence packageID, CharSequence transferor, CharSequence transferee, Location location, LocalTime time) {
        this.order = order;
        this.packageID = packageID;
        this.transferor = transferor;
        this.transferee = transferee;
        this.location = location;
        this.time = time;
    }

}
