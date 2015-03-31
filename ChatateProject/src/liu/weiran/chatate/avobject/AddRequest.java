package liu.weiran.chatate.avobject;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;

@AVClassName("AddRequest")
public class AddRequest extends AVObject {

	public static final int STATUS_PENDING = 0;
	public static final int STATUS_DONE = 1;
	public static final String FROM_USER = "sender";
	public static final String TO_USER = "receiver";
	public static final String STATUS = "status";

	AVUser requestReceiver;
	AVUser requestSender;

	/* A compulsory constructor with no parameters as required by AVOS */
	public AddRequest() {
	}

	public synchronized AVUser getRequestSender() {
		if (requestSender != null) {
			return requestSender;
		} else {
			return getAVUser(FROM_USER, AVUser.class);
		}
	}

	public void setRequestSender(AVUser requestSender) {
		put(FROM_USER, requestSender);
	}

	public synchronized AVUser getRequestReceiver() {
		if (requestReceiver != null) {
			return requestReceiver;
		} else {
			return getAVUser(TO_USER, AVUser.class);
		}
	}

	public void setRequestReceiver(AVUser requestReceiver) {
		put(TO_USER, requestReceiver);
	}

	public int getStatus() {
		return getInt(STATUS);
	}

	public void setStatus(int status) {
		put(STATUS, status);
	}

}
