package liu.weiran.chatate.avobject;

import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUser;

public class User {

	public static final String USER_NAME = "username";
	public static final String PASSWORD = "password";
	public static final String AVATAR = "avatar";
	public static final String FRIENDS = "friends";
	public static final String LOCATION = "location";
	public static final String GENDER = "gender";
	public static final String INSTALLATION = "installation";
	public static String[] genderDescriptions = { "Male", "Female" };

	public static String getCurrentUserID() {
		AVUser currentUser = AVUser.getCurrentUser();
		if (currentUser != null) {
			return currentUser.getObjectId();
		} else {
			return null;
		}
	}

	public static enum Gender {
		Male(0), Female(1);
		int genderValue;

		Gender(int value) {
			this.genderValue = value;
		}

		public int getGenderValue() {
			return genderValue;
		}

		public static Gender getGenderFromInt(int index) {
			// Make sure gender index is not out of bound
			if (index >= 0 && index < 2)
				return values()[index];
			else
				return values()[0];
		}
	}

	public static String getAvatarUrl(AVUser user) {
		AVFile avatar = user.getAVFile(AVATAR);
		if (avatar != null) {
			return avatar.getUrl();
		} else {
			return null;
		}
	}

	public static Gender getGender(AVUser user) {
		int genderIndex = user.getInt(GENDER);
		return Gender.getGenderFromInt(genderIndex);
	}

	public static void setGender(AVUser user, Gender g) {
		user.put(GENDER, g.getGenderValue());
	}

	public static String getGenderDescription(AVUser user) {
		int genderIndex = getGender(user).getGenderValue();
		return genderDescriptions[genderIndex];
	}

	public static AVInstallation getInstallation(AVUser user) {
		try {
			return user.getAVObject(INSTALLATION, AVInstallation.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
