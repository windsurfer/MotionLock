package com.wesaphzt.privatelock.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wesaphzt.privatelock.R;

public class FragmentDonate extends Fragment {

    private Context context;

    private static final String BITCOIN_PREFIX = "bitcoin:";
    private static final String LITECOIN_PREFIX = "litecoin:";
    private static final String ETHEREUM_PREFIX = "ethereum:";
    private static final String MONERO_PREFIX = "monero:";

    private static String BITCOIN_ADDRESS;
    private static String BITCOIN_FULL;

    private static String LITECOIN_ADDRESS;
    private static String LITECOIN_FULL;

    private static String ETHEREUM_ADDRESS;
    private static String ETHEREUM_FULL;

    private static String MONERO_ADDRESS;
    private static String MONERO_FULL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donate, container, false);

        setHasOptionsMenu(true);

        context = getContext();


        return view;
    }

    private void copyToClipboard(String AUTHOR_EXTRA) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.fragment_donate_clipboard_label), AUTHOR_EXTRA);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, R.string.fragment_donate_clipboard_message, Toast.LENGTH_SHORT).show();
    }

    private void openURI(String uri) {
        Intent openURI = new Intent(Intent.ACTION_VIEW);
        openURI.setData(Uri.parse(uri));
        startActivity(openURI);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //hide action bar menu
        menu.setGroupVisible(R.id.menu_main, false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set title
        getActivity().setTitle(getString(R.string.fragment_donate_title));
    }
}