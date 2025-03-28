package requestandresult;

public record CreateGameRequest(String authToken, String gameName) {
}
