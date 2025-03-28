package requestandresult;

public record JoinGameRequest(String authToken, String playerColor, Integer gameID) {
}
