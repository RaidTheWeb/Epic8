package tech.raidtheweb.epicsys.devices;

public class TestDevice implements Device {

	public TestDevice() {}
	
	@Override
	public short poll() {
		return 0x39;
	}

	@Override
	public void query(short data) {
		System.out.println(String.format("Recived data from CPU: 0x%04x", data));
	}

}
