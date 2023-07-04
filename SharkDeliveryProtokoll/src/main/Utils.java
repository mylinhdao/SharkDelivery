import java.io.*;
import java.security.*;
import java.time.LocalTime;

import net.sharksystem.asap.crypto.ASAPCryptoParameterStorage;
import net.sharksystem.asap.crypto.ASAPKeyStore;

public class Utils {
    public static byte[] toBytes(Object object){
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static Object byteArrayToObject(byte[] array){
        try (ByteArrayInputStream bis = new ByteArrayInputStream(array);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Object obj = (Object) ois.readObject();
            return obj;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean verifySignature(byte[] signedContent, byte[] signatureBytes, PublicKey publicKey){
        if(publicKey == null) return false;

        try {
            //Signing algorithm immer fest?

            Signature signature = Signature.getInstance(ASAPCryptoParameterStorage.DEFAULT_ASYMMETRIC_SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey); // init with private key
            signature.update(signedContent); // feed with signed data
            return signature.verify(signatureBytes); // check against signature
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            return false;
        }
    }
    public static Location getMyLocation(){
        return new Location(30,20); // TODO:
    }
    public static LocalTime getLocaltime(){
        return LocalTime.now();
    }
    public static boolean provePackageID(CharSequence packageID){
        return true;
    }
}
