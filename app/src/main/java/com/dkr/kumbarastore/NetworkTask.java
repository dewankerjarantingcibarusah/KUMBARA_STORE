package com.dkr.kumbarastore;

import android.os.AsyncTask;
import android.widget.Toast;
import android.content.Context;

public class NetworkTask extends AsyncTask<Void, Void, Boolean> {

    private Context context;

    public NetworkTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return NetworkUtils.isPingSuccessful("https://www.google.com");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            // Network is good
        } else {

        }
    }
}
