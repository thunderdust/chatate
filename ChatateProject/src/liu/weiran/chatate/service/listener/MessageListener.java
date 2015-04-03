package liu.weiran.chatate.service.listener;

public interface MessageListener {
	public boolean onMessageUpdate(String otherId);// if true, will not pass to
													// next listener
}
