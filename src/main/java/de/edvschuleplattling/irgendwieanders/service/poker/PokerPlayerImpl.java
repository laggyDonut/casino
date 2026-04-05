package de.edvschuleplattling.irgendwieanders.service.poker;

import de.edvschuleplattling.irgendwieanders.websocket.poker.dto.PublicUserProfileDto;
import de.simonaltschaeffl.poker.model.Player;

import java.util.Map;

public class PokerPlayerImpl extends Player {

    private final PublicUserProfileDto userProfile;

    public PokerPlayerImpl(PublicUserProfileDto userProfile, int chips) {
        super(userProfile.userId().toString(), userProfile.displayName(), chips);
        this.userProfile = userProfile;
    }

    public PublicUserProfileDto getUserProfile() {
        return userProfile;
    }

    @Override
    public void onLeave() {

    }

    @Override
    public void onHandEnded(Map<String, Integer> map) {

    }
}
