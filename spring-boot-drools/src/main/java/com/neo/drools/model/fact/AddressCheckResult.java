package com.neo.drools.model.fact;


public class AddressCheckResult {
	// true:通过校验；false：未通过校验
    private boolean postCodeResult = false; 
    private int num = 0;

    public boolean isPostCodeResult() {
        return postCodeResult;
    }

    public void setPostCodeResult(boolean postCodeResult) {
        this.postCodeResult = postCodeResult;
    }

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
}
