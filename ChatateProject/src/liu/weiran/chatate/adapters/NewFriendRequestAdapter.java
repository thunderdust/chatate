package liu.weiran.chatate.adapters;

import java.util.List;

import liu.weiran.chatate.R;
import liu.weiran.chatate.avobject.AddRequest;
import liu.weiran.chatate.service.CloudService;
import liu.weiran.chatate.service.UserService;
import liu.weiran.chatate.ui.views.ViewHolder;
import liu.weiran.chatate.util.NetworkAsyncTask;
import liu.weiran.chatate.util.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class NewFriendRequestAdapter extends BaseListAdapter<AddRequest> {

	public NewFriendRequestAdapter(Context context, List<AddRequest> list) {
		super(context, list);
	}

	@Override
	public View getView(int position, View conView, ViewGroup parent) {
		if (conView == null) {
			LayoutInflater mInflater = LayoutInflater.from(ctx);
			conView = mInflater.inflate(R.layout.add_friend_item, null);
		}
		final AddRequest addRequest = items.get(position);
		TextView nameView = ViewHolder.findViewById(conView, R.id.name);
		ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
		final Button addBtn = ViewHolder.findViewById(conView, R.id.add);

		UserService.displayAvatar(addRequest.getRequestSender(), avatarView);
		int status = addRequest.getStatus();
		if (status == AddRequest.STATUS_PENDING) {
			addBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					agreeAdd(addBtn, addRequest);
				}
			});
		} else if (status == AddRequest.STATUS_DONE) {
			toAgreedTextView(addBtn);
		}
		if (addRequest.getRequestSender() != null) {
			nameView.setText(addRequest.getRequestSender().getUsername());
		}
		return conView;
	}

	public void toAgreedTextView(Button addBtn) {
		addBtn.setText(R.string.agreed);
		addBtn.setBackgroundDrawable(null);
		addBtn.setTextColor(Utils.getColor(R.color.base_color_text_black));
		addBtn.setEnabled(false);
	}

	private void agreeAdd(final Button addBtn, final AddRequest addRequest) {
		new NetworkAsyncTask(ctx) {
			@Override
			protected void doInBack() throws Exception {
				CloudService.agreeAddRequest(addRequest.getObjectId());
			}

			@Override
			protected void onPost(Exception e) {
				if (e != null) {
					Utils.toast(e.getMessage());
				} else {
					// update button view
					toAgreedTextView(addBtn);
				}
			}
		}.execute();
	}
}
