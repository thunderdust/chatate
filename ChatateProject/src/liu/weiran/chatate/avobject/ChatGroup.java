package liu.weiran.chatate.avobject;

import java.util.ArrayList;
import java.util.List;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;

@AVClassName("AVOSRealtimeGroups")
public class ChatGroup extends AVObject {
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_ADMIN = "admin";
	public static final String ATTRIBUTE_MEMBERS = "member";
	
	public List<String> getMembers(){
		@SuppressWarnings("unchecked")
		List<String> memberList = getList(ATTRIBUTE_MEMBERS);
		if (memberList==null){
			memberList = new ArrayList<String>();
		}
		return memberList;
	}
	
	public void setMembers(List<String> members){
		put(ATTRIBUTE_MEMBERS,members);
	}
	
	public String getGroupName(){
		return getString(ATTRIBUTE_NAME);
	}
	
	public void setName(String name){
		put(ATTRIBUTE_NAME,name);
	}
	
	public AVUser getAdmin(){
		return getAVUser(ATTRIBUTE_ADMIN, AVUser.class);
	}
	
	public void setAdmin(AVUser admin){
		put(ATTRIBUTE_ADMIN,admin);
	}
	
	public String getGroupDesc(){
		List<String> members = getMembers();
		int size =  members.size();
		String description = getGroupName()+"("+size+" members"+")";
		return description;
	}
}
