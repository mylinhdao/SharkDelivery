public class DeliveryMetadataContent {
    CharSequence e2eSender;
    CharSequence e2eReceiver;
    Location location;
    CharSequence packageID;

    public DeliveryMetadataContent(CharSequence e2eSender, CharSequence e2eReceiver, Location location, CharSequence packageID) {
        this.e2eSender = e2eSender;
        this.e2eReceiver = e2eReceiver;
        this.location = location;
        this.packageID = packageID;
    }
}
