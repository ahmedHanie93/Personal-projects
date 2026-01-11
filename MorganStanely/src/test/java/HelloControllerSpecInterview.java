import org.junit.jupiter.api.Test;
import org.springframework.test.util.AssertionErrors;
import org.springframework.web.client.RestTemplate;

public class HelloControllerSpecInterview {

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void shouldReturnGreeting() {
        String url = "http://localhost:8080/hello";
        String response = restTemplate.getForObject(url, String.class);
        AssertionErrors.assertEquals(null, "Hello Morgan Stanley!", response);
    }
}
