package Engine;

public class MachineError extends Error {

	int error; // The error code.
	
	public MachineError(int error, String message) {
		super(message);
		this.error = error;
	}
	
	public String toString() {
		return "Machine Error: " + error + " " + getMessage();
	}

}