package com.capgemini.kabanos.common.enums;

public enum TestFrameworkType {
	JUNIT(".java"),
	CUCUMBER(".feature")
//	//not implemented yet
//	JAVA_SCRIPT(".js")
	;
	
	private String fileType;
	TestFrameworkType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileType() {
		return fileType;
	}
}
