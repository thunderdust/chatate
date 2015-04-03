package liu.weiran.chatate.entity;

import com.avos.avoscloud.AVUser;

public class SortUser {
	private AVUser innerUser;
	private String sortLetters;

	public AVUser getInnerUser() {
		return innerUser;
	}

	public void setInnerUser(AVUser innerUser) {
		this.innerUser = innerUser;
	}

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
}
