package liu.weiran.chatate.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import liu.weiran.chatate.R;
import liu.weiran.chatate.ui.views.HeaderLayout;

public class BaseFragment extends Fragment {
  HeaderLayout headerLayout;
  Context ctx;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    ctx = getActivity();
    headerLayout = (HeaderLayout) getView().findViewById(R.id.headerLayout);
  }
}
