package tech.raidtheweb.epicsys.devices;

public interface Device {
	short poll();
	void query(short data);
}
