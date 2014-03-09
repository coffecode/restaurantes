package codecoffe.restaurantes.utilitarios;

public class Header 
{
	private short header;
	private Object extra, extra2;

	public Header(short header) {
		this.header = header;
	}
	
	public Header(short header, Object extra) {
		this.header = header;
		this.extra = extra;
	}

	public Header(short header, Object extra, Object extra2) {
		this.header = header;
		this.extra = extra;
		this.extra2 = extra2;
	}

	public short getHeader() {
		return header;
	}

	public void setHeader(short header) {
		this.header = header;
	}

	public Object getExtra() {
		return extra;
	}

	public void setExtra(Object extra) {
		this.extra = extra;
	}

	public Object getExtra2() {
		return extra2;
	}

	public void setExtra2(Object extra2) {
		this.extra2 = extra2;
	}
}