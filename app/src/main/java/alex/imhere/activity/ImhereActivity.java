package alex.imhere.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseException;

import alex.imhere.R;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.fragment.StatusFragment;
import alex.imhere.fragment.view.AbstractView;
import alex.imhere.fragment.view.UiRunnable;

public class ImhereActivity extends AppCompatActivity
		implements StatusFragment.FragmentInteractionListener {

	ImhereModel model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String udid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		model = new ImhereModel(new Handler(), udid);

		setContentView(R.layout.activity_main);
		showUsersFragment(false);
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		AbstractView abstractView = (AbstractView) fragment;
		abstractView.setModel(model);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void showUsersFragment(final boolean doShow) {
		model.getUiHandler().post(new Runnable() {
			@Override
			public void run() {
				FragmentManager fragmentManager = getSupportFragmentManager();
				Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_users);

				FragmentTransaction transaction = fragmentManager.beginTransaction();
				transaction.setCustomAnimations(R.anim.push_up_in, R.anim.push_up_out);
				if (doShow) {
					transaction.show(fragment);
				} else {
					transaction.hide(fragment);
				}
				transaction.commit();
			}
		});
	}

	@Override
	public void onImhereClick(@NonNull final UiRunnable onPostExecute) {
		if (model.isCurrentSessionAlive()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					model.cancelCurrentSession();
					onPostExecute.run();
					showUsersFragment(false);
				}
			}).start();
		} else {
			final Context context = this;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						model.openNewSession();
						onPostExecute.run();
						showUsersFragment(true);
					} catch (ParseException e) {
						e.printStackTrace();
						String toaskString = "Error logining to server: " + e.getMessage();
						Toast.makeText(context, toaskString, Toast.LENGTH_SHORT).show();
					}
				}
			}).start();
		}
	}
}
