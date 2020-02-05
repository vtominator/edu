package hu.vtominator.edu.Model;


public class Comment {

    private String username;
    private String comment;


    public Comment(String username, String comment) {
        this.username = username;
        this.comment = comment;
    }


    public String getUsername() {
        return username;
    }

    public String getComment() {
        return comment;
    }


}