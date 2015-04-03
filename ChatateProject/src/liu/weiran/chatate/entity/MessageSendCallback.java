package liu.weiran.chatate.entity;

public interface MessageSendCallback {
	void onError(Exception e);

	void onStart(Message msg);

	void onSuccess(Message msg);
}