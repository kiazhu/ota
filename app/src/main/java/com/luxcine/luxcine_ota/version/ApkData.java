package com.luxcine.luxcine_ota_customized.version;

public class ApkData {
    private String date;
    private String url;
    private String md5;
    private String storagemem;
    private String version_code;
    private String describe;

    public String getVersion_code() {
        return version_code;
    }

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getStoragemem() {
        return storagemem;
    }

    public void setStoragemem(String storagemem) {
        this.storagemem = storagemem;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }


}
