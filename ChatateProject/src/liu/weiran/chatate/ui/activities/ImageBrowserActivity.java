package liu.weiran.chatate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import liu.weiran.chatate.R;
import liu.weiran.chatate.adapters.ChatMessageAdapter;


public class ImageBrowserActivity extends BaseActivity {
	String url;
	String path;
	ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_browser);
		imageView = (ImageView) findViewById(R.id.image_view_browser);
		Intent intent = getIntent();
		url = intent.getStringExtra("url");
		path = intent.getStringExtra("path");
		ChatMessageAdapter.displayImageByUri(imageView, path, url);
	}
}
