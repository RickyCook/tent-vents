package com.thatpanda.tentvents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	/**
	 * The default email/url to populate fields with.
	 */
	public static final String EXTRA_URL = "com.thatpanda.tentvents.extra.URL";
	public static final String EXTRA_EMAIL = "com.thatpanda.tentvents.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for url, email and password at the time of the login attempt.
	private String mUrl;
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mUrlView;
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUrl = getIntent().getStringExtra(EXTRA_URL);
		if (mUrl == null) mUrl = getDefaultUri();
		mUrlView = (EditText) findViewById(R.id.url);
		mUrlView.setText(mUrl);
		mUrlView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// Lost focus so do updates
				if (!hasFocus) {
					Editable text = mUrlView.getText();
					String textString = text.toString();
					
					// TODO: Trim whitespace
					// Prepend URI scheme
					if (!textString.contains("://")) {
						String prepend = getString(
								R.string.default_uri_scheme) + "://";
						text.insert(0,  prepend);
					}
					// TODO: Remove trailing slash
				}
			}
		});
		
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		if (mEmail == null) mEmail = getDefaultEmail();
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUrlView.setError(null);
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUrl = mUrlView.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}
		
		// Check for a valid url.
		if (TextUtils.isEmpty(mUrl)) {
			mUrlView.setError(getString(R.string.error_field_required));
			focusView = mUrlView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			setDefaultUri(mUrl);
			setDefaultEmail(mEmail);
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(this);
			mAuthTask.execute(
					getApiUri(R.string.api_login),
					mEmailView.getText().toString(),
					mPasswordView.getText().toString()
			);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	/**
	 * Gets an API URI from URL text and resource id representing API command
	 */
	private String getApiUri(int id) {
		return
				  mUrlView.getText().toString()
				+ "/"
				+ getString(id);
	}
	private void setDefaultUri(String uri) {
		SharedPreferences.Editor p = getPreferences(MODE_PRIVATE).edit();
		p.putString("uri", uri);
		p.commit();
	}
	public String getDefaultUri() {
		SharedPreferences p = getPreferences(MODE_PRIVATE);
		return p.getString("uri", null);
	}
	private void setDefaultEmail(String email) {
		SharedPreferences.Editor p = getPreferences(MODE_PRIVATE).edit();
		p.putString("email", email);
		p.commit();
	}
	public String getDefaultEmail() {
		SharedPreferences p = getPreferences(MODE_PRIVATE);
		return p.getString("email", null);
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
		private HashMap<EditText, String> errors = new HashMap<EditText, String>();
		
		private AlertDialog alertDialog;
		private boolean alertReady = false;

		public UserLoginTask(Context context) {
			alertDialog = new AlertDialog.Builder(context).create();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			String loginUri = params[0];
			String email = params[1];
			String password = params[2];
			
			// TODO: login
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(loginUri + "/local.json");
				
				List <NameValuePair> httpParams = new ArrayList<NameValuePair>();
				httpParams.add(new BasicNameValuePair("email", email));
				httpParams.add(new BasicNameValuePair("password", password));
				httpPost.setEntity(new UrlEncodedFormEntity(httpParams, HTTP.UTF_8));
				
				HttpResponse response = client.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				switch (statusCode) {
					case 200:
						BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
						StringBuilder builder = new StringBuilder();
						for (String line = null; (line = reader.readLine()) != null;) {
						    builder.append(line).append("\n");
						}
						errors.put(mEmailView, builder.toString());
						return false;
						//return true;
					case 403:
						errors.put(mPasswordView, getString(R.string.error_incorrect_password));
						return false;
					// TODO: parse error results for message
					default:
						errors.put(mUrlView, statusLine.getReasonPhrase());
						return false;
				}
			} catch (SecurityException e) {
				alertDialog.setTitle(getString(R.string.security_title));
				alertDialog.setMessage(getString(R.string.security_message)
						.replace("%s", e.getLocalizedMessage()));
				alertReady = true;
				
				return false;
			} catch (IOException e) {
				errors.put(mUrlView, e.getLocalizedMessage());
				return false;
			}
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			if (alertReady) {
				alertDialog.show();
				alertReady = false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);
			
			for (EditText e : errors.keySet()) {
				e.setError(errors.get(e));
				e.requestFocus();
			}
			
			if (success) {
				finish();
			} else if (alertReady) {
				alertDialog.show();
				alertReady = false;
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
