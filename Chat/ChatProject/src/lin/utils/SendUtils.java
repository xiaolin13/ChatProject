package lin.utils;

import java.io.DataOutputStream;
import java.io.IOException;

public class SendUtils {
	
	public static void sendData(DataOutputStream dos, String msg) {
		try {
			dos.writeUTF(msg);
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
