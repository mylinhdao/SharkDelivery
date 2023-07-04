import java.io.*;
import java.util.ArrayList;

public class DeliveryContractStorageImpl implements DeliveryContractStorage{

    @Override
    public void save(DeliveryContract deliveryContract) {
        boolean contractAlreadyExists = false;
        ArrayList<DeliveryContract> contracts;
        try {
             contracts = getAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(int i = 0; i<contracts.size(); i++){
            if(deliveryContract.getPackageID() == contracts.get(i).getPackageID()){
                contracts.set(i, deliveryContract);
                contractAlreadyExists = true;
            }
        }
        if(!contractAlreadyExists){
            contracts.add(deliveryContract);
        }
        FileOutputStream inFile = null;
        try {
            inFile = new FileOutputStream(DELIVERYCONTRACT_STORAGE);
            ObjectOutputStream inStream = new ObjectOutputStream(inFile);
            inStream.writeObject(contracts);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public DeliveryContractImpl get(CharSequence packageID) {
        return null;
    }

    @Override
    public ArrayList<DeliveryContract> getAll() throws IOException{
        FileInputStream inFile = new FileInputStream(DELIVERYCONTRACT_STORAGE);
        ObjectInputStream inStream = new ObjectInputStream(inFile);
        ArrayList<DeliveryContract> contracts;
        try {
             contracts = (ArrayList<DeliveryContract>)inStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return contracts;
    }
}
