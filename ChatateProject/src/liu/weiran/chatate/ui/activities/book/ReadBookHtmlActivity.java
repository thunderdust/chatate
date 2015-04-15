package liu.weiran.chatate.ui.activities.book;

public class ReadBookHtmlActivity {

	public static class AnnotationFragment {

		// create an object of SingleObject
		private static final AnnotationFragment instance = new AnnotationFragment();

		// make the constructor private so that this class cannot be
		// instantiated
		private AnnotationFragment() {
		}

		// Get the only object available
		public static AnnotationFragment getInstance() {
			return instance;
		}
	}

}
