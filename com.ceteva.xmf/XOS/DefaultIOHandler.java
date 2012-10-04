package XOS;

public class DefaultIOHandler implements XmfIOHandler  {

	public Object[] dialog(String dialogType, Object[] inputData) throws XmfIOException {
		if (dialogType.equals("LicenseError"))
	      return licenseKeyError(inputData);
		return null;
	}
	
	public Object[] licenseKeyError(Object[] inputData) throws XmfIOException {
		System.out.println(inputData[0]);
		return null;
	}

}
