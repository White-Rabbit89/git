package com.example.haveavacation;

import java.util.Locale;

import com.dropbox.chooser.android.DbxChooser;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		
	    ///////////////////////////////////////////////////////////////////////////
	    //                      Your app-specific settings.                      //
	    ///////////////////////////////////////////////////////////////////////////

	    // Replace this with your app key and secret assigned by Dropbox.
	    // Note that this is a really insecure way to do this, and you shouldn't
	    // ship code which contains your key & secret in such an obvious way.
	    // Obfuscation is good.
	    private static final String APP_KEY = "7o718xwo8a1v3v8";
	    private static final String APP_SECRET = "94egfpwvf3q1zrt";

	    ///////////////////////////////////////////////////////////////////////////
	    //                      End app-specific settings.                       //
	    ///////////////////////////////////////////////////////////////////////////
	    
	    // You don't need to change these, leave them alone.
	    private static final String ACCOUNT_PREFS_NAME = "prefs";
	    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
	    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	    private static final boolean USE_OAUTH1 = false;

	    private DropboxAPI<AndroidAuthSession> mApi;
	    private Button mSubmit;
	    private Context globalContext;
	    
	    private boolean mLoggedIn;
	    
	    private View rootView;

		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			
			globalContext = this.getActivity();

	        
			// We create a new AuthSession so that we can use the Dropbox API.
	        AndroidAuthSession session = buildSession();
	        mApi = new DropboxAPI<AndroidAuthSession>(session);
			
	        checkAppKeySetup();
	        
			mSubmit = (Button)rootView.findViewById(R.id.auth_button);

	        mSubmit.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                // This logs you out if you're logged in, or vice versa
	                if (mLoggedIn) {
	                    logOut();
	                } else {
	                    // Start the remote authentication
	                    if (USE_OAUTH1) {
	                        mApi.getSession().startAuthentication(globalContext);
	                    } else {
	                        mApi.getSession().startOAuth2Authentication(globalContext);
	                    }
	                }
	            }
	        });
	        
	        // Display the proper UI state if logged in or not
	        setLoggedIn(mApi.getSession().isLinked());
			
			return rootView;
		}
	    
	    private void clearKeys() {
	        SharedPreferences prefs = this.getActivity().getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
	        Editor edit = prefs.edit();
	        edit.clear();
	        edit.commit();
	    }
		
	    private void logOut() {
	        // Remove credentials from the session
	        mApi.getSession().unlink();

	        // Clear our stored keys
	        clearKeys();
	        // Change UI state to display logged out version
	        setLoggedIn(false);
	    }   
		
	    /**
	     * Convenience function to change UI state based on being logged in
	     */
	    private void setLoggedIn(boolean loggedIn) {
	    	mLoggedIn = loggedIn;
	    	if (loggedIn) {
//				mSubmit.setText("Unlink from Dropbox");
//				mDisplay.setVisibility(View.VISIBLE);
	    	} else {
//	    		mSubmit.setText("Link with Dropbox");
//	            mDisplay.setVisibility(View.GONE);
//	            mImage.setImageDrawable(null);
	    	}
	    }
	    
	    /**
	     * Shows keeping the access keys returned from Trusted Authenticator in a local
	     * store, rather than storing user name & password, and re-authenticating each
	     * time (which is not to be done, ever).
	     */
	    private void loadAuth(AndroidAuthSession session) {
	        SharedPreferences prefs = this.getActivity().getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
	        String key = prefs.getString(ACCESS_KEY_NAME, null);
	        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
	        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

	        if (key.equals("oauth2:")) {
	            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
	            session.setOAuth2AccessToken(secret);
	        } else {
	            // Still support using old OAuth 1 tokens.
	            session.setAccessTokenPair(new AccessTokenPair(key, secret));
	        }
	    }
	    
		private void checkAppKeySetup() {
			// Check to make sure that we have a valid app key
			if (APP_KEY.startsWith("CHANGE") || APP_SECRET.startsWith("CHANGE")) {
				showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
				this.getActivity().finish();
				return;
			}

			// Check if the app has set up its manifest properly.
			Intent testIntent = new Intent(Intent.ACTION_VIEW);
			String scheme = "db-" + APP_KEY;
			String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
			testIntent.setData(Uri.parse(uri));
			PackageManager pm = this.getActivity().getPackageManager();
			if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
				showToast("URL scheme in your app's "
						+ "manifest is not set up correctly. You should have a "
						+ "com.dropbox.client2.android.AuthActivity with the "
						+ "scheme: " + scheme);
				this.getActivity().finish();
			}
		}

		private void showToast(String msg) {
			Toast error = Toast.makeText(this.getActivity(), msg, Toast.LENGTH_LONG);
			error.show();
		}
	    
	    private AndroidAuthSession buildSession() {
	        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

	        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
	        loadAuth(session);
	        return session;
	    }
	}
}
