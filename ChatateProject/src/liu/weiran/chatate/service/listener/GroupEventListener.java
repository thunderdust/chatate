package liu.weiran.chatate.service.listener;

import com.avos.avoscloud.Group;

//group member events
public interface GroupEventListener {

	void onJoined(Group group);

	void onMemberUpdate(Group group);

	void onQuit(Group group);

}
