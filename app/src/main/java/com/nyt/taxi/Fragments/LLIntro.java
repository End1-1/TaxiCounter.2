package com.nyt.taxi.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.nyt.taxi.Activities.ActivityServer;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.databinding.FragmentIntroBinding;

public class LLIntro extends Fragment implements View.OnClickListener {

    private FragmentIntroBinding bind;
    private IntroInterface introInterface;

    public LLIntro() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bind = FragmentIntroBinding.inflate(getLayoutInflater(), container, false);
        bind.txtLogin.setOnClickListener(this);
        bind.imgShowPassword.setOnClickListener(this);
        bind.txtPrivacyPolicy.setOnClickListener(this);
        //bind.imgAnim.setBackgroundResource(R.drawable.intro_anim);
        //((AnimationDrawable)bind.imgAnim.getBackground()).start();
        bind.txtVersion.setText(UPref.infoCode());
        bind.txtServer.setText(UConfig.host());
        return bind.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IntroInterface) {
            introInterface = (IntroInterface) context;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtLogin:
                if (bind.etUsername.getText().toString().equalsIgnoreCase("server")) {
                    startActivity(new Intent(getContext(), ActivityServer.class));
                    return;
                }
                introInterface.onLogin(bind.etUsername.getText().toString(), bind.etPassword.getText().toString());
                break;
            case R.id.imgShowPassword:
                if (bind.etPassword.getInputType() == InputType.TYPE_CLASS_TEXT) {
                    bind.etPassword.setInputType(129);
                } else {
                    bind.etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                break;
            case R.id.txtPrivacyPolicy:
                String url = "https://nyt.ru/politika-konfidenczialnosti/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
        }
    }

    public interface IntroInterface {
        void onLogin(String username, String password);
    }
}
