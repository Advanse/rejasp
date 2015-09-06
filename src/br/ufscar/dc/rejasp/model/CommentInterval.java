package br.ufscar.dc.rejasp.model;

public class CommentInterval {
	private boolean lineComment;
	private int nStart;
	private int nLength;

	public CommentInterval(int nStart, int nLength, boolean lineComment) {
		this.nStart = nStart;
		this.nLength = nLength;
		this.lineComment = lineComment;
	}

	public boolean contains( int nIndex ) {
		if ( nIndex >= nStart && nIndex < nStart + nLength )
			return true;
		return false;
	}
	
	public void setStart(int start) {
		this.nStart = start;
	}

	public void setLength(int length) {
		this.nLength = length;
	}

	public void setLineComment(boolean lineComment) {
		this.lineComment = lineComment;
	}
	
	public int getStart() {
		return nStart;
	}
	
	public int getLength() {
		return nLength;
	}
	
	public boolean isLineComment() {
		return lineComment;
	}
}