package com.gsatechworld.linnaas.utils.login;

public class LoginResponse {


    private LoginResultResp loginResultResp;

    public LoginResponse(LoginResultResp loginResultResp){
        this.loginResultResp = loginResultResp;
    }

    public LoginResultResp getLoginResultResp() {
        return loginResultResp;
    }

    public void setLoginResultResp(LoginResultResp loginResultResp) {
        this.loginResultResp = loginResultResp;
    }
}
