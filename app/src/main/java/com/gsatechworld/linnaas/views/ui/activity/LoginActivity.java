package com.gsatechworld.linnaas.views.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.gsatechworld.linnaas.R;
import com.gsatechworld.linnaas.databinding.ActivityLoginBinding;
import com.gsatechworld.linnaas.utils.login.LoginResponse;
import com.gsatechworld.linnaas.utils.login.LoginResultResp;
import com.gsatechworld.linnaas.utils.login.VerifyOtpResp;
import com.gsatechworld.linnaas.utils.login.VerifyOtpResult;
import com.gsatechworld.linnaas.viewmodels.LoginViewModel;
import com.gsatechworld.linnaas.viewmodels.VerifyOtpModel;
import com.gsatechworld.linnaas.views.base.BaseActivity;
import com.hbb20.CountryCodePicker;

import javax.inject.Inject;

import dmax.dialog.SpotsDialog;

import static dagger.android.AndroidInjection.inject;

public class LoginActivity extends BaseActivity<LoginViewModel> {

    ActivityLoginBinding binding;

    LoginViewModel viewModel;
    //private AlertDialog progressDialog;
    SharedPreferences preferences;
    TextInputEditText mobile_txt;
    CountryCodePicker cp;
    private AlertDialog progressDialog;

    @Inject
    ViewModelProvider.Factory factory;

    boolean permissionGranted;

    @Override
    public LoginViewModel getViewModel() {
        viewModel = ViewModelProviders.of(this, factory).get(LoginViewModel.class);
        return viewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject(this);

        binding = DataBindingUtil.setContentView(LoginActivity.this, R.layout.activity_login_);

        //progressDialog = new SpotsDialog(this, R.style.Custom);
        askAudioPermission();
        progressDialog = new SpotsDialog(this, R.style.Custom);

        binding.btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //progressDialog.show();
                binding.spinKit.setVisibility(View.VISIBLE);
                if (binding.mobileNumber.getText().toString().equals("") ||
                        (binding.mobileNumber.getText().toString().length() != 10)) {
                    //progressDialog.dismiss();
                    binding.spinKit.setVisibility(View.GONE);
                    validMobile();
                } else {
                    loginResult();
                }
            }
        });

    }

    private void validMobile() {
        Toast.makeText(this, "Please enter valid mobile number", Toast.LENGTH_SHORT).show();
    }

    private void loginResult() {
        viewModel.isLoginSuccess(new LoginResultResp(binding.ccp.getSelectedCountryCode(), binding.mobileNumber.getText().toString()))
                .observe(this, new Observer<LoginResponse>() {
                    @Override
                    public void onChanged(LoginResponse loginResultResp) {
                        //progressDialog.dismiss();
                        binding.spinKit.setVisibility(View.GONE);
                        if (loginResultResp != null) {
                            if (loginResultResp.getLoginResultResp().getSuccess().equals("true")) {
                                preferences = getSharedPreferences("logindata", 0);
                                String alreadyuser = preferences.getString("alreadyuser", "0");
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("mobile", binding.mobileNumber.getEditableText().toString());
                                editor.putString("country_code", binding.ccp.getSelectedCountryCode().toString());
                                editor.putString("audiopermission", "1");
                                editor.apply();
                                progressDialog.show();
                                doSubmit();

                            }
                            else{

                                Toast.makeText(LoginActivity.this, "Please check internet connection", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    public void doSubmit() {
        preferences = getSharedPreferences("logindata",0);
        String mobile_no = preferences.getString("mobile","");

        viewModel.sendOtp(new VerifyOtpResp( mobile_no,"1111"))
                .observe(this, new Observer<VerifyOtpResult>() {
                    @Override
                    public void onChanged(VerifyOtpResult verifyOtpResult) {
                        progressDialog.dismiss();
                        if(verifyOtpResult != null && verifyOtpResult.getVerifyOtpResp().getSuccess().equals("true")){


                            preferences = getSharedPreferences("logindata",0);
                            String alreadyuser = preferences.getString("alreadyuser","0");
                            SharedPreferences.Editor editor = preferences.edit();

                            if(verifyOtpResult.getVerifyOtpResp().getData().size() == 0) {

                                startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                                finish();
                            }
                            else {

                                editor.putString("id",verifyOtpResult.getVerifyOtpResp().getData().get(0).getId());
                                editor.putString("firstname",verifyOtpResult.getVerifyOtpResp().getData().get(0).getFirst_name());
                                editor.putString("lastname",verifyOtpResult.getVerifyOtpResp().getData().get(0).getLast());
                                editor.apply();
                                startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                                finish();
                            }
                        }
                    }
                });

    }

    private void askAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        } else
            permissionGranted = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true;
            }
        }
    }
}