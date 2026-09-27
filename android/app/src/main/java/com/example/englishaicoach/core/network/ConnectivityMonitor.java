package com.example.englishaicoach.core.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.lifecycle.LiveData;

public final class ConnectivityMonitor extends LiveData<Boolean> {
    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback callback =
            new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    refresh();
                }

                @Override
                public void onLost(Network network) {
                    refresh();
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                    refresh();
                }
            };

    public ConnectivityMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onActive() {
        super.onActive();
        refresh();
        connectivityManager.registerDefaultNetworkCallback(callback);
    }

    @Override
    protected void onInactive() {
        connectivityManager.unregisterNetworkCallback(callback);
        super.onInactive();
    }

    private void refresh() {
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = network == null
                ? null : connectivityManager.getNetworkCapabilities(network);
        postValue(capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
    }
}
