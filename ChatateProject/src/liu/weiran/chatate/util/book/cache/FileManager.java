package liu.weiran.chatate.util.book.cache;

import liu.weiran.chatate.util.book.CommonUtil;

public class FileManager {

	public static String getSaveFilePath() {
		if (CommonUtil.hasSDCard()) {
			return CommonUtil.getRootFilePath() + "chatate/Cache/Pictures";
		} else {
			return CommonUtil.getRootFilePath() + "chatate/Cache/Pictures";
		}
	}
}