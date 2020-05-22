package com.github.shadowsocks.bean;

import com.github.shadowsocks.util.AESUtil;
import com.github.shadowsocks.util.StringUtil;

/**
 * {SERVERID=2,IP:'35.224.102.245',PORT:'8898',PWD:'a123456',PROTOCOL:'origin',EMETHOD:' aes-256-cfb',OBFS:' plain'}
 */
public class NodeBean {
    private int SERVERID = 0;
    private String IP;
    private String PORT;
    private String PWD;
    private String PROTOCOL;
    private String EMETHOD;
    private String OBFS;

    public int getSERVERID() {
        return SERVERID;
    }

    public void setSERVERID(int SERVERID) {
        this.SERVERID = SERVERID;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getPORT() {
        return PORT;
    }

    public void setPORT(String PORT) {
        this.PORT = PORT;
    }

    public String getPWD() {
        return PWD;
    }

    public void setPWD(String PWD) {
        this.PWD = PWD;
    }

    public String getPROTOCOL() {
        return PROTOCOL;
    }

    public void setPROTOCOL(String PROTOCOL) {
        this.PROTOCOL = PROTOCOL;
    }

    public String getEMETHOD() {
        return EMETHOD;
    }

    public void setEMETHOD(String EMETHOD) {
        this.EMETHOD = EMETHOD;
    }

    public String getOBFS() {
        return OBFS;
    }

    public void setOBFS(String OBFS) {
        this.OBFS = OBFS;
    }

    public void format() {
        IP = StringUtil.trim(IP);
        PORT = StringUtil.trim(PORT);
        PROTOCOL = StringUtil.trim(PROTOCOL);
        EMETHOD = StringUtil.trim(EMETHOD);
        OBFS = StringUtil.trim(OBFS);
    }

    @Override
    public String toString() {
        return "NodeBean{" +
                "SERVERID=" + SERVERID +
                ", IP='" + IP + '\'' +
                ", PORT='" + PORT + '\'' +
                ", PWD='" + PWD + '\'' +
                ", PROTOCOL='" + PROTOCOL + '\'' +
                ", EMETHOD='" + EMETHOD + '\'' +
                ", OBFS='" + OBFS + '\'' +
                '}';
    }

    public boolean isInvalid() {
        return StringUtil.isEmpty(IP, PORT, PWD, EMETHOD);
    }
//
//    public static String example() {
//        String str = "{\"SERVERID\":2,\"IP\":\"35.224.102.245\",\"PORT\":\"8898\",\"PWD\":\"a123456\",\"PROTOCOL\":\"origin\",\"EMETHOD\":\"aes-256-cfb\",\"OBFS\":\"plain\"}";
//        return AESUtil.encrypt(str, AESUtil.AES_KEY);
//    }
}
