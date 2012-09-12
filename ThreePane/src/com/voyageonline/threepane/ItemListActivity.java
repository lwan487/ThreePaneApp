package com.voyageonline.threepane;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class ItemListActivity extends FragmentActivity {

	private boolean mTwoPane;
	private UIControllerBase mUIController;

	private void initUIController() {
		if (getResources().getBoolean(R.bool.use_two_pane)) {
			mUIController = new UIControllerTwoPane(this);
		} else {
			mUIController = new UIControllerOnePane(this);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		initUIController();
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(mUIController.getLayoutId());
		mUIController.onActivityViewReady();

		if (savedInstanceState != null) {
			mUIController.onRestoreInstanceState(savedInstanceState);
		} else {
			final Intent intent = getIntent();
			final MyContext viewContext = null; //MessageListContext
					//.forIntent(this, intent);
		    final long messageId = 0; //intent.getLongExtra(EXTRA_MESSAGE_ID,
						//Message.NO_MESSAGE);
			mUIController.open(viewContext, messageId);
		}
		mUIController.onActivityCreated();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
