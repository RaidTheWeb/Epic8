package tech.raidtheweb.epicsys.hexdump;

import java.util.Arrays;

public final class SimpleDataProvider implements IDataProvider {

	private final byte[] m_data;
	private final int max_length;

	public SimpleDataProvider(final byte[] data, final int max_length) {
		this.m_data = data;
		this.max_length = max_length;
	}

	@Override
	public void addListener(final IDataChangedListener listener) {
	}

	@Override
	public byte[] getData() {
		return getData(0L, getDataLength());
	}

	@Override
	public byte[] getData(final long offset, final int length) {
		return Arrays.copyOfRange(this.m_data, (int) offset, (int) (offset + length));
	}

	@Override
	public int getDataLength() {
		return this.max_length;
	}

	public long getOffset() {
		return 0L;
	}

	@Override
	public boolean hasData(final long offset, final int length) {
		return true;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean keepTrying() {
		return false;
	}

	@Override
	public void removeListener(final IDataChangedListener listener) {
	}

	@Override
	public void setData(final long offset, final byte[] data) {
		System.arraycopy(data, 0, this.m_data, (int) offset, data.length);
	}
}