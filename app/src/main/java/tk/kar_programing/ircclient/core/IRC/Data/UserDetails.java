package tk.kar_programing.ircclient.core.IRC.Data;


public class UserDetails {
    public String nickname;
    public String username;
    public String realname;

    public UserDetails(String nickname, String username, String realname){
        this.nickname = nickname;
        this.username = username;
        this.realname = realname;
    }
}
