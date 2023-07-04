import java.io.IOException;
import java.util.ArrayList;

public interface DeliveryContractStorage {
    String DELIVERYCONTRACT_STORAGE = "contracts.txt";
    /** Save a delivery contract in the storage. **/
    void save(DeliveryContract deliveryContract);
    
    /** Save a transit contract in the storage. It belongs to the stored delivery contract with the same packageID**/

    /** get the delivery contract with ID*/
    DeliveryContractImpl get(CharSequence packageID);

    ArrayList<DeliveryContract> getAll() throws IOException, ClassNotFoundException;
}
