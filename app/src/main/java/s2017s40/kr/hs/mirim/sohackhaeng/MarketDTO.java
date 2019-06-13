package s2017s40.kr.hs.mirim.sohackhaeng;

public class MarketDTO {
    private String address;
    private String code;
    private String name;
    MarketDTO(String address, String code, String name){
        this.address = address;
        this.code = code;
        this.name = name;
    }
    public String getCode() {
        return code;
    }

    public String getAddress() {
        return address;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
