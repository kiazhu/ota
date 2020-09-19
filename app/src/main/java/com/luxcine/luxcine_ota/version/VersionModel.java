package com.luxcine.luxcine_ota_customized.version;



public class VersionModel extends BaseModel {


    /**
     * name : luxcine_update
     * version : 1
     * changelog : null
     * updated_at : 1572590410
     * versionShort : 1.0
     * build : 1
     * installUrl : http://download.fir.im/apps/5dbbd345b2eb4656076b8cab/install?download_token=aded96260b4d5825211cc1a71a2e3c37&source=update
     * install_url : http://download.fir.im/apps/5dbbd345b2eb4656076b8cab/install?download_token=aded96260b4d5825211cc1a71a2e3c37&source=update
     * direct_install_url : http://download.fir.im/apps/5dbbd345b2eb4656076b8cab/install?download_token=aded96260b4d5825211cc1a71a2e3c37&source=update
     * update_url : http://fir.im/3cel
     * binary : {"fsize":1655206}
     */

    private String name;
    private String version;
    private Object changelog;
    private int updated_at;
    private String versionShort;
    private String build;
    private String installUrl;
    private String install_url;
    private String direct_install_url;
    private String update_url;
    private BinaryBean binary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Object getChangelog() {
        return changelog;
    }

    public void setChangelog(Object changelog) {
        this.changelog = changelog;
    }

    public int getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(int updated_at) {
        this.updated_at = updated_at;
    }

    public String getVersionShort() {
        return versionShort;
    }

    public void setVersionShort(String versionShort) {
        this.versionShort = versionShort;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getInstallUrl() {
        return installUrl;
    }

    public void setInstallUrl(String installUrl) {
        this.installUrl = installUrl;
    }

    public String getInstall_url() {
        return install_url;
    }

    public void setInstall_url(String install_url) {
        this.install_url = install_url;
    }

    public String getDirect_install_url() {
        return direct_install_url;
    }

    public void setDirect_install_url(String direct_install_url) {
        this.direct_install_url = direct_install_url;
    }

    public String getUpdate_url() {
        return update_url;
    }

    public void setUpdate_url(String update_url) {
        this.update_url = update_url;
    }

    public BinaryBean getBinary() {
        return binary;
    }

    public void setBinary(BinaryBean binary) {
        this.binary = binary;
    }

    public static class BinaryBean {
        /**
         * fsize : 1655206
         */

        private int fsize;

        public int getFsize() {
            return fsize;
        }

        public void setFsize(int fsize) {
            this.fsize = fsize;
        }
    }
}
