package XOS;

// User / password pairs

public class UserPassword {

	private String user;
	private String password;
	
	public UserPassword(String user, String password){
		this.user = user;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public String getUser() {
		return user;
	}

}
