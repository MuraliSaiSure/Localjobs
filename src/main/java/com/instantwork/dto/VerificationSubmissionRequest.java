package com.instantwork.dto;

public class VerificationSubmissionRequest {
    private Long userId;
    private String docType; // AADHAAR or PAN
    private String docNumber; // Raw input from user (will be validated and masked before saving)
    private String nameOnDoc;

    public VerificationSubmissionRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getDocNumber() { return docNumber; }
    public void setDocNumber(String docNumber) { this.docNumber = docNumber; }

    public String getNameOnDoc() { return nameOnDoc; }
    public void setNameOnDoc(String nameOnDoc) { this.nameOnDoc = nameOnDoc; }
}
