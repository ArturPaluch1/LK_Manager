package LKManager.model.UserMZ;


import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement(name = "UserData")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class UserData implements Serializable {

    private String username;
    private Integer userId;
    private String countryShortname;
    private String userImage;
    private UserData opponentUser;
    private List<Team> teamlist = new ArrayList();


    public UserData(String username, Integer userId, String countryShortname, String userImage, List<Team> teamlist) {
        this.username = username;
        this.userId = userId;
        this.countryShortname = countryShortname;
        this.userImage = userImage;
        this.teamlist = teamlist;


    }

    public UserData() {
    }

    public UserData getOpponentUser() {
        return opponentUser;
    }

    public void setOpponentUser(UserData opponentUser) {
        this.opponentUser = opponentUser;
    }

    @XmlElement(name = "Team")
    public List<LKManager.model.UserMZ.Team> getTeamlist() {
        return teamlist;
    }

    public void setTeamlist(Team team) {

        this.teamlist.add(team);
    }

    public void setTeamlist(List<Team> teamlist) {
        this.teamlist = teamlist;
    }

    @XmlAttribute
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlAttribute
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer setUserId) {
        this.userId = setUserId;
    }

    @XmlAttribute
    public String getCountryShortname() {
        return countryShortname;
    }

    public void setCountryShortname(String countryShortname) {
        this.countryShortname = countryShortname;
    }

    @XmlAttribute
    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "username='" + username + '\'' +
                ", userId=" + userId +
                ", countryShortname='" + countryShortname + '\'' +
                ", userImage='" + userImage + '\'' +
                ", teamlist=" + teamlist +
                '}';
    }
}
