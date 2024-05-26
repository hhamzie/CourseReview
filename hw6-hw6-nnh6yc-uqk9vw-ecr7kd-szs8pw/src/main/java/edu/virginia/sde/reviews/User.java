package edu.virginia.sde.reviews;

public class User {
    private String username;
    private String password;
    public User(String username, String password){
        this.username = username;
        if (password.length() >= 8){
            setPassword(password);
        }
    }

    public void setPassword(String password){
        this.password = password;
    }
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
