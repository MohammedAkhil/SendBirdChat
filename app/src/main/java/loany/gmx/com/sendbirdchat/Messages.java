package loany.gmx.com.sendbirdchat;

import com.orm.SugarRecord;

/**
 * Created by akhil on 26/01/17.
 */

public class Messages extends SugarRecord {
    String senderId;
    String receiverId;
    String message;


    public Messages(){

    }

    public Messages(String senderId, String receiverId, String message) {
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }


    public String getMessage(){
        return message;
    }
    public String getSenderId(){
        return senderId;
    }
    public String getReceiverId() {
        return receiverId;
    }
}
